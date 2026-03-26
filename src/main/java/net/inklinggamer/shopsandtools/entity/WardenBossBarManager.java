package net.inklinggamer.shopsandtools.entity;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

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

    private static void handleEntityLoad(Entity entity, ServerWorld world) {
        if (!(entity instanceof WardenEntity warden)) {
            return;
        }

        TrackedWarden previousTracker = TRACKERS.put(warden.getUuid(), new TrackedWarden(warden, world));
        if (previousTracker != null) {
            previousTracker.clearPlayers();
        }
    }

    private static void handleEntityUnload(Entity entity, ServerWorld world) {
        if (!(entity instanceof WardenEntity warden)) {
            return;
        }

        TrackedWarden tracker = TRACKERS.get(warden.getUuid());
        if (tracker == null) {
            return;
        }

        tracker.captureWardenState();
        tracker.detach();
        if (!tracker.isPersistingAfterDeath()) {
            tracker.clearPlayers();
            TRACKERS.remove(warden.getUuid());
        }
    }

    private static void handleAfterDeath(LivingEntity entity, net.minecraft.entity.damage.DamageSource damageSource) {
        if (!(entity instanceof WardenEntity warden)) {
            return;
        }

        TRACKERS.compute(warden.getUuid(), (uuid, existingTracker) -> {
            TrackedWarden tracker = existingTracker == null
                    ? new TrackedWarden(warden, (ServerWorld) warden.getEntityWorld())
                    : existingTracker;
            tracker.markDead();
            return tracker;
        });
    }

    private static final class TrackedWarden {
        private final ServerBossBar bossBar;
        private WardenEntity warden;
        private ServerWorld world;
        private double x;
        private double y;
        private double z;
        private Text name;
        private int deathGraceTicksRemaining;

        private TrackedWarden(WardenEntity warden, ServerWorld world) {
            this.bossBar = new ServerBossBar(warden.getDisplayName(), BossBar.Color.BLUE, BossBar.Style.PROGRESS);
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
            this.bossBar.setPercent(0.0F);
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

            this.world = (ServerWorld) this.warden.getEntityWorld();
            this.x = this.warden.getX();
            this.y = this.warden.getY();
            this.z = this.warden.getZ();
            this.name = this.warden.getDisplayName();
            this.bossBar.setName(this.name);
            this.bossBar.setPercent(this.warden.isAlive() ? getBossBarPercent(this.warden.getHealth(), this.warden.getMaxHealth()) : 0.0F);
        }

        private void syncPlayers() {
            for (ServerPlayerEntity player : List.copyOf(this.bossBar.getPlayers())) {
                if (player.getEntityWorld() != this.world || !isWithinBossBarRange(player.squaredDistanceTo(this.x, this.y, this.z))) {
                    this.bossBar.removePlayer(player);
                }
            }

            for (ServerPlayerEntity player : this.world.getPlayers()) {
                if (isWithinBossBarRange(player.squaredDistanceTo(this.x, this.y, this.z))) {
                    this.bossBar.addPlayer(player);
                }
            }
        }

        private void clearPlayers() {
            this.bossBar.clearPlayers();
        }
    }
}
