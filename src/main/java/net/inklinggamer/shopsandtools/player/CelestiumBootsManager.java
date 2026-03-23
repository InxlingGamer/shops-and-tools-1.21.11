package net.inklinggamer.shopsandtools.player;

import net.inklinggamer.shopsandtools.item.ModItems;
import net.inklinggamer.shopsandtools.mixin.EntityInvoker;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class CelestiumBootsManager {
    private static final int LADDER_SOUND_INTERVAL_TICKS = 4;
    private static final float WALL_CLIMB_SOUND_VOLUME_MULTIPLIER = 1.15F;
    private static final double WALL_CLIMB_SPEED = 0.2D;
    private static final double WALL_STRAFE_SPEED = 0.12D;
    private static final double WALL_STICK_SPEED = 0.08D;
    private static final double WALL_CONTACT_EPSILON = 1.0E-4D;
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
            state.lastWallClimbSoundTick = Long.MIN_VALUE;
            return;
        }

        applyWallMovement(player, wallDirection);
        playWallClimbSound(player, state);
    }

    public static boolean isCelestiumBootsEquipped(PlayerEntity player) {
        return player.getEquippedStack(EquipmentSlot.FEET).isOf(ModItems.CELESTIUM_BOOTS);
    }

    public static void setWallClimbInput(ServerPlayerEntity player, boolean sneakHeld, boolean forwardHeld, boolean leftHeld, boolean rightHeld) {
        PlayerState state = STATES.computeIfAbsent(player.getUuid(), uuid -> new PlayerState());
        state.sneakKeyHeld = sneakHeld;
        state.forwardKeyHeld = forwardHeld;
        state.leftKeyHeld = leftHeld;
        state.rightKeyHeld = rightHeld;

        if (!sneakHeld) {
            state.wallClimbing = false;
            state.wallDirection = null;
            state.lastWallClimbSoundTick = Long.MIN_VALUE;
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
            continuingWallClimb = isSneakKeyHeld(player);
        }

        return resolveWallClimbDirection(player, preferredDirection, continuingWallClimb) != null;
    }

    public static Direction resolveWallClimbDirection(PlayerEntity player, Direction preferredDirection, boolean continuingWallClimb) {
        return resolveWallDirection(player, preferredDirection, continuingWallClimb);
    }

    public static BlockPos resolveWallClimbSoundPos(PlayerEntity player, Direction wallDirection) {
        return wallDirection == null ? null : resolveWallSoundPos(player, wallDirection);
    }

    public static boolean hasWallClimbMovementInput(PlayerEntity player) {
        return isMovingOnWall(player);
    }

    public static boolean hasWallClimbMovementInput(boolean forwardHeld, boolean leftHeld, boolean rightHeld) {
        return forwardHeld || getSidewaysInput(leftHeld, rightHeld) != 0;
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

        if (preferredDirection != null) {
            if (hasClimbColumn(player, preferredDirection) || continuingWallClimb && hasWallContact(player, preferredDirection)) {
                return preferredDirection;
            }
        }

        for (Direction direction : HORIZONTAL_DIRECTIONS) {
            if (hasClimbColumn(player, direction)) {
                return direction;
            }
        }

        if (continuingWallClimb) {
            for (Direction direction : HORIZONTAL_DIRECTIONS) {
                if (hasWallContact(player, direction)) {
                    return direction;
                }
            }
        }

        return null;
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

    private static boolean isMovingOnWall(PlayerEntity player) {
        return isForwardKeyHeld(player) || getSidewaysInput(player) != 0;
    }

    private static boolean hasClimbColumn(PlayerEntity player, Direction direction) {
        World world = player.getEntityWorld();
        Box playerBox = player.getBoundingBox();
        int lowerY = MathHelper.floor(playerBox.minY + WALL_CONTACT_EPSILON);
        return hasWallSegment(world, playerBox, lowerY, direction) && hasWallSegment(world, playerBox, lowerY + 1, direction);
    }

    private static boolean hasWallContact(PlayerEntity player, Direction direction) {
        Box playerBox = player.getBoundingBox();
        int minY = MathHelper.floor(playerBox.minY + WALL_CONTACT_EPSILON);
        int maxY = MathHelper.floor(playerBox.maxY - WALL_CONTACT_EPSILON);
        World world = player.getEntityWorld();
        for (int y = minY; y <= maxY; y++) {
            if (hasWallSegment(world, playerBox, y, direction)) {
                return true;
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

    private static void applyWallMovement(ServerPlayerEntity player, Direction wallDirection) {
        Vec3d wallNormal = new Vec3d(wallDirection.getOffsetX(), 0.0D, wallDirection.getOffsetZ());
        double verticalSpeed = isForwardKeyHeld(player) ? WALL_CLIMB_SPEED : 0.0D;
        Vec3d strafeVelocity = getWallStrafeVelocity(player, wallNormal);
        Vec3d adjustedVelocity = strafeVelocity.add(wallNormal.multiply(WALL_STICK_SPEED)).add(0.0D, verticalSpeed, 0.0D);
        player.setVelocity(adjustedVelocity);
        player.fallDistance = 0.0D;
        ((EntityInvoker) player).shopsandtools$invokeScheduleVelocityUpdate();
    }

    private static void playWallClimbSound(ServerPlayerEntity player, PlayerState state) {
        if (!isMovingOnWall(player)) {
            state.lastWallClimbSoundTick = Long.MIN_VALUE;
            return;
        }

        long worldTime = player.getEntityWorld().getTime();
        if (worldTime - state.lastWallClimbSoundTick < LADDER_SOUND_INTERVAL_TICKS) {
            return;
        }

        playWallStepSound(player, state.wallDirection);
        state.lastWallClimbSoundTick = worldTime;
    }

    private static void playWallStepSound(ServerPlayerEntity player, Direction wallDirection) {
        BlockPos soundPos = wallDirection == null ? null : resolveWallSoundPos(player, wallDirection);
        SoundEvent sound = SoundEvents.BLOCK_LADDER_STEP;
        float volume = 1.0F;
        float pitch = 1.0F;
        double soundX = player.getX();
        double soundY = player.getY();
        double soundZ = player.getZ();

        if (soundPos != null) {
            BlockState soundState = player.getEntityWorld().getBlockState(soundPos);
            BlockSoundGroup soundGroup = soundState.getSoundGroup();
            if (soundGroup != null) {
                sound = soundGroup.getStepSound();
                volume = Math.max(0.1F, soundGroup.getVolume() * WALL_CLIMB_SOUND_VOLUME_MULTIPLIER);
                pitch = soundGroup.getPitch();
            }

            soundX = soundPos.getX() + 0.5D;
            soundY = soundPos.getY() + 0.5D;
            soundZ = soundPos.getZ() + 0.5D;
        }

        player.getEntityWorld().playSound(
                player,
                soundX,
                soundY,
                soundZ,
                sound,
                SoundCategory.PLAYERS,
                volume,
                pitch
        );
    }

    private static BlockPos resolveWallSoundPos(PlayerEntity player, Direction wallDirection) {
        Box playerBox = player.getBoundingBox();
        World world = player.getEntityWorld();
        int minY = MathHelper.floor(playerBox.minY + WALL_CONTACT_EPSILON);
        int maxY = MathHelper.floor(playerBox.maxY - WALL_CONTACT_EPSILON);
        for (int y = minY; y <= maxY; y++) {
            BlockPos soundPos = findWallSegment(world, playerBox, y, wallDirection);
            if (soundPos != null) {
                return soundPos;
            }
        }

        return null;
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

    private static BlockPos findWallSegment(World world, Box playerBox, int y, Direction direction) {
        switch (direction) {
            case WEST -> {
                int x = MathHelper.floor(playerBox.minX - WALL_CONTACT_EPSILON);
                int minZ = MathHelper.floor(playerBox.minZ + WALL_CONTACT_EPSILON);
                int maxZ = MathHelper.floor(playerBox.maxZ - WALL_CONTACT_EPSILON);
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (isClimbWall(world, pos)) {
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
                    if (isClimbWall(world, pos)) {
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
                    if (isClimbWall(world, pos)) {
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
                    if (isClimbWall(world, pos)) {
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

    private static final class PlayerState {
        private boolean sneakKeyHeld;
        private boolean forwardKeyHeld;
        private boolean leftKeyHeld;
        private boolean rightKeyHeld;
        private boolean wallClimbing;
        private Direction wallDirection;
        private long lastWallClimbSoundTick = Long.MIN_VALUE;
    }
}
