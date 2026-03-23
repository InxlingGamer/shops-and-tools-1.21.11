package net.inklinggamer.shopsandtools.player;

import net.inklinggamer.shopsandtools.item.ModItems;
import net.inklinggamer.shopsandtools.mixin.EntityInvoker;
import net.inklinggamer.shopsandtools.network.SyncCelestiumThrustCooldownPayload;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.UpdatePlayerAbilitiesC2SPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class CelestiumLeggingsManager {
    private static final int DOLPHINS_GRACE_DURATION_TICKS = 40;
    private static final int DOLPHINS_GRACE_REFRESH_THRESHOLD_TICKS = 20;
    private static final int WITHER_DURATION_TICKS = 100;
    private static final int THRUST_COOLDOWN_TICKS = 60;
    private static final double THRUST_HORIZONTAL_STRENGTH = 1.2D;
    private static final double THRUST_VERTICAL_STRENGTH = 0.58D;
    private static final double STATIONARY_HORIZONTAL_SPEED_SQUARED_THRESHOLD = 1.0E-4D;

    private static final Map<UUID, PlayerState> STATES = new HashMap<>();

    private CelestiumLeggingsManager() {
    }

    public static void tickServer(MinecraftServer server) {
        STATES.entrySet().removeIf(entry -> server.getPlayerManager().getPlayer(entry.getKey()) == null);
    }

    public static void tickPlayer(ServerPlayerEntity player) {
        if (!player.isAlive()) {
            resetPlayerState(player);
            return;
        }

        if (!isCelestiumLeggingsEquipped(player)) {
            resetPlayerState(player);
            return;
        }

        applyDolphinsGrace(player);

        PlayerState state = STATES.computeIfAbsent(player.getUuid(), uuid -> new PlayerState());
        if (player.isOnGround()) {
            state.doubleJumpUsed = false;
        }

        syncFlightPermission(player, state);
    }

    public static boolean isCelestiumLeggingsEquipped(PlayerEntity player) {
        return player.getEquippedStack(EquipmentSlot.LEGS).isOf(ModItems.CELESTIUM_LEGGINGS);
    }

    public static boolean hasActiveFlightPermission(PlayerEntity player) {
        if (!isCelestiumLeggingsEquipped(player) || !player.getAbilities().allowFlying) {
            return false;
        }

        if (player instanceof ServerPlayerEntity serverPlayer) {
            PlayerState state = STATES.get(serverPlayer.getUuid());
            return state != null && state.flightPermissionActive;
        }

        return isDoubleJumpMovementEligible(player);
    }

    public static void onPlayerDamaged(LivingEntity victim, DamageSource source) {
        if (!(victim instanceof PlayerEntity player) || !isCelestiumLeggingsEquipped(player)) {
            return;
        }

        Entity attackerEntity = source.getAttacker();
        if (!(attackerEntity instanceof LivingEntity attacker) || attacker == victim) {
            return;
        }

        attacker.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, WITHER_DURATION_TICKS, 1));
    }

    public static boolean handleFlightToggle(ServerPlayerEntity player, UpdatePlayerAbilitiesC2SPacket packet) {
        if (!packet.isFlying() || !isCelestiumLeggingsEquipped(player)) {
            return false;
        }

        PlayerState state = STATES.get(player.getUuid());
        if (state == null || !state.flightPermissionActive) {
            return false;
        }

        ServerWorld world = player.getEntityWorld();
        long worldTime = world.getTime();
        cancelFlight(player);

        if (state.cooldownEndsAt > worldTime) {
            return true;
        }

        if (!isDoubleJumpMovementEligible(player)) {
            return true;
        }

        if (player.isOnGround()) {
            return true;
        }

        if (state.doubleJumpUsed) {
            return true;
        }

        state.doubleJumpUsed = true;
        state.cooldownEndsAt = worldTime + THRUST_COOLDOWN_TICKS;
        SyncCelestiumThrustCooldownPayload.send(player, THRUST_COOLDOWN_TICKS);
        launchPlayer(player, world);
        return true;
    }

    private static void applyDolphinsGrace(ServerPlayerEntity player) {
        StatusEffectInstance current = player.getStatusEffect(StatusEffects.DOLPHINS_GRACE);
        if (current == null || current.getAmplifier() != 0 || current.getDuration() <= DOLPHINS_GRACE_REFRESH_THRESHOLD_TICKS) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.DOLPHINS_GRACE, DOLPHINS_GRACE_DURATION_TICKS, 0, false, false, false));
        }
    }

    private static boolean isDoubleJumpMovementEligible(PlayerEntity player) {
        return !player.isCreative()
                && !player.isSpectator()
                && !player.hasVehicle()
                && !player.isSwimming()
                && !player.isTouchingWater()
                && !player.isClimbing()
                && !player.isGliding();
    }

    private static void syncFlightPermission(ServerPlayerEntity player, PlayerState state) {
        boolean shouldAllowFlight = isDoubleJumpMovementEligible(player);

        if (shouldAllowFlight && !state.flightPermissionActive) {
            player.getAbilities().allowFlying = true;
            state.flightPermissionActive = true;
            player.sendAbilitiesUpdate();
            return;
        }

        if (!shouldAllowFlight && state.flightPermissionActive) {
            clearFlightPermission(player, state);
        }
    }

    private static void clearFlightPermission(ServerPlayerEntity player, PlayerState state) {
        if (!state.flightPermissionActive) {
            return;
        }

        state.flightPermissionActive = false;
        player.getAbilities().allowFlying = false;
        cancelFlight(player);
    }

    private static void cancelFlight(ServerPlayerEntity player) {
        player.getAbilities().flying = false;
        player.sendAbilitiesUpdate();
    }

    private static void launchPlayer(ServerPlayerEntity player, ServerWorld world) {
        Vec3d launchVelocity = new Vec3d(0.0D, THRUST_VERTICAL_STRENGTH, 0.0D);
        Vec3d horizontalVelocity = new Vec3d(player.getVelocity().x, 0.0D, player.getVelocity().z);

        if (horizontalVelocity.lengthSquared() >= STATIONARY_HORIZONTAL_SPEED_SQUARED_THRESHOLD) {
            Vec3d look = player.getRotationVector();
            Vec3d horizontal = new Vec3d(look.x, 0.0D, look.z);

            if (horizontal.lengthSquared() < 1.0E-4D) {
                double yawRadians = Math.toRadians(player.getYaw());
                horizontal = new Vec3d(-Math.sin(yawRadians), 0.0D, Math.cos(yawRadians));
            }

            launchVelocity = horizontal.normalize().multiply(THRUST_HORIZONTAL_STRENGTH).add(0.0D, THRUST_VERTICAL_STRENGTH, 0.0D);
        }

        player.setVelocity(launchVelocity);
        ((EntityInvoker) player).shopsandtools$invokeScheduleVelocityUpdate();

        world.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.ITEM_FIRECHARGE_USE,
                SoundCategory.PLAYERS,
                1.0F,
                1.0F
        );
        world.spawnParticles(
                ParticleTypes.FLAME,
                player.getX(),
                player.getY() + 0.1D,
                player.getZ(),
                12,
                0.2D,
                0.05D,
                0.2D,
                0.02D
        );
    }

    private static void resetPlayerState(ServerPlayerEntity player) {
        PlayerState state = STATES.remove(player.getUuid());
        if (state != null) {
            SyncCelestiumThrustCooldownPayload.send(player, 0);
            clearFlightPermission(player, state);
        }
    }

    private static final class PlayerState {
        private boolean doubleJumpUsed;
        private boolean flightPermissionActive;
        private long cooldownEndsAt;
    }
}
