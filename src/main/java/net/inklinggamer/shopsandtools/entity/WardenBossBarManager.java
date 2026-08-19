package net.inklinggamer.shopsandtools.entity;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.warden.Warden;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class WardenBossBarManager {
    private static final double BOSS_BAR_RANGE = 100.0D;
    private static final double BOSS_BAR_RANGE_SQUARED = BOSS_BAR_RANGE * BOSS_BAR_RANGE;
    private static final int DEATH_GRACE_TICKS = 60;
    private static final Map<UUID, TrackedWarden> TRACKERS = new HashMap<>();

    private WardenBossBarManager() {
    }

    public static void register() {
        ServerEntityEvents.ENTITY_LOAD.register(WardenBossBarManager::handleEntityLoad);
        ServerEntityEvents.ENTITY_UNLOAD.register(WardenBossBarManager::handleEntityUnload);
        ServerLivingEntityEvents.AFTER_DEATH.register(WardenBossBarManager::handleAfterDeath);
    }

    public static void tickServer(MinecraftServer server) {
        TRACKERS.entrySet().removeIf(entry -> {
            boolean keepTracker = entry.getValue().tick();
            if (!keepTracker) {
                entry.getValue().clearPlayers();
            }

            return !keepTracker;
        });
    }

    static float getBossBarPercent(float currentHealth, float maxHealth) {
        if (maxHealth <= 0.0F) {
            return 0.0F;
        }

        return Math.max(0.0F, Math.min(1.0F, currentHealth / maxHealth));
    }

    static boolean isWithinBossBarRange(double squaredDistance) {
        return squaredDistance <= BOSS_BAR_RANGE_SQUARED;
    }

    static int tickDeathGrace(int remainingTicks) {
        return Math.max(0, remainingTicks - 1);
    }

    private static void handleEntityLoad(Entity entity, ServerLevel world) {
        if (!(entity instanceof Warden warden)) {
            return;
        }

        TrackedWarden previousTracker = TRACKERS.put(warden.getUUID(), new TrackedWarden(warden, world));
        if (previousTracker != null) {
            previousTracker.clearPlayers();
        }
    }

    private static void handleEntityUnload(Entity entity, ServerLevel world) {
        if (!(entity instanceof Warden warden)) {
            return;
        }

        TrackedWarden tracker = TRACKERS.get(warden.getUUID());
        if (tracker == null) {
            return;
        }

        tracker.captureWardenState();
        tracker.detach();
        if (!tracker.isPersistingAfterDeath()) {
            tracker.clearPlayers();
            TRACKERS.remove(warden.getUUID());
        }
    }

    private static void handleAfterDeath(LivingEntity entity, net.minecraft.world.damagesource.DamageSource damageSource) {
        if (!(entity instanceof Warden warden)) {
            return;
        }

        TRACKERS.compute(warden.getUUID(), (uuid, existingTracker) -> {
            TrackedWarden tracker = existingTracker == null
                    ? new TrackedWarden(warden, (ServerLevel) warden.level())
                    : existingTracker;
            tracker.markDead();
            return tracker;
        });
    }

    private static final class TrackedWarden {
        private final ServerBossEvent bossBar;
        private Warden warden;
        private ServerLevel world;
        private double x;
        private double y;
        private double z;
        private Component name;
        private int deathGraceTicksRemaining;

        private TrackedWarden(Warden warden, ServerLevel world) {
            this.bossBar = new ServerBossEvent(warden.getUUID(), warden.getDisplayName(), BossEvent.BossBarColor.BLUE, BossEvent.BossBarOverlay.PROGRESS);
            this.warden = warden;
            this.world = world;
            this.name = warden.getDisplayName();
            captureWardenState();
            this.bossBar.setVisible(true);
        }

        private boolean tick() {
            if (this.warden != null) {
                captureWardenState();
            }

            syncPlayers();

            if (this.deathGraceTicksRemaining > 0) {
                this.deathGraceTicksRemaining = tickDeathGrace(this.deathGraceTicksRemaining);
            }

            return (this.warden != null && this.warden.isAlive()) || this.deathGraceTicksRemaining > 0;
        }

        private void markDead() {
            captureWardenState();
            this.deathGraceTicksRemaining = DEATH_GRACE_TICKS;
            this.bossBar.setProgress(0.0F);
        }

        private boolean isPersistingAfterDeath() {
            return this.deathGraceTicksRemaining > 0;
        }

        private void detach() {
            this.warden = null;
        }

        private void captureWardenState() {
            if (this.warden == null) {
                return;
            }

            this.world = (ServerLevel) this.warden.level();
            this.x = this.warden.getX();
            this.y = this.warden.getY();
            this.z = this.warden.getZ();
            this.name = this.warden.getDisplayName();
            this.bossBar.setName(this.name);
            this.bossBar.setProgress(this.warden.isAlive() ? getBossBarPercent(this.warden.getHealth(), this.warden.getMaxHealth()) : 0.0F);
        }

        private void syncPlayers() {
            for (ServerPlayer player : List.copyOf(this.bossBar.getPlayers())) {
                if (player.level() != this.world || !isWithinBossBarRange(player.distanceToSqr(this.x, this.y, this.z))) {
                    this.bossBar.removePlayer(player);
                }
            }

            for (ServerPlayer player : this.world.players()) {
                if (isWithinBossBarRange(player.distanceToSqr(this.x, this.y, this.z))) {
                    this.bossBar.addPlayer(player);
                }
            }
        }

        private void clearPlayers() {
            this.bossBar.removeAllPlayers();
        }
    }
}
