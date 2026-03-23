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
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class CelestiumBootsManager {
    private static final int LADDER_SOUND_INTERVAL_TICKS = 8;
    private static final double WALL_CLIMB_SPEED = 0.2D;
    private static final double WALL_CLIMB_DRAG = 0.15D;
    private static final double WALL_CONTACT_EPSILON = 1.0E-4D;

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
        boolean climbing = shouldWallClimb(player, state.wallClimbing);
        state.wallClimbing = climbing;

        if (!climbing) {
            state.lastLadderSoundTick = Long.MIN_VALUE;
            return;
        }

        applyWallClimbVelocity(player);
        playWallClimbSound(player, state);
    }

    public static boolean isCelestiumBootsEquipped(PlayerEntity player) {
        return player.getEquippedStack(EquipmentSlot.FEET).isOf(ModItems.CELESTIUM_BOOTS);
    }

    public static void setSprintKeyHeld(ServerPlayerEntity player, boolean held) {
        PlayerState state = STATES.computeIfAbsent(player.getUuid(), uuid -> new PlayerState());
        state.sprintKeyHeld = held;

        if (!held) {
            state.lastLadderSoundTick = Long.MIN_VALUE;
            state.wallClimbing = false;
        }
    }

    public static boolean shouldWallClimb(PlayerEntity player) {
        boolean continuingWallClimb = false;
        if (player instanceof ServerPlayerEntity serverPlayer) {
            PlayerState state = STATES.get(serverPlayer.getUuid());
            continuingWallClimb = state != null && state.wallClimbing;
        } else if (player.getVelocity().y > 0.0D) {
            continuingWallClimb = true;
        }

        return shouldWallClimb(player, continuingWallClimb);
    }

    private static boolean shouldWallClimb(PlayerEntity player, boolean continuingWallClimb) {
        if (!isCelestiumBootsEquipped(player)
                || !player.isAlive()
                || player.isSpectator()
                || !isSprintKeyHeld(player)
                || player.hasVehicle()
                || player.isSwimming()
                || player.isTouchingWater()
                || player.isSubmergedInWater()
                || player.isGliding()
                || player.getAbilities().flying) {
            return false;
        }

        if (hasAdjacentClimbColumn(player)) {
            return true;
        }

        return continuingWallClimb && hasAdjacentWallContact(player);
    }

    public static boolean shouldMuffleMovementVibrations(Entity entity, RegistryEntry<GameEvent> event) {
        if (!(entity instanceof PlayerEntity player) || !isCelestiumBootsEquipped(player)) {
            return false;
        }

        return event == GameEvent.STEP || event == GameEvent.HIT_GROUND;
    }

    private static boolean isSprintKeyHeld(PlayerEntity player) {
        if (player instanceof ServerPlayerEntity serverPlayer) {
            PlayerState state = STATES.get(serverPlayer.getUuid());
            return state != null && state.sprintKeyHeld;
        }

        return player.isSprinting();
    }

    private static boolean hasAdjacentClimbColumn(PlayerEntity player) {
        World world = player.getEntityWorld();
        Box playerBox = player.getBoundingBox();
        VoxelShape playerShape = VoxelShapes.cuboid(playerBox.expand(WALL_CONTACT_EPSILON));
        int lowerY = MathHelper.floor(playerBox.minY + WALL_CONTACT_EPSILON);
        Set<BlockPos> wallBases = collectAdjacentWallBases(playerBox, lowerY);

        for (BlockPos wallBase : wallBases) {
            if (isClimbWall(world, wallBase, playerShape) && isClimbWall(world, wallBase.up(), playerShape)) {
                return true;
            }
        }

        return false;
    }

    private static boolean hasAdjacentWallContact(PlayerEntity player) {
        World world = player.getEntityWorld();
        Box playerBox = player.getBoundingBox();
        VoxelShape playerShape = VoxelShapes.cuboid(playerBox.expand(WALL_CONTACT_EPSILON));
        int minY = MathHelper.floor(playerBox.minY + WALL_CONTACT_EPSILON);
        int maxY = MathHelper.floor(playerBox.maxY - WALL_CONTACT_EPSILON);

        for (int y = minY; y <= maxY; y++) {
            for (BlockPos wallPos : collectAdjacentWallBases(playerBox, y)) {
                if (isClimbWall(world, wallPos, playerShape)) {
                    return true;
                }
            }
        }

        return false;
    }

    private static Set<BlockPos> collectAdjacentWallBases(Box playerBox, int y) {
        Set<BlockPos> positions = new LinkedHashSet<>();
        int minX = MathHelper.floor(playerBox.minX + WALL_CONTACT_EPSILON);
        int maxX = MathHelper.floor(playerBox.maxX - WALL_CONTACT_EPSILON);
        int minZ = MathHelper.floor(playerBox.minZ + WALL_CONTACT_EPSILON);
        int maxZ = MathHelper.floor(playerBox.maxZ - WALL_CONTACT_EPSILON);
        int westX = MathHelper.floor(playerBox.minX - WALL_CONTACT_EPSILON);
        int eastX = MathHelper.floor(playerBox.maxX + WALL_CONTACT_EPSILON);
        int northZ = MathHelper.floor(playerBox.minZ - WALL_CONTACT_EPSILON);
        int southZ = MathHelper.floor(playerBox.maxZ + WALL_CONTACT_EPSILON);

        for (int z = minZ; z <= maxZ; z++) {
            positions.add(new BlockPos(westX, y, z));
            positions.add(new BlockPos(eastX, y, z));
        }

        for (int x = minX; x <= maxX; x++) {
            positions.add(new BlockPos(x, y, northZ));
            positions.add(new BlockPos(x, y, southZ));
        }

        return positions;
    }

    private static boolean isClimbWall(World world, BlockPos pos, VoxelShape playerShape) {
        BlockState state = world.getBlockState(pos);
        if (state.isAir() || state.isIn(BlockTags.CLIMBABLE) || !state.blocksMovement()) {
            return false;
        }

        VoxelShape shape = state.getCollisionShape(world, pos);
        if (shape.isEmpty()) {
            return false;
        }

        VoxelShape shiftedShape = shape.offset(pos.getX(), pos.getY(), pos.getZ());
        return VoxelShapes.matchesAnywhere(playerShape, shiftedShape, BooleanBiFunction.AND);
    }

    private static void applyWallClimbVelocity(ServerPlayerEntity player) {
        Vec3d velocity = player.getVelocity();
        double climbVelocity = Math.max(velocity.y, WALL_CLIMB_SPEED);
        Vec3d adjustedVelocity = new Vec3d(velocity.x * WALL_CLIMB_DRAG, climbVelocity, velocity.z * WALL_CLIMB_DRAG);
        player.setVelocity(adjustedVelocity);
        player.fallDistance = 0.0D;
        ((EntityInvoker) player).shopsandtools$invokeScheduleVelocityUpdate();
    }

    private static void playWallClimbSound(ServerPlayerEntity player, PlayerState state) {
        long worldTime = player.getEntityWorld().getTime();
        if (worldTime - state.lastLadderSoundTick < LADDER_SOUND_INTERVAL_TICKS) {
            return;
        }

        player.getEntityWorld().playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.BLOCK_LADDER_STEP,
                SoundCategory.PLAYERS,
                0.35F,
                1.0F
        );
        state.lastLadderSoundTick = worldTime;
    }

    private static final class PlayerState {
        private long lastLadderSoundTick = Long.MIN_VALUE;
        private boolean sprintKeyHeld;
        private boolean wallClimbing;
    }
}
