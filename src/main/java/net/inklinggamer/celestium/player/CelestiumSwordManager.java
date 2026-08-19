package net.inklinggamer.celestium.player;

import net.inklinggamer.celestium.Celestium;
import net.inklinggamer.celestium.item.ModItems;
import net.inklinggamer.celestium.network.SyncCelestiumRagePayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class CelestiumSwordManager {
    private static final int MAX_RAGE_STACKS = 10;
    private static final int RAGE_DURATION_TICKS = 1200;
    private static final float LIFESTEAL_RATIO = 0.10F;
    private static final double ATTACK_SPEED_PER_STACK = 0.05D;
    private static final Identifier RAGE_ATTACK_SPEED_MODIFIER_ID = Identifier.fromNamespaceAndPath(Celestium.MOD_ID, "celestium_sword_rage_attack_speed");

    private static final Map<UUID, PlayerState> STATES = new HashMap<>();
    private static final Set<UUID> ACTIVE_ATTACKERS = new HashSet<>();

    private CelestiumSwordManager() {
    }

    public static void tickServer(MinecraftServer server) {
        STATES.entrySet().removeIf(entry -> server.getPlayerList().getPlayer(entry.getKey()) == null);
    }

    public static void tickPlayer(ServerPlayer player) {
        if (!player.isAlive()) {
            resetPlayerState(player);
            return;
        }

        PlayerState state = STATES.get(player.getUUID());
        if (state == null) {
            clearAttackSpeedModifier(player);
            return;
        }

        expireRageStacks(state, player.level().getGameTime());
        updateAttackSpeedModifier(player, state);
        syncRageStacks(player, state);

        if (state.canDiscard()) {
            STATES.remove(player.getUUID());
        }
    }

    public static boolean isCelestiumSwordEquipped(Player player) {
        return player.getItemBySlot(EquipmentSlot.MAINHAND).is(ModItems.CELESTIUM_SWORD);
    }

    public static boolean isCelestiumSwordHeldForXp(Player player) {
        return player.getMainHandItem().is(ModItems.CELESTIUM_SWORD)
                || player.getOffhandItem().is(ModItems.CELESTIUM_SWORD);
    }

    public static boolean isCelestiumRageWeaponEquipped(Player player) {
        return isCelestiumSwordEquipped(player) || CelestiumAxeManager.isCelestiumAxeEquipped(player);
    }

    public static void beginRageWeaponAttack(ServerPlayer player) {
        if (isCelestiumRageWeaponEquipped(player)) {
            ACTIVE_ATTACKERS.add(player.getUUID());
        }
    }

    public static void endRageWeaponAttack(ServerPlayer player) {
        ACTIVE_ATTACKERS.remove(player.getUUID());
    }

    public static void onDirectSwordDamage(ServerPlayer player, float damageDealt) {
        if (!isCelestiumSwordEquipped(player) || damageDealt <= 0.0F) {
            return;
        }

        player.heal(damageDealt * LIFESTEAL_RATIO);
    }

    public static void onRageWeaponMobKilled(ServerPlayer player) {
        if (!ACTIVE_ATTACKERS.contains(player.getUUID()) || !isCelestiumRageWeaponEquipped(player)) {
            return;
        }

        addRageStack(player);
    }

    private static void addRageStack(ServerPlayer player) {
        PlayerState state = STATES.computeIfAbsent(player.getUUID(), uuid -> new PlayerState());
        long expiresAt = player.level().getGameTime() + RAGE_DURATION_TICKS;

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

    private static void updateAttackSpeedModifier(ServerPlayer player, PlayerState state) {
        AttributeInstance attackSpeedAttribute = player.getAttribute(Attributes.ATTACK_SPEED);
        if (attackSpeedAttribute == null) {
            return;
        }

        int appliedStacks = isCelestiumRageWeaponEquipped(player) ? state.rageExpirations.size() : 0;
        if (state.appliedAttackSpeedStacks == appliedStacks) {
            return;
        }

        if (attackSpeedAttribute.hasModifier(RAGE_ATTACK_SPEED_MODIFIER_ID)) {
            attackSpeedAttribute.removeModifier(RAGE_ATTACK_SPEED_MODIFIER_ID);
        }

        if (appliedStacks > 0) {
            attackSpeedAttribute.addTransientModifier(new AttributeModifier(
                    RAGE_ATTACK_SPEED_MODIFIER_ID,
                    appliedStacks * ATTACK_SPEED_PER_STACK,
                    AttributeModifier.Operation.ADD_MULTIPLIED_BASE
            ));
        }

        state.appliedAttackSpeedStacks = appliedStacks;
    }

    private static void syncRageStacks(ServerPlayer player, PlayerState state) {
        int currentStacks = state.rageExpirations.size();
        if (state.lastSyncedRageStacks == currentStacks) {
            return;
        }

        SyncCelestiumRagePayload.send(player, currentStacks);
        state.lastSyncedRageStacks = currentStacks;
    }

    private static void resetPlayerState(ServerPlayer player) {
        PlayerState state = STATES.remove(player.getUUID());
        if (state == null) {
            clearAttackSpeedModifier(player);
            SyncCelestiumRagePayload.send(player, 0);
            return;
        }

        clearAttackSpeedModifier(player);
        SyncCelestiumRagePayload.send(player, 0);
    }

    private static void clearAttackSpeedModifier(ServerPlayer player) {
        AttributeInstance attackSpeedAttribute = player.getAttribute(Attributes.ATTACK_SPEED);
        if (attackSpeedAttribute != null && attackSpeedAttribute.hasModifier(RAGE_ATTACK_SPEED_MODIFIER_ID)) {
            attackSpeedAttribute.removeModifier(RAGE_ATTACK_SPEED_MODIFIER_ID);
        }
    }

    private static final class PlayerState {
        private final List<Long> rageExpirations = new ArrayList<>();
        private int lastSyncedRageStacks = -1;
        private int appliedAttackSpeedStacks = -1;

        private boolean canDiscard() {
            return this.rageExpirations.isEmpty();
        }
    }
}
