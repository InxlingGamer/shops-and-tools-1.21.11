package net.inklinggamer.celestium.player;

import net.inklinggamer.celestium.item.ModItems;
import net.inklinggamer.celestium.mixin.EntityInvoker;
import net.inklinggamer.celestium.network.SyncCelestiumWallClimbStatePayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class CelestiumBootsManager {
    private static final float WALL_CLIMB_SOUND_VOLUME_MULTIPLIER = 1.15F;
    private static final double WALL_CLIMB_SPEED = 0.2D;
    private static final double WALL_STRAFE_SPEED = 0.12D;
    private static final double WALL_STICK_SPEED = 0.08D;
    private static final double WALL_CONTACT_EPSILON = 1.0E-4D;
    private static final TagKey<Block> FENCES_TAG = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("minecraft", "fences"));
    private static final TagKey<Block> WALLS_TAG = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("minecraft", "walls"));
    private static final Direction[] HORIZONTAL_DIRECTIONS = {
            Direction.NORTH,
            Direction.SOUTH,
            Direction.WEST,
            Direction.EAST
    };

    private static final Map<UUID, PlayerState> STATES = new HashMap<>();

    private CelestiumBootsManager() {
    }

    public static void tickServer(MinecraftServer server) {
        STATES.entrySet().removeIf(entry -> server.getPlayerList().getPlayer(entry.getKey()) == null);
    }

    public static void tickPlayer(ServerPlayer player) {
        if (!player.isAlive()) {
            resetPlayerState(player);
            return;
        }

        if (!isCelestiumBootsEquipped(player)) {
            resetPlayerState(player);
            return;
        }

        PlayerState state = STATES.computeIfAbsent(player.getUUID(), uuid -> new PlayerState());
        AuthoritativeWallClimbMotion motion = resolveAuthoritativeWallClimbMotion(
                state.wallClimbing,
                state.wallDirection,
                state.wallStrafeBasis,
                resolveWallDirection(player, state.wallDirection, state.wallClimbing),
                player.getYRot(),
                getVerticalWallInput(player),
                getSidewaysInput(player)
        );
        state.wallClimbing = motion.active();
        state.wallDirection = motion.wallDirection();
        state.wallStrafeBasis = motion.wallStrafeBasis();
        syncWallClimbState(player, state, motion);

        if (!motion.active()) {
            resetWallClimbSoundState(state);
            return;
        }

        applyWallMovement(player, motion.velocity());
        playWallClimbSound(player, state);
    }

    public static boolean isCelestiumBootsEquipped(Player player) {
        return player.getItemBySlot(EquipmentSlot.FEET).is(ModItems.CELESTIUM_BOOTS);
    }

    public static void setWallClimbInput(ServerPlayer player, boolean sneakHeld, boolean forwardHeld, boolean backwardHeld, boolean leftHeld, boolean rightHeld) {
        PlayerState state = STATES.computeIfAbsent(player.getUUID(), uuid -> new PlayerState());
        state.sneakKeyHeld = sneakHeld;
        state.forwardKeyHeld = forwardHeld;
        state.backwardKeyHeld = backwardHeld;
        state.leftKeyHeld = leftHeld;
        state.rightKeyHeld = rightHeld;

        if (!sneakHeld) {
            state.wallClimbing = false;
            state.wallDirection = null;
            state.wallStrafeBasis = null;
            resetWallClimbSoundState(state);
        }
    }

    public static boolean shouldWallClimb(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return false;
        }

        PlayerState state = STATES.get(serverPlayer.getUUID());
        Direction preferredDirection = null;
        boolean continuingWallClimb = false;
        if (state != null) {
            continuingWallClimb = state.wallClimbing;
            preferredDirection = state.wallDirection;
        }

        return resolveWallClimbDirection(player, preferredDirection, continuingWallClimb) != null;
    }

    public static Direction resolveWallClimbDirection(Player player, Direction preferredDirection, boolean continuingWallClimb) {
        return resolveWallDirection(player, preferredDirection, continuingWallClimb);
    }

    public static BlockPos resolveWallClimbSoundPos(Player player, Direction wallDirection) {
        return wallDirection == null ? null : resolveWallSoundPos(player, wallDirection);
    }

    static AuthoritativeWallClimbMotion resolveAuthoritativeWallClimbMotion(
            boolean wasActive,
            Direction previousWallDirection,
            Vec3 previousWallStrafeBasis,
            Direction currentWallDirection,
            float attachmentYaw,
            int verticalInput,
            int sidewaysInput
    ) {
        if (currentWallDirection == null) {
            return AuthoritativeWallClimbMotion.inactive();
        }

        Vec3 wallStrafeBasis = previousWallStrafeBasis;
        if (!wasActive || previousWallDirection != currentWallDirection || wallStrafeBasis == null) {
            wallStrafeBasis = resolveWallStrafeBasis(currentWallDirection, attachmentYaw);
        }

        return new AuthoritativeWallClimbMotion(
                true,
                currentWallDirection,
                wallStrafeBasis,
                getWallClimbVelocity(currentWallDirection, wallStrafeBasis, verticalInput, sidewaysInput)
        );
    }

    public static WallClimbSoundTransition evaluateWallClimbSoundTransition(BlockPos previousSoundPos, BlockPos currentSoundPos) {
        if (currentSoundPos == null) {
            return new WallClimbSoundTransition(false, previousSoundPos);
        }

        if (previousSoundPos == null) {
            return new WallClimbSoundTransition(false, currentSoundPos);
        }

        return new WallClimbSoundTransition(!currentSoundPos.equals(previousSoundPos), currentSoundPos);
    }

    public static boolean hasWallClimbMovementInput(Player player) {
        return isMovingOnWall(player);
    }

    public static boolean hasWallClimbMovementInput(boolean forwardHeld, boolean backwardHeld, boolean leftHeld, boolean rightHeld) {
        return getVerticalWallInput(forwardHeld, backwardHeld) != 0 || getSidewaysInput(leftHeld, rightHeld) != 0;
    }

    private static Direction resolveWallDirection(Player player, Direction preferredDirection, boolean continuingWallClimb) {
        if (!isCelestiumBootsEquipped(player)
                || !player.isAlive()
                || player.isSpectator()
                || !isSneakKeyHeld(player)
                || player.isPassenger()
                || player.isSwimming()
                || player.isInWater()
                || player.isUnderWater()
                || player.isFallFlying()
                || player.getAbilities().flying) {
            return null;
        }

        if (!continuingWallClimb && !isForwardKeyHeld(player)) {
            return null;
        }

        if (preferredDirection != null && hasValidClimbSurface(player, preferredDirection, continuingWallClimb)) {
            return preferredDirection;
        }

        return findBestWallDirection(player, continuingWallClimb);
    }

    public static boolean shouldMuffleMovementVibrations(Entity entity, Holder<GameEvent> event) {
        if (!(entity instanceof Player player) || !isCelestiumBootsEquipped(player)) {
            return false;
        }

        return event == GameEvent.STEP || event == GameEvent.HIT_GROUND;
    }

    private static boolean isSneakKeyHeld(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            PlayerState state = STATES.get(serverPlayer.getUUID());
            return state != null && state.sneakKeyHeld;
        }

        return player.isShiftKeyDown();
    }

    private static boolean isForwardKeyHeld(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            PlayerState state = STATES.get(serverPlayer.getUUID());
            return state != null && state.forwardKeyHeld;
        }

        return player.zza > 0.0F;
    }

    private static boolean isBackwardKeyHeld(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            PlayerState state = STATES.get(serverPlayer.getUUID());
            return state != null && state.backwardKeyHeld;
        }

        return player.zza < 0.0F;
    }

    private static int getSidewaysInput(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            PlayerState state = STATES.get(serverPlayer.getUUID());
            if (state == null) {
                return 0;
            }

            return getSidewaysInput(state.leftKeyHeld, state.rightKeyHeld);
        }

        return Math.round(player.xxa);
    }

    private static int getSidewaysInput(boolean leftHeld, boolean rightHeld) {
        return (rightHeld ? 1 : 0) - (leftHeld ? 1 : 0);
    }

    private static int getVerticalWallInput(Player player) {
        return getVerticalWallInput(isForwardKeyHeld(player), isBackwardKeyHeld(player));
    }

    private static int getVerticalWallInput(boolean forwardHeld, boolean backwardHeld) {
        return (forwardHeld ? 1 : 0) - (backwardHeld ? 1 : 0);
    }

    private static boolean isMovingOnWall(Player player) {
        return getVerticalWallInput(player) != 0 || getSidewaysInput(player) != 0;
    }

    private static Direction findBestWallDirection(Player player, boolean continuingWallClimb) {
        Direction bestDirection = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        Vec3 horizontalLook = getHorizontalClimbLookVector(player);

        for (Direction direction : HORIZONTAL_DIRECTIONS) {
            if (!hasValidClimbSurface(player, direction, continuingWallClimb)) {
                continue;
            }

            Vec3 wallNormal = new Vec3(direction.getStepX(), 0.0D, direction.getStepZ());
            double score = horizontalLook.dot(wallNormal);
            if (score > bestScore) {
                bestScore = score;
                bestDirection = direction;
            }
        }

        return bestDirection;
    }

    private static boolean hasValidClimbSurface(Player player, Direction direction, boolean continuingWallClimb) {
        return hasClimbColumn(player, direction)
                || hasTallFenceOrWall(player, direction)
                || continuingWallClimb && !player.onGround() && (
                        hasClimbColumnBelowFeet(player, direction)
                        || hasTallFenceOrWallBelowFeet(player, direction)
                );
    }

    private static boolean hasClimbColumn(Player player, Direction direction) {
        Level world = player.level();
        AABB playerBox = player.getBoundingBox();
        int lowerY = Mth.floor(playerBox.minY + WALL_CONTACT_EPSILON);
        return hasClimbColumn(world, playerBox, lowerY, direction);
    }

    private static boolean hasClimbColumnBelowFeet(Player player, Direction direction) {
        Level world = player.level();
        AABB playerBox = player.getBoundingBox();
        int lowerY = Mth.floor(playerBox.minY + WALL_CONTACT_EPSILON) - 1;
        return hasClimbColumn(world, playerBox, lowerY, direction);
    }

    private static boolean hasTallFenceOrWall(Player player, Direction direction) {
        Level world = player.level();
        AABB playerBox = player.getBoundingBox();
        int lowerY = Mth.floor(playerBox.minY + WALL_CONTACT_EPSILON);
        return hasTallFenceOrWall(world, playerBox, lowerY, direction);
    }

    private static boolean hasTallFenceOrWallBelowFeet(Player player, Direction direction) {
        Level world = player.level();
        AABB playerBox = player.getBoundingBox();
        int lowerY = Mth.floor(playerBox.minY + WALL_CONTACT_EPSILON) - 1;
        return hasTallFenceOrWall(world, playerBox, lowerY, direction);
    }

    private static boolean hasClimbColumn(Level world, AABB playerBox, int lowerY, Direction direction) {
        return hasWallSegment(world, playerBox, lowerY, direction) && hasWallSegment(world, playerBox, lowerY + 1, direction);
    }

    private static boolean hasTallFenceOrWall(Level world, AABB playerBox, int y, Direction direction) {
        switch (direction) {
            case WEST -> {
                int x = Mth.floor(playerBox.minX - WALL_CONTACT_EPSILON);
                int minZ = Mth.floor(playerBox.minZ + WALL_CONTACT_EPSILON);
                int maxZ = Mth.floor(playerBox.maxZ - WALL_CONTACT_EPSILON);
                for (int z = minZ; z <= maxZ; z++) {
                    if (isTallFenceOrWall(world, new BlockPos(x, y, z))) {
                        return true;
                    }
                }
            }
            case EAST -> {
                int x = Mth.floor(playerBox.maxX + WALL_CONTACT_EPSILON);
                int minZ = Mth.floor(playerBox.minZ + WALL_CONTACT_EPSILON);
                int maxZ = Mth.floor(playerBox.maxZ - WALL_CONTACT_EPSILON);
                for (int z = minZ; z <= maxZ; z++) {
                    if (isTallFenceOrWall(world, new BlockPos(x, y, z))) {
                        return true;
                    }
                }
            }
            case NORTH -> {
                int z = Mth.floor(playerBox.minZ - WALL_CONTACT_EPSILON);
                int minX = Mth.floor(playerBox.minX + WALL_CONTACT_EPSILON);
                int maxX = Mth.floor(playerBox.maxX - WALL_CONTACT_EPSILON);
                for (int x = minX; x <= maxX; x++) {
                    if (isTallFenceOrWall(world, new BlockPos(x, y, z))) {
                        return true;
                    }
                }
            }
            case SOUTH -> {
                int z = Mth.floor(playerBox.maxZ + WALL_CONTACT_EPSILON);
                int minX = Mth.floor(playerBox.minX + WALL_CONTACT_EPSILON);
                int maxX = Mth.floor(playerBox.maxX - WALL_CONTACT_EPSILON);
                for (int x = minX; x <= maxX; x++) {
                    if (isTallFenceOrWall(world, new BlockPos(x, y, z))) {
                        return true;
                    }
                }
            }
            default -> {
                return false;
            }
        }

        return false;
    }

    private static boolean hasWallSegment(Level world, AABB playerBox, int y, Direction direction) {
        switch (direction) {
            case WEST -> {
                int x = Mth.floor(playerBox.minX - WALL_CONTACT_EPSILON);
                int minZ = Mth.floor(playerBox.minZ + WALL_CONTACT_EPSILON);
                int maxZ = Mth.floor(playerBox.maxZ - WALL_CONTACT_EPSILON);
                for (int z = minZ; z <= maxZ; z++) {
                    if (isClimbWall(world, new BlockPos(x, y, z))) {
                        return true;
                    }
                }
            }
            case EAST -> {
                int x = Mth.floor(playerBox.maxX + WALL_CONTACT_EPSILON);
                int minZ = Mth.floor(playerBox.minZ + WALL_CONTACT_EPSILON);
                int maxZ = Mth.floor(playerBox.maxZ - WALL_CONTACT_EPSILON);
                for (int z = minZ; z <= maxZ; z++) {
                    if (isClimbWall(world, new BlockPos(x, y, z))) {
                        return true;
                    }
                }
            }
            case NORTH -> {
                int z = Mth.floor(playerBox.minZ - WALL_CONTACT_EPSILON);
                int minX = Mth.floor(playerBox.minX + WALL_CONTACT_EPSILON);
                int maxX = Mth.floor(playerBox.maxX - WALL_CONTACT_EPSILON);
                for (int x = minX; x <= maxX; x++) {
                    if (isClimbWall(world, new BlockPos(x, y, z))) {
                        return true;
                    }
                }
            }
            case SOUTH -> {
                int z = Mth.floor(playerBox.maxZ + WALL_CONTACT_EPSILON);
                int minX = Mth.floor(playerBox.minX + WALL_CONTACT_EPSILON);
                int maxX = Mth.floor(playerBox.maxX - WALL_CONTACT_EPSILON);
                for (int x = minX; x <= maxX; x++) {
                    if (isClimbWall(world, new BlockPos(x, y, z))) {
                        return true;
                    }
                }
            }
            default -> {
                return false;
            }
        }

        return false;
    }

    private static boolean isClimbWall(Level world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        return !state.isAir()
                && !state.is(BlockTags.CLIMBABLE)
                && state.blocksMotion()
                && !state.getCollisionShape(world, pos).isEmpty();
    }

    private static boolean isTallFenceOrWall(Level world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (!state.is(FENCES_TAG) && !state.is(WALLS_TAG)) {
            return false;
        }

        VoxelShape collisionShape = state.getCollisionShape(world, pos);
        return !state.isAir()
                && !state.is(BlockTags.CLIMBABLE)
                && state.blocksMotion()
                && !collisionShape.isEmpty()
                && collisionShape.max(Direction.Axis.Y) > 1.0D + WALL_CONTACT_EPSILON;
    }

    static Vec3 resolveWallStrafeBasis(Direction wallDirection, float yawDegrees) {
        Vec3 wallNormal = getWallNormal(wallDirection);
        Vec3 wallTangent = new Vec3(-wallNormal.z, 0.0D, wallNormal.x);
        if (wallTangent.dot(getCameraRightVector(yawDegrees)) < 0.0D) {
            wallTangent = wallTangent.reverse();
        }

        return wallTangent;
    }

    static Vec3 getWallClimbVelocity(Direction wallDirection, Vec3 wallStrafeBasis, int verticalInput, int sidewaysInput) {
        Vec3 strafeVelocity = sidewaysInput == 0 || wallStrafeBasis == null
                ? Vec3.ZERO
                : wallStrafeBasis.scale(sidewaysInput * WALL_STRAFE_SPEED);
        return strafeVelocity
                .add(getWallNormal(wallDirection).scale(WALL_STICK_SPEED))
                .add(0.0D, verticalInput * WALL_CLIMB_SPEED, 0.0D);
    }

    private static void applyWallMovement(ServerPlayer player, Vec3 wallVelocity) {
        player.setDeltaMovement(wallVelocity);
        player.fallDistance = 0.0D;
        ((EntityInvoker) player).celestium$invokeScheduleVelocityUpdate();
    }

    private static void playWallClimbSound(ServerPlayer player, PlayerState state) {
        if (!isMovingOnWall(player)) {
            resetWallClimbSoundState(state);
            return;
        }

        BlockPos soundPos = resolveWallSoundPos(player, state.wallDirection);
        WallClimbSoundTransition transition = evaluateWallClimbSoundTransition(state.lastWallClimbSoundPos, soundPos);
        state.lastWallClimbSoundPos = transition.trackedSoundPos();
        if (!transition.shouldPlaySound() || soundPos == null) {
            return;
        }

        playWallStepSound(player, soundPos);
    }

    private static void playWallStepSound(ServerPlayer player, BlockPos soundPos) {
        SoundType soundGroup = player.level().getBlockState(soundPos).getSoundType();
        if (soundGroup == null || soundGroup.getVolume() <= 0.0F) {
            return;
        }

        player.level().playSound(
                player,
                soundPos.getX() + 0.5D,
                soundPos.getY() + 0.5D,
                soundPos.getZ() + 0.5D,
                soundGroup.getStepSound(),
                SoundSource.PLAYERS,
                Math.max(0.1F, soundGroup.getVolume() * WALL_CLIMB_SOUND_VOLUME_MULTIPLIER),
                soundGroup.getPitch()
        );
    }

    private static BlockPos resolveWallSoundPos(Player player, Direction wallDirection) {
        AABB playerBox = player.getBoundingBox();
        Level world = player.level();
        int feetY = Mth.floor(playerBox.minY + WALL_CONTACT_EPSILON);
        BlockPos soundPos = findWallSoundSurface(world, playerBox, feetY, wallDirection);
        if (soundPos != null) {
            return soundPos;
        }

        return player.onGround() ? null : findWallSoundSurface(world, playerBox, feetY - 1, wallDirection);
    }

    private static Vec3 getCameraRightVector(float yawDegrees) {
        float yawRadians = yawDegrees * (float) (Math.PI / 180.0);
        return new Vec3(-Math.cos(yawRadians), 0.0D, -Math.sin(yawRadians));
    }

    private static Vec3 getWallNormal(Direction wallDirection) {
        return new Vec3(wallDirection.getStepX(), 0.0D, wallDirection.getStepZ());
    }

    private static Vec3 getHorizontalClimbLookVector(Player player) {
        Vec3 look = player.getLookAngle();
        Vec3 horizontalLook = new Vec3(look.x, 0.0D, look.z);
        if (horizontalLook.lengthSqr() > 1.0E-6D) {
            return horizontalLook.normalize();
        }

        double yawRadians = Math.toRadians(player.getYRot());
        return new Vec3(-Math.sin(yawRadians), 0.0D, Math.cos(yawRadians));
    }

    private static BlockPos findWallSoundSurface(Level world, AABB playerBox, int y, Direction direction) {
        switch (direction) {
            case WEST -> {
                int x = Mth.floor(playerBox.minX - WALL_CONTACT_EPSILON);
                int minZ = Mth.floor(playerBox.minZ + WALL_CONTACT_EPSILON);
                int maxZ = Mth.floor(playerBox.maxZ - WALL_CONTACT_EPSILON);
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (isWallSoundSurface(world, pos)) {
                        return pos;
                    }
                }
            }
            case EAST -> {
                int x = Mth.floor(playerBox.maxX + WALL_CONTACT_EPSILON);
                int minZ = Mth.floor(playerBox.minZ + WALL_CONTACT_EPSILON);
                int maxZ = Mth.floor(playerBox.maxZ - WALL_CONTACT_EPSILON);
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (isWallSoundSurface(world, pos)) {
                        return pos;
                    }
                }
            }
            case NORTH -> {
                int z = Mth.floor(playerBox.minZ - WALL_CONTACT_EPSILON);
                int minX = Mth.floor(playerBox.minX + WALL_CONTACT_EPSILON);
                int maxX = Mth.floor(playerBox.maxX - WALL_CONTACT_EPSILON);
                for (int x = minX; x <= maxX; x++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (isWallSoundSurface(world, pos)) {
                        return pos;
                    }
                }
            }
            case SOUTH -> {
                int z = Mth.floor(playerBox.maxZ + WALL_CONTACT_EPSILON);
                int minX = Mth.floor(playerBox.minX + WALL_CONTACT_EPSILON);
                int maxX = Mth.floor(playerBox.maxX - WALL_CONTACT_EPSILON);
                for (int x = minX; x <= maxX; x++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (isWallSoundSurface(world, pos)) {
                        return pos;
                    }
                }
            }
            default -> {
                return null;
            }
        }

        return null;
    }

    private static boolean isWallSoundSurface(Level world, BlockPos pos) {
        return isClimbWall(world, pos) || isTallFenceOrWall(world, pos);
    }

    private static void resetPlayerState(ServerPlayer player) {
        PlayerState state = STATES.remove(player.getUUID());
        if (state != null) {
            syncWallClimbState(player, state, AuthoritativeWallClimbMotion.inactive());
        }
    }

    private static void syncWallClimbState(ServerPlayer player, PlayerState state, AuthoritativeWallClimbMotion motion) {
        Vec3 velocity = motion.active() ? motion.velocity() : Vec3.ZERO;
        if (!shouldSyncWallClimbState(state, motion.active(), motion.wallDirection(), velocity)) {
            return;
        }

        SyncCelestiumWallClimbStatePayload.send(player, motion.active(), motion.wallDirection(), velocity);
        state.lastSyncedWallClimbActive = motion.active();
        state.lastSyncedWallDirection = motion.wallDirection();
        state.lastSyncedWallVelocity = velocity;
    }

    private static boolean shouldSyncWallClimbState(PlayerState state, boolean active, Direction wallDirection, Vec3 velocity) {
        return state.lastSyncedWallClimbActive != active
                || state.lastSyncedWallDirection != wallDirection
                || !hasSameVelocity(state.lastSyncedWallVelocity, velocity);
    }

    private static boolean hasSameVelocity(Vec3 previousVelocity, Vec3 velocity) {
        return Math.abs(previousVelocity.x - velocity.x) <= 1.0E-7D
                && Math.abs(previousVelocity.y - velocity.y) <= 1.0E-7D
                && Math.abs(previousVelocity.z - velocity.z) <= 1.0E-7D;
    }

    private static void resetWallClimbSoundState(PlayerState state) {
        state.lastWallClimbSoundPos = null;
    }

    public record WallClimbSoundTransition(boolean shouldPlaySound, BlockPos trackedSoundPos) {
    }

    static record AuthoritativeWallClimbMotion(boolean active, Direction wallDirection, Vec3 wallStrafeBasis, Vec3 velocity) {
        private static AuthoritativeWallClimbMotion inactive() {
            return new AuthoritativeWallClimbMotion(false, null, null, Vec3.ZERO);
        }
    }

    private static final class PlayerState {
        private boolean sneakKeyHeld;
        private boolean forwardKeyHeld;
        private boolean backwardKeyHeld;
        private boolean leftKeyHeld;
        private boolean rightKeyHeld;
        private boolean wallClimbing;
        private Direction wallDirection;
        private Vec3 wallStrafeBasis;
        private BlockPos lastWallClimbSoundPos;
        private boolean lastSyncedWallClimbActive;
        private Direction lastSyncedWallDirection;
        private Vec3 lastSyncedWallVelocity = Vec3.ZERO;
    }
}
