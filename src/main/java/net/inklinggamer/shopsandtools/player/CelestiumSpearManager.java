package net.inklinggamer.shopsandtools.player;

import net.inklinggamer.shopsandtools.item.CelestiumSpearHelper;
import net.inklinggamer.shopsandtools.network.SyncCelestiumSpearStunCooldownPayload;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

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
        int serverTicks = server.getTicks();
        Iterator<Map.Entry<UUID, Integer>> iterator = STUNNED_MOBS.entrySet().iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getValue() <= serverTicks) {
                iterator.remove();
            }
        }

        STATES.entrySet().removeIf(entry -> server.getPlayerManager().getPlayer(entry.getKey()) == null);
    }

    public static void tickPlayer(ServerPlayerEntity player) {
        if (!player.isAlive()) {
            resetPlayerState(player);
            return;
        }

        expireStunCooldown(player, player.getEntityWorld().getTime());
        if (!CelestiumSpearHelper.isCelestiumSpearHeld(player)) {
            return;
        }

        StatusEffectInstance currentSpeed = player.getStatusEffect(StatusEffects.SPEED);
        if (currentSpeed == null || currentSpeed.getAmplifier() != 0 || currentSpeed.getDuration() <= SPEED_REFRESH_THRESHOLD_TICKS) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, SPEED_DURATION_TICKS, 0, false, false, false));
        }
    }

    public static void onDirectSpearDamage(ServerPlayerEntity attacker, LivingEntity target, float damageDealt) {
        if (!CelestiumSpearHelper.isCelestiumSpearEquipped(attacker) || damageDealt <= 0.0F) {
            return;
        }

        target.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, WITHER_DURATION_TICKS, WITHER_AMPLIFIER, false, true, true));

        if (!(target instanceof MobEntity mob)) {
            return;
        }

        long worldTime = attacker.getEntityWorld().getTime();
        if (hasActiveStunCooldown(attacker, worldTime)) {
            return;
        }

        STUNNED_MOBS.put(mob.getUuid(), attacker.getEntityWorld().getServer().getTicks() + STUN_DURATION_TICKS);
        startStunCooldown(attacker, worldTime);
    }

    public static boolean isStunned(MobEntity mob) {
        Integer expiresAt = STUNNED_MOBS.get(mob.getUuid());
        if (expiresAt == null) {
            return false;
        }

        int currentTicks = mob.getEntityWorld() instanceof net.minecraft.server.world.ServerWorld serverWorld
                ? serverWorld.getServer().getTicks()
                : Integer.MAX_VALUE;
        if (expiresAt <= currentTicks) {
            STUNNED_MOBS.remove(mob.getUuid());
            return false;
        }

        return true;
    }

    private static boolean hasActiveStunCooldown(ServerPlayerEntity player, long worldTime) {
        PlayerState state = STATES.get(player.getUuid());
        return state != null && state.cooldownEndsAt > worldTime;
    }

    private static void startStunCooldown(ServerPlayerEntity player, long worldTime) {
        PlayerState state = STATES.computeIfAbsent(player.getUuid(), uuid -> new PlayerState());
        state.cooldownEndsAt = worldTime + STUN_COOLDOWN_TICKS;
        SyncCelestiumSpearStunCooldownPayload.send(player, STUN_COOLDOWN_TICKS);
    }

    private static void expireStunCooldown(ServerPlayerEntity player, long worldTime) {
        PlayerState state = STATES.get(player.getUuid());
        if (state != null && state.cooldownEndsAt <= worldTime) {
            STATES.remove(player.getUuid());
        }
    }

    private static void resetPlayerState(ServerPlayerEntity player) {
        if (STATES.remove(player.getUuid()) != null) {
            SyncCelestiumSpearStunCooldownPayload.send(player, 0);
        }
    }

    private static final class PlayerState {
        private long cooldownEndsAt;
    }
}
