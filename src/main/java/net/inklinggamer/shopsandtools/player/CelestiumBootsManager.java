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
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class CelestiumBootsManager {
    private static final int LADDER_SOUND_INTERVAL_TICKS = 8;
    private static final double WALL_CLIMB_SPEED = 0.2D;
    private static final double WALL_CLIMB_DRAG = 0.15D;

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

        boolean climbing = shouldWallClimb(player);
        PlayerState state = STATES.computeIfAbsent(player.getUuid(), uuid -> new PlayerState());

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

    public static boolean shouldWallClimb(PlayerEntity player) {
        if (!isCelestiumBootsEquipped(player)
                || !player.isAlive()
                || player.isSpectator()
                || !player.isSprinting()
                || player.forwardSpeed <= 0.0F
                || !player.horizontalCollision
                || player.hasVehicle()
                || player.isSwimming()
                || player.isTouchingWater()
                || player.isSubmergedInWater()
                || player.isGliding()
                || player.getAbilities().flying) {
            return false;
        }

        Direction facing = player.getHorizontalFacing();
        BlockPos wallBase = player.getBlockPos().offset(facing);
        BlockPos wallAbove = wallBase.up();
        World world = player.getEntityWorld();

        return isClimbWall(world, wallBase) && isClimbWall(world, wallAbove);
    }

    public static boolean shouldMuffleMovementVibrations(Entity entity, RegistryEntry<GameEvent> event) {
        if (!(entity instanceof PlayerEntity player) || !isCelestiumBootsEquipped(player)) {
            return false;
        }

        return event == GameEvent.STEP || event == GameEvent.HIT_GROUND;
    }

    private static boolean isClimbWall(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (state.isAir() || state.isIn(BlockTags.CLIMBABLE) || !state.blocksMovement() || !state.isSolidBlock(world, pos)) {
            return false;
        }

        VoxelShape shape = state.getCollisionShape(world, pos);
        if (shape.isEmpty()) {
            return false;
        }

        Box box = shape.getBoundingBox();
        return box.getLengthX() >= 0.99D && box.getLengthY() >= 0.99D && box.getLengthZ() >= 0.99D;
    }

    private static void applyWallClimbVelocity(ServerPlayerEntity player) {
        Vec3d velocity = player.getVelocity();
        double climbVelocity = Math.max(velocity.y, WALL_CLIMB_SPEED);
        player.setVelocity(velocity.x * WALL_CLIMB_DRAG, climbVelocity, velocity.z * WALL_CLIMB_DRAG);
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
    }
}
