package net.inklinggamer.shopsandtools.player;

import net.inklinggamer.shopsandtools.ShopsAndTools;
import net.inklinggamer.shopsandtools.item.ModItems;
import net.inklinggamer.shopsandtools.network.SyncCelestiumRagePayload;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class CelestiumSwordManager {
    private static final double XP_REMAINDER_EPSILON = 1.0E-6D;
    private static final int MAX_RAGE_STACKS = 10;
    private static final int RAGE_DURATION_TICKS = 600;
    private static final double XP_BONUS_MULTIPLIER = 0.20D;
    private static final float LIFESTEAL_RATIO = 0.10F;
    private static final double ATTACK_SPEED_PER_STACK = 0.05D;
    private static final Identifier RAGE_ATTACK_SPEED_MODIFIER_ID = Identifier.of(ShopsAndTools.MOD_ID, "celestium_sword_rage_attack_speed");

    private static final Map<UUID, PlayerState> STATES = new HashMap<>();

    private CelestiumSwordManager() {
    }

    public static void tickServer(MinecraftServer server) {
        STATES.entrySet().removeIf(entry -> server.getPlayerManager().getPlayer(entry.getKey()) == null);
    }

    public static void tickPlayer(ServerPlayerEntity player) {
        if (!player.isAlive()) {
            resetPlayerState(player);
            return;
        }

        PlayerState state = STATES.get(player.getUuid());
        if (state == null) {
            clearAttackSpeedModifier(player);
            return;
        }

        expireRageStacks(state, player.getEntityWorld().getTime());
        updateAttackSpeedModifier(player, state);
        syncRageStacks(player, state);

        if (state.canDiscard()) {
            STATES.remove(player.getUuid());
        }
    }

    public static boolean isCelestiumSwordEquipped(PlayerEntity player) {
        return player.getEquippedStack(EquipmentSlot.MAINHAND).isOf(ModItems.CELESTIUM_SWORD);
    }

    public static void onDirectSwordDamage(ServerPlayerEntity player, float damageDealt, boolean killedMob) {
        if (!isCelestiumSwordEquipped(player) || damageDealt <= 0.0F) {
            return;
        }

        player.heal(damageDealt * LIFESTEAL_RATIO);

        if (killedMob) {
            addRageStack(player);
        }
    }

    public static int applyXpBonus(ServerPlayerEntity player, int baseExperience) {
        if (baseExperience <= 0 || !isCelestiumSwordEquipped(player)) {
            return baseExperience;
        }

        PlayerState state = STATES.computeIfAbsent(player.getUuid(), uuid -> new PlayerState());
        double totalBonus = baseExperience * XP_BONUS_MULTIPLIER + state.xpBonusRemainder;
        int bonusExperience = (int) Math.floor(totalBonus);
        state.xpBonusRemainder = totalBonus - bonusExperience;
        return baseExperience + bonusExperience;
    }

    private static void addRageStack(ServerPlayerEntity player) {
        PlayerState state = STATES.computeIfAbsent(player.getUuid(), uuid -> new PlayerState());
        long expiresAt = player.getEntityWorld().getTime() + RAGE_DURATION_TICKS;

        if (state.rageExpirations.size() >= MAX_RAGE_STACKS) {
            state.rageExpirations.removeFirst();
        }

        state.rageExpirations.add(expiresAt);
        updateAttackSpeedModifier(player, state);
        syncRageStacks(player, state);
    }

    private static void expireRageStacks(PlayerState state, long worldTime) {
        while (!state.rageExpirations.isEmpty() && state.rageExpirations.getFirst() <= worldTime) {
            state.rageExpirations.removeFirst();
        }
    }

    private static void updateAttackSpeedModifier(ServerPlayerEntity player, PlayerState state) {
        EntityAttributeInstance attackSpeedAttribute = player.getAttributeInstance(EntityAttributes.ATTACK_SPEED);
        if (attackSpeedAttribute == null) {
            return;
        }

        int appliedStacks = isCelestiumSwordEquipped(player) ? state.rageExpirations.size() : 0;
        if (state.appliedAttackSpeedStacks == appliedStacks) {
            return;
        }

        if (attackSpeedAttribute.hasModifier(RAGE_ATTACK_SPEED_MODIFIER_ID)) {
            attackSpeedAttribute.removeModifier(RAGE_ATTACK_SPEED_MODIFIER_ID);
        }

        if (appliedStacks > 0) {
            attackSpeedAttribute.addTemporaryModifier(new EntityAttributeModifier(
                    RAGE_ATTACK_SPEED_MODIFIER_ID,
                    appliedStacks * ATTACK_SPEED_PER_STACK,
                    EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE
            ));
        }

        state.appliedAttackSpeedStacks = appliedStacks;
    }

    private static void syncRageStacks(ServerPlayerEntity player, PlayerState state) {
        int currentStacks = state.rageExpirations.size();
        if (state.lastSyncedRageStacks == currentStacks) {
            return;
        }

        SyncCelestiumRagePayload.send(player, currentStacks);
        state.lastSyncedRageStacks = currentStacks;
    }

    private static void resetPlayerState(ServerPlayerEntity player) {
        PlayerState state = STATES.remove(player.getUuid());
        if (state == null) {
            clearAttackSpeedModifier(player);
            SyncCelestiumRagePayload.send(player, 0);
            return;
        }

        clearAttackSpeedModifier(player);
        SyncCelestiumRagePayload.send(player, 0);
    }

    private static void clearAttackSpeedModifier(ServerPlayerEntity player) {
        EntityAttributeInstance attackSpeedAttribute = player.getAttributeInstance(EntityAttributes.ATTACK_SPEED);
        if (attackSpeedAttribute != null && attackSpeedAttribute.hasModifier(RAGE_ATTACK_SPEED_MODIFIER_ID)) {
            attackSpeedAttribute.removeModifier(RAGE_ATTACK_SPEED_MODIFIER_ID);
        }
    }

    private static final class PlayerState {
        private final List<Long> rageExpirations = new ArrayList<>();
        private double xpBonusRemainder;
        private int lastSyncedRageStacks = -1;
        private int appliedAttackSpeedStacks = -1;

        private boolean canDiscard() {
            return this.rageExpirations.isEmpty() && this.xpBonusRemainder <= XP_REMAINDER_EPSILON;
        }
    }
}
