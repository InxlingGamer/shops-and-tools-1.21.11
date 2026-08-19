package net.inklinggamer.celestium.player;

import net.inklinggamer.celestium.item.CelestiumSpearHelper;
import net.inklinggamer.celestium.network.SyncCelestiumSpearStunCooldownPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public final class CelestiumSpearManager {
    private static final int SPEED_DURATION_TICKS = 40;
    private static final int SPEED_REFRESH_THRESHOLD_TICKS = 20;
    private static final int WITHER_DURATION_TICKS = 100;
    private static final int WITHER_AMPLIFIER = 1;
    private static final int STUN_DURATION_TICKS = 100;
    private static final int STUN_COOLDOWN_TICKS = 160;

    private static final Map<UUID, Integer> STUNNED_MOBS = new HashMap<>();
    private static final Map<UUID, PlayerState> STATES = new HashMap<>();

    private CelestiumSpearManager() {
    }

    public static void tickServer(MinecraftServer server) {
        int serverTicks = server.getTickCount();
        Iterator<Map.Entry<UUID, Integer>> iterator = STUNNED_MOBS.entrySet().iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getValue() <= serverTicks) {
                iterator.remove();
            }
        }

        STATES.entrySet().removeIf(entry -> server.getPlayerList().getPlayer(entry.getKey()) == null);
    }

    public static void tickPlayer(ServerPlayer player) {
        if (!player.isAlive()) {
            resetPlayerState(player);
            return;
        }

        expireStunCooldown(player, player.level().getGameTime());
        if (!CelestiumSpearHelper.isCelestiumSpearHeld(player)) {
            return;
        }

        MobEffectInstance currentSpeed = player.getEffect(MobEffects.SPEED);
        if (currentSpeed == null || currentSpeed.getAmplifier() != 0 || currentSpeed.getDuration() <= SPEED_REFRESH_THRESHOLD_TICKS) {
            player.addEffect(new MobEffectInstance(MobEffects.SPEED, SPEED_DURATION_TICKS, 0, false, false, false));
        }
    }

    public static void onDirectSpearDamage(ServerPlayer attacker, LivingEntity target, float damageDealt) {
        if (!CelestiumSpearHelper.isCelestiumSpearEquipped(attacker) || damageDealt <= 0.0F) {
            return;
        }

        target.addEffect(new MobEffectInstance(MobEffects.WITHER, WITHER_DURATION_TICKS, WITHER_AMPLIFIER, false, true, true));

        if (!(target instanceof Mob mob)) {
            return;
        }

        long worldTime = attacker.level().getGameTime();
        if (hasActiveStunCooldown(attacker, worldTime)) {
            return;
        }

        STUNNED_MOBS.put(mob.getUUID(), attacker.level().getServer().getTickCount() + STUN_DURATION_TICKS);
        startStunCooldown(attacker, worldTime);
    }

    public static boolean isStunned(Mob mob) {
        Integer expiresAt = STUNNED_MOBS.get(mob.getUUID());
        if (expiresAt == null) {
            return false;
        }

        int currentTicks = mob.level() instanceof net.minecraft.server.level.ServerLevel serverWorld
                ? serverWorld.getServer().getTickCount()
                : Integer.MAX_VALUE;
        if (expiresAt <= currentTicks) {
            STUNNED_MOBS.remove(mob.getUUID());
            return false;
        }

        return true;
    }

    private static boolean hasActiveStunCooldown(ServerPlayer player, long worldTime) {
        PlayerState state = STATES.get(player.getUUID());
        return state != null && state.cooldownEndsAt > worldTime;
    }

    private static void startStunCooldown(ServerPlayer player, long worldTime) {
        PlayerState state = STATES.computeIfAbsent(player.getUUID(), uuid -> new PlayerState());
        state.cooldownEndsAt = worldTime + STUN_COOLDOWN_TICKS;
        SyncCelestiumSpearStunCooldownPayload.send(player, STUN_COOLDOWN_TICKS);
    }

    private static void expireStunCooldown(ServerPlayer player, long worldTime) {
        PlayerState state = STATES.get(player.getUUID());
        if (state != null && state.cooldownEndsAt <= worldTime) {
            STATES.remove(player.getUUID());
        }
    }

    private static void resetPlayerState(ServerPlayer player) {
        if (STATES.remove(player.getUUID()) != null) {
            SyncCelestiumSpearStunCooldownPayload.send(player, 0);
        }
    }

    private static final class PlayerState {
        private long cooldownEndsAt;
    }
}
