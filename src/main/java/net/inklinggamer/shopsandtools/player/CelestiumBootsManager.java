package net.inklinggamer.shopsandtools.player;

import net.inklinggamer.shopsandtools.item.ModItems;
import net.inklinggamer.shopsandtools.mixin.EntityInvoker;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class CelestiumBootsManager {
    private static final float WALL_CLIMB_SOUND_VOLUME_MULTIPLIER = 1.15F;
    private static final double WALL_CLIMB_SPEED = 0.2D;
    private static final double WALL_STRAFE_SPEED = 0.12D;
    private static final double WALL_STICK_SPEED = 0.08D;
    private static final double WALL_CONTACT_EPSILON = 1.0E-4D;
    private static final TagKey<Block> FENCES_TAG = TagKey.of(RegistryKeys.BLOCK, Identifier.of("minecraft", "fences"));
    private static final TagKey<Block> WALLS_TAG = TagKey.of(RegistryKeys.BLOCK, Identifier.of("minecraft", "walls"));
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
        STATES.entrySet().removeIf(entry -> server.getPlayerManager().getPlayer(entry.getKey()) == null);
    }

    public static void tickPlayer(ServerPlayerEntity player) {
        if (!isCelestiumBootsEquipped(player)) {
            STATES.remove(player.getUuid());
            return;
        }

        PlayerState state = STATES.computeIfAbsent(player.getUuid(), uuid -> new PlayerState());
        Direction wallDirection = resolveWallDirection(player, state.wallDirection, state.wallClimbing);
        boolean attached = wallDirection != null;
        state.wallClimbing = attached;
        state.wallDirection = wallDirection;

        if (!attached) {
            resetWallClimbSoundState(state);
            return;
        }

        applyWallMovement(player, wallDirection);
        playWallClimbSound(player, state);
    }

    public static boolean isCelestiumBootsEquipped(PlayerEntity player) {
        return player.getEquippedStack(EquipmentSlot.FEET).isOf(ModItems.CELESTIUM_BOOTS);
    }

    public static void setWallClimbInput(ServerPlayerEntity player, boolean sneakHeld, boolean forwardHeld, boolean backwardHeld, boolean leftHeld, boolean rightHeld) {
        PlayerState state = STATES.computeIfAbsent(player.getUuid(), uuid -> new PlayerState());
        state.sneakKeyHeld = sneakHeld;
        state.forwardKeyHeld = forwardHeld;
        state.backwardKeyHeld = backwardHeld;
        state.leftKeyHeld = leftHeld;
        state.rightKeyHeld = rightHeld;

        if (!sneakHeld) {
            state.wallClimbing = false;
            state.wallDirection = null;
            resetWallClimbSoundState(state);
        }
    }

    public static boolean shouldWallClimb(PlayerEntity player) {
        Direction preferredDirection = null;
        boolean continuingWallClimb = false;
        if (player instanceof ServerPlayerEntity serverPlayer) {
            PlayerState state = STATES.get(serverPlayer.getUuid());
            if (state != null) {
                continuingWallClimb = state.wallClimbing;
                preferredDirection = state.wallDirection;
            }
        } else {
            continuingWallClimb = isSneakKeyHeld(player) && !player.isOnGround();
        }

        return resolveWallClimbDirection(player, preferredDirection, continuingWallClimb) != null;
    }

    public static Direction resolveWallClimbDirection(PlayerEntity player, Direction preferredDirection, boolean continuingWallClimb) {
        return resolveWallDirection(player, preferredDirection, continuingWallClimb);
    }

    public static BlockPos resolveWallClimbSoundPos(PlayerEntity player, Direction wallDirection) {
        return wallDirection == null ? null : resolveWallSoundPos(player, wallDirection);
    }

    public static Vec3d getWallClimbVelocity(PlayerEntity player, Direction wallDirection) {
        Vec3d wallNormal = new Vec3d(wallDirection.getOffsetX(), 0.0D, wallDirection.getOffsetZ());
        double verticalSpeed = getVerticalWallInput(player) * WALL_CLIMB_SPEED;
        Vec3d strafeVelocity = getWallStrafeVelocity(player, wallNormal);
        return strafeVelocity.add(wallNormal.multiply(WALL_STICK_SPEED)).add(0.0D, verticalSpeed, 0.0D);
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

    public static boolean hasWallClimbMovementInput(PlayerEntity player) {
        return isMovingOnWall(player);
    }

    public static boolean hasWallClimbMovementInput(boolean forwardHeld, boolean backwardHeld, boolean leftHeld, boolean rightHeld) {
        return getVerticalWallInput(forwardHeld, backwardHeld) != 0 || getSidewaysInput(leftHeld, rightHeld) != 0;
    }

    private static Direction resolveWallDirection(PlayerEntity player, Direction preferredDirection, boolean continuingWallClimb) {
        if (!isCelestiumBootsEquipped(player)
                || !player.isAlive()
                || player.isSpectator()
                || !isSneakKeyHeld(player)
                || player.hasVehicle()
                || player.isSwimming()
                || player.isTouchingWater()
                || player.isSubmergedInWater()
                || player.isGliding()
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

    public static boolean shouldMuffleMovementVibrations(Entity entity, RegistryEntry<GameEvent> event) {
        if (!(entity instanceof PlayerEntity player) || !isCelestiumBootsEquipped(player)) {
            return false;
        }

        return event == GameEvent.STEP || event == GameEvent.HIT_GROUND;
    }

    private static boolean isSneakKeyHeld(PlayerEntity player) {
        if (player instanceof ServerPlayerEntity serverPlayer) {
            PlayerState state = STATES.get(serverPlayer.getUuid());
            return state != null && state.sneakKeyHeld;
        }

        return player.isSneaking();
    }

    private static boolean isForwardKeyHeld(PlayerEntity player) {
        if (player instanceof ServerPlayerEntity serverPlayer) {
            PlayerState state = STATES.get(serverPlayer.getUuid());
            return state != null && state.forwardKeyHeld;
        }

        return player.forwardSpeed > 0.0F;
    }

    private static boolean isBackwardKeyHeld(PlayerEntity player) {
        if (player instanceof ServerPlayerEntity serverPlayer) {
            PlayerState state = STATES.get(serverPlayer.getUuid());
            return state != null && state.backwardKeyHeld;
        }

        return player.forwardSpeed < 0.0F;
    }

    private static int getSidewaysInput(PlayerEntity player) {
        if (player instanceof ServerPlayerEntity serverPlayer) {
            PlayerState state = STATES.get(serverPlayer.getUuid());
            if (state == null) {
                return 0;
            }

            return getSidewaysInput(state.leftKeyHeld, state.rightKeyHeld);
        }

        return Math.round(player.sidewaysSpeed);
    }

    private static int getSidewaysInput(boolean leftHeld, boolean rightHeld) {
        return (rightHeld ? 1 : 0) - (leftHeld ? 1 : 0);
    }

    private static int getVerticalWallInput(PlayerEntity player) {
        return getVerticalWallInput(isForwardKeyHeld(player), isBackwardKeyHeld(player));
    }

    private static int getVerticalWallInput(boolean forwardHeld, boolean backwardHeld) {
        return (forwardHeld ? 1 : 0) - (backwardHeld ? 1 : 0);
    }

    private static boolean isMovingOnWall(PlayerEntity player) {
        return getVerticalWallInput(player) != 0 || getSidewaysInput(player) != 0;
    }

    private static Direction findBestWallDirection(PlayerEntity player, boolean continuingWallClimb) {
        Direction bestDirection = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        Vec3d horizontalLook = getHorizontalClimbLookVector(player);

        for (Direction direction : HORIZONTAL_DIRECTIONS) {
            if (!hasValidClimbSurface(player, direction, continuingWallClimb)) {
                continue;
            }

            Vec3d wallNormal = new Vec3d(direction.getOffsetX(), 0.0D, direction.getOffsetZ());
            double score = horizontalLook.dotProduct(wallNormal);
            if (score > bestScore) {
                bestScore = score;
                bestDirection = direction;
            }
        }

        return bestDirection;
    }

    private static boolean hasValidClimbSurface(PlayerEntity player, Direction direction, boolean continuingWallClimb) {
        return hasClimbColumn(player, direction)
                || hasTallFenceOrWall(player, direction)
                || continuingWallClimb && !player.isOnGround() && (
                        hasClimbColumnBelowFeet(player, direction)
                        || hasTallFenceOrWallBelowFeet(player, direction)
                );
    }

    private static boolean hasClimbColumn(PlayerEntity player, Direction direction) {
        World world = player.getEntityWorld();
        Box playerBox = player.getBoundingBox();
        int lowerY = MathHelper.floor(playerBox.minY + WALL_CONTACT_EPSILON);
        return hasClimbColumn(world, playerBox, lowerY, direction);
    }

    private static boolean hasClimbColumnBelowFeet(PlayerEntity player, Direction direction) {
        World world = player.getEntityWorld();
        Box playerBox = player.getBoundingBox();
        int lowerY = MathHelper.floor(playerBox.minY + WALL_CONTACT_EPSILON) - 1;
        return hasClimbColumn(world, playerBox, lowerY, direction);
    }

    private static boolean hasTallFenceOrWall(PlayerEntity player, Direction direction) {
        World world = player.getEntityWorld();
        Box playerBox = player.getBoundingBox();
        int lowerY = MathHelper.floor(playerBox.minY + WALL_CONTACT_EPSILON);
        return hasTallFenceOrWall(world, playerBox, lowerY, direction);
    }

    private static boolean hasTallFenceOrWallBelowFeet(PlayerEntity player, Direction direction) {
        World world = player.getEntityWorld();
        Box playerBox = player.getBoundingBox();
        int lowerY = MathHelper.floor(playerBox.minY + WALL_CONTACT_EPSILON) - 1;
        return hasTallFenceOrWall(world, playerBox, lowerY, direction);
    }

    private static boolean hasClimbColumn(World world, Box playerBox, int lowerY, Direction direction) {
        return hasWallSegment(world, playerBox, lowerY, direction) && hasWallSegment(world, playerBox, lowerY + 1, direction);
    }

    private static boolean hasTallFenceOrWall(World world, Box playerBox, int y, Direction direction) {
        switch (direction) {
            case WEST -> {
                int x = MathHelper.floor(playerBox.minX - WALL_CONTACT_EPSILON);
                int minZ = MathHelper.floor(playerBox.minZ + WALL_CONTACT_EPSILON);
                int maxZ = MathHelper.floor(playerBox.maxZ - WALL_CONTACT_EPSILON);
                for (int z = minZ; z <= maxZ; z++) {
                    if (isTallFenceOrWall(world, new BlockPos(x, y, z))) {
                        return true;
                    }
                }
            }
            case EAST -> {
                int x = MathHelper.floor(playerBox.maxX + WALL_CONTACT_EPSILON);
                int minZ = MathHelper.floor(playerBox.minZ + WALL_CONTACT_EPSILON);
                int maxZ = MathHelper.floor(playerBox.maxZ - WALL_CONTACT_EPSILON);
                for (int z = minZ; z <= maxZ; z++) {
                    if (isTallFenceOrWall(world, new BlockPos(x, y, z))) {
                        return true;
                    }
                }
            }
            case NORTH -> {
                int z = MathHelper.floor(playerBox.minZ - WALL_CONTACT_EPSILON);
                int minX = MathHelper.floor(playerBox.minX + WALL_CONTACT_EPSILON);
                int maxX = MathHelper.floor(playerBox.maxX - WALL_CONTACT_EPSILON);
                for (int x = minX; x <= maxX; x++) {
                    if (isTallFenceOrWall(world, new BlockPos(x, y, z))) {
                        return true;
                    }
                }
            }
            case SOUTH -> {
                int z = MathHelper.floor(playerBox.maxZ + WALL_CONTACT_EPSILON);
                int minX = MathHelper.floor(playerBox.minX + WALL_CONTACT_EPSILON);
                int maxX = MathHelper.floor(playerBox.maxX - WALL_CONTACT_EPSILON);
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

    private static boolean hasWallSegment(World world, Box playerBox, int y, Direction direction) {
        switch (direction) {
            case WEST -> {
                int x = MathHelper.floor(playerBox.minX - WALL_CONTACT_EPSILON);
                int minZ = MathHelper.floor(playerBox.minZ + WALL_CONTACT_EPSILON);
                int maxZ = MathHelper.floor(playerBox.maxZ - WALL_CONTACT_EPSILON);
                for (int z = minZ; z <= maxZ; z++) {
                    if (isClimbWall(world, new BlockPos(x, y, z))) {
                        return true;
                    }
                }
            }
            case EAST -> {
                int x = MathHelper.floor(playerBox.maxX + WALL_CONTACT_EPSILON);
                int minZ = MathHelper.floor(playerBox.minZ + WALL_CONTACT_EPSILON);
                int maxZ = MathHelper.floor(playerBox.maxZ - WALL_CONTACT_EPSILON);
                for (int z = minZ; z <= maxZ; z++) {
                    if (isClimbWall(world, new BlockPos(x, y, z))) {
                        return true;
                    }
                }
            }
            case NORTH -> {
                int z = MathHelper.floor(playerBox.minZ - WALL_CONTACT_EPSILON);
                int minX = MathHelper.floor(playerBox.minX + WALL_CONTACT_EPSILON);
                int maxX = MathHelper.floor(playerBox.maxX - WALL_CONTACT_EPSILON);
                for (int x = minX; x <= maxX; x++) {
                    if (isClimbWall(world, new BlockPos(x, y, z))) {
                        return true;
                    }
                }
            }
            case SOUTH -> {
                int z = MathHelper.floor(playerBox.maxZ + WALL_CONTACT_EPSILON);
                int minX = MathHelper.floor(playerBox.minX + WALL_CONTACT_EPSILON);
                int maxX = MathHelper.floor(playerBox.maxX - WALL_CONTACT_EPSILON);
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

    private static boolean isClimbWall(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        return !state.isAir()
                && !state.isIn(BlockTags.CLIMBABLE)
                && state.blocksMovement()
                && !state.getCollisionShape(world, pos).isEmpty();
    }

    private static boolean isTallFenceOrWall(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (!state.isIn(FENCES_TAG) && !state.isIn(WALLS_TAG)) {
            return false;
        }

        VoxelShape collisionShape = state.getCollisionShape(world, pos);
        return !state.isAir()
                && !state.isIn(BlockTags.CLIMBABLE)
                && state.blocksMovement()
                && !collisionShape.isEmpty()
                && collisionShape.getMax(Direction.Axis.Y) > 1.0D + WALL_CONTACT_EPSILON;
    }

    private static void applyWallMovement(ServerPlayerEntity player, Direction wallDirection) {
        player.setVelocity(getWallClimbVelocity(player, wallDirection));
        player.fallDistance = 0.0D;
        ((EntityInvoker) player).shopsandtools$invokeScheduleVelocityUpdate();
    }

    private static void playWallClimbSound(ServerPlayerEntity player, PlayerState state) {
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

    private static void playWallStepSound(ServerPlayerEntity player, BlockPos soundPos) {
        BlockSoundGroup soundGroup = player.getEntityWorld().getBlockState(soundPos).getSoundGroup();
        if (soundGroup == null || soundGroup.getVolume() <= 0.0F) {
            return;
        }

        player.getEntityWorld().playSound(
                player,
                soundPos.getX() + 0.5D,
                soundPos.getY() + 0.5D,
                soundPos.getZ() + 0.5D,
                soundGroup.getStepSound(),
                SoundCategory.PLAYERS,
                Math.max(0.1F, soundGroup.getVolume() * WALL_CLIMB_SOUND_VOLUME_MULTIPLIER),
                soundGroup.getPitch()
        );
    }

    private static BlockPos resolveWallSoundPos(PlayerEntity player, Direction wallDirection) {
        Box playerBox = player.getBoundingBox();
        World world = player.getEntityWorld();
        int feetY = MathHelper.floor(playerBox.minY + WALL_CONTACT_EPSILON);
        BlockPos soundPos = findWallSoundSurface(world, playerBox, feetY, wallDirection);
        if (soundPos != null) {
            return soundPos;
        }

        return player.isOnGround() ? null : findWallSoundSurface(world, playerBox, feetY - 1, wallDirection);
    }

    private static Vec3d getWallStrafeVelocity(PlayerEntity player, Vec3d wallNormal) {
        int sidewaysInput = getSidewaysInput(player);
        if (sidewaysInput == 0) {
            return Vec3d.ZERO;
        }

        Vec3d wallTangent = new Vec3d(-wallNormal.z, 0.0D, wallNormal.x);
        Vec3d cameraRight = getCameraRightVector(player);
        if (wallTangent.dotProduct(cameraRight) < 0.0D) {
            wallTangent = wallTangent.negate();
        }

        return wallTangent.multiply(sidewaysInput * WALL_STRAFE_SPEED);
    }

    private static Vec3d getCameraRightVector(PlayerEntity player) {
        float yawRadians = player.getYaw() * (float) (Math.PI / 180.0);
        return new Vec3d(-Math.cos(yawRadians), 0.0D, -Math.sin(yawRadians));
    }

    private static Vec3d getHorizontalClimbLookVector(PlayerEntity player) {
        Vec3d look = player.getRotationVector();
        Vec3d horizontalLook = new Vec3d(look.x, 0.0D, look.z);
        if (horizontalLook.lengthSquared() > 1.0E-6D) {
            return horizontalLook.normalize();
        }

        double yawRadians = Math.toRadians(player.getYaw());
        return new Vec3d(-Math.sin(yawRadians), 0.0D, Math.cos(yawRadians));
    }

    private static BlockPos findWallSoundSurface(World world, Box playerBox, int y, Direction direction) {
        switch (direction) {
            case WEST -> {
                int x = MathHelper.floor(playerBox.minX - WALL_CONTACT_EPSILON);
                int minZ = MathHelper.floor(playerBox.minZ + WALL_CONTACT_EPSILON);
                int maxZ = MathHelper.floor(playerBox.maxZ - WALL_CONTACT_EPSILON);
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (isWallSoundSurface(world, pos)) {
                        return pos;
                    }
                }
            }
            case EAST -> {
                int x = MathHelper.floor(playerBox.maxX + WALL_CONTACT_EPSILON);
                int minZ = MathHelper.floor(playerBox.minZ + WALL_CONTACT_EPSILON);
                int maxZ = MathHelper.floor(playerBox.maxZ - WALL_CONTACT_EPSILON);
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (isWallSoundSurface(world, pos)) {
                        return pos;
                    }
                }
            }
            case NORTH -> {
                int z = MathHelper.floor(playerBox.minZ - WALL_CONTACT_EPSILON);
                int minX = MathHelper.floor(playerBox.minX + WALL_CONTACT_EPSILON);
                int maxX = MathHelper.floor(playerBox.maxX - WALL_CONTACT_EPSILON);
                for (int x = minX; x <= maxX; x++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (isWallSoundSurface(world, pos)) {
                        return pos;
                    }
                }
            }
            case SOUTH -> {
                int z = MathHelper.floor(playerBox.maxZ + WALL_CONTACT_EPSILON);
                int minX = MathHelper.floor(playerBox.minX + WALL_CONTACT_EPSILON);
                int maxX = MathHelper.floor(playerBox.maxX - WALL_CONTACT_EPSILON);
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

    private static boolean isWallSoundSurface(World world, BlockPos pos) {
        return isClimbWall(world, pos) || isTallFenceOrWall(world, pos);
    }

    private static void resetWallClimbSoundState(PlayerState state) {
        state.lastWallClimbSoundPos = null;
    }

    public record WallClimbSoundTransition(boolean shouldPlaySound, BlockPos trackedSoundPos) {
    }

    private static final class PlayerState {
        private boolean sneakKeyHeld;
        private boolean forwardKeyHeld;
        private boolean backwardKeyHeld;
        private boolean leftKeyHeld;
        private boolean rightKeyHeld;
        private boolean wallClimbing;
        private Direction wallDirection;
        private BlockPos lastWallClimbSoundPos;
    }
}
