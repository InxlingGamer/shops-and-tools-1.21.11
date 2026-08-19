package net.inklinggamer.celestium.player;

import net.inklinggamer.celestium.item.ModItems;
import net.inklinggamer.celestium.mixin.EntityInvoker;
import net.inklinggamer.celestium.network.SyncCelestiumThrustCooldownPayload;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ServerboundPlayerAbilitiesPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
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
        STATES.entrySet().removeIf(entry -> server.getPlayerList().getPlayer(entry.getKey()) == null);
    }

    public static void tickPlayer(ServerPlayer player) {
        if (!player.isAlive()) {
            resetPlayerState(player);
            return;
        }

        if (!isCelestiumLeggingsEquipped(player)) {
            resetPlayerState(player);
            return;
        }

        applyDolphinsGrace(player);

        PlayerState state = STATES.computeIfAbsent(player.getUUID(), uuid -> new PlayerState());
        if (player.onGround()) {
            state.doubleJumpUsed = false;
        }

        syncFlightPermission(player, state);
    }

    public static boolean isCelestiumLeggingsEquipped(Player player) {
        return player.getItemBySlot(EquipmentSlot.LEGS).is(ModItems.CELESTIUM_LEGGINGS);
    }

    public static boolean hasActiveFlightPermission(Player player) {
        if (!isCelestiumLeggingsEquipped(player)
                || !player.getAbilities().mayfly
                || hasVanillaFlightPermission(player)) {
            return false;
        }

        if (player instanceof ServerPlayer serverPlayer) {
            PlayerState state = STATES.get(serverPlayer.getUUID());
            return state != null && state.flightPermissionActive;
        }

        return isDoubleJumpMovementEligible(player);
    }

    public static void onPlayerDamaged(LivingEntity victim, DamageSource source) {
        if (!(victim instanceof Player player) || !isCelestiumLeggingsEquipped(player)) {
            return;
        }

        Entity attackerEntity = source.getEntity();
        if (!(attackerEntity instanceof LivingEntity attacker) || attacker == victim) {
            return;
        }

        attacker.addEffect(new MobEffectInstance(MobEffects.WITHER, WITHER_DURATION_TICKS, 1));
    }

    public static boolean handleFlightToggle(ServerPlayer player, ServerboundPlayerAbilitiesPacket packet) {
        PlayerState state = STATES.get(player.getUUID());
        if (!shouldInterceptFlightToggle(
                packet.isFlying(),
                player.isCreative(),
                player.isSpectator(),
                isCelestiumLeggingsEquipped(player),
                state != null && state.flightPermissionActive
        )) {
            return false;
        }

        ServerLevel world = player.level();
        long worldTime = world.getGameTime();
        cancelFlight(player);

        if (state.cooldownEndsAt > worldTime) {
            return true;
        }

        if (!isDoubleJumpMovementEligible(player)) {
            return true;
        }

        if (player.onGround()) {
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

    private static void applyDolphinsGrace(ServerPlayer player) {
        MobEffectInstance current = player.getEffect(MobEffects.DOLPHINS_GRACE);
        if (current == null || current.getAmplifier() != 0 || current.getDuration() <= DOLPHINS_GRACE_REFRESH_THRESHOLD_TICKS) {
            player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, DOLPHINS_GRACE_DURATION_TICKS, 0, false, false, false));
        }
    }

    private static boolean isDoubleJumpMovementEligible(Player player) {
        return !player.isCreative()
                && !player.isSpectator()
                && !player.isPassenger()
                && !player.isSwimming()
                && !player.isInWater()
                && !player.onClimbable()
                && !player.isFallFlying();
    }

    private static void syncFlightPermission(ServerPlayer player, PlayerState state) {
        boolean vanillaFlightPermission = hasVanillaFlightPermission(player);
        if (shouldRestoreVanillaAllowFlying(vanillaFlightPermission, player.getAbilities().mayfly)) {
            player.getAbilities().mayfly = true;
            player.onUpdateAbilities();
        }

        FlightPermissionSync sync = resolveFlightPermission(
                state.flightPermissionActive,
                vanillaFlightPermission,
                shouldManageCelestiumFlight(vanillaFlightPermission, isDoubleJumpMovementEligible(player))
        );

        if (sync.shouldGrantPermission()) {
            player.getAbilities().mayfly = true;
            state.flightPermissionActive = true;
            player.onUpdateAbilities();
            return;
        }

        if (sync.shouldRevokePermission()) {
            clearFlightPermission(player, state, sync.shouldDisableFlightOnRevoke());
        }
    }

    private static void clearFlightPermission(ServerPlayer player, PlayerState state, boolean disableFlight) {
        if (!state.flightPermissionActive) {
            return;
        }

        state.flightPermissionActive = false;
        if (disableFlight) {
            player.getAbilities().mayfly = false;
            cancelFlight(player);
        }
    }

    private static void cancelFlight(ServerPlayer player) {
        player.getAbilities().flying = false;
        player.onUpdateAbilities();
    }

    private static void launchPlayer(ServerPlayer player, ServerLevel world) {
        Vec3 launchVelocity = new Vec3(0.0D, THRUST_VERTICAL_STRENGTH, 0.0D);
        Vec3 horizontalVelocity = new Vec3(player.getDeltaMovement().x, 0.0D, player.getDeltaMovement().z);

        if (horizontalVelocity.lengthSqr() >= STATIONARY_HORIZONTAL_SPEED_SQUARED_THRESHOLD) {
            Vec3 look = player.getLookAngle();
            Vec3 horizontal = new Vec3(look.x, 0.0D, look.z);

            if (horizontal.lengthSqr() < 1.0E-4D) {
                double yawRadians = Math.toRadians(player.getYRot());
                horizontal = new Vec3(-Math.sin(yawRadians), 0.0D, Math.cos(yawRadians));
            }

            launchVelocity = horizontal.normalize().scale(THRUST_HORIZONTAL_STRENGTH).add(0.0D, THRUST_VERTICAL_STRENGTH, 0.0D);
        }

        player.setDeltaMovement(launchVelocity);
        ((EntityInvoker) player).celestium$invokeScheduleVelocityUpdate();

        world.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.FIRECHARGE_USE,
                SoundSource.PLAYERS,
                1.0F,
                1.0F
        );
        world.sendParticles(
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

    private static void resetPlayerState(ServerPlayer player) {
        PlayerState state = STATES.remove(player.getUUID());
        if (state != null) {
            SyncCelestiumThrustCooldownPayload.send(player, 0);
            clearFlightPermission(player, state, shouldDisableFlightOnPermissionClear(hasVanillaFlightPermission(player)));
        }

        if (shouldRestoreVanillaAllowFlying(hasVanillaFlightPermission(player), player.getAbilities().mayfly)) {
            player.getAbilities().mayfly = true;
            player.onUpdateAbilities();
        }
    }

    static boolean hasVanillaFlightPermission(boolean creativeMode, boolean spectatorMode) {
        return creativeMode || spectatorMode;
    }

    static boolean shouldManageCelestiumFlight(boolean vanillaFlightPermission, boolean movementEligible) {
        return !vanillaFlightPermission && movementEligible;
    }

    static boolean shouldDisableFlightOnPermissionClear(boolean vanillaFlightPermission) {
        return !vanillaFlightPermission;
    }

    static boolean shouldRestoreVanillaAllowFlying(boolean vanillaFlightPermission, boolean allowFlying) {
        return vanillaFlightPermission && !allowFlying;
    }

    static boolean shouldInterceptFlightToggle(
            boolean packetFlying,
            boolean creativeMode,
            boolean spectatorMode,
            boolean leggingsEquipped,
            boolean flightPermissionActive
    ) {
        return packetFlying
                && leggingsEquipped
                && flightPermissionActive
                && !hasVanillaFlightPermission(creativeMode, spectatorMode);
    }

    static FlightPermissionSync resolveFlightPermission(
            boolean flightPermissionActive,
            boolean vanillaFlightPermission,
            boolean celestiumFlightEligible
    ) {
        boolean shouldGrantPermission = celestiumFlightEligible && !flightPermissionActive;
        boolean shouldRevokePermission = flightPermissionActive && !celestiumFlightEligible;
        return new FlightPermissionSync(
                shouldGrantPermission,
                shouldRevokePermission,
                shouldRevokePermission && shouldDisableFlightOnPermissionClear(vanillaFlightPermission)
        );
    }

    private static boolean hasVanillaFlightPermission(Player player) {
        return hasVanillaFlightPermission(player.isCreative(), player.isSpectator());
    }

    private static final class PlayerState {
        private boolean doubleJumpUsed;
        private boolean flightPermissionActive;
        private long cooldownEndsAt;
    }

    record FlightPermissionSync(boolean shouldGrantPermission, boolean shouldRevokePermission, boolean shouldDisableFlightOnRevoke) {
    }
}
