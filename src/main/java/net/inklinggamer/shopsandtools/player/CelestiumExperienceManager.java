package net.inklinggamer.shopsandtools.player;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class CelestiumExperienceManager {
    private static final double XP_REMAINDER_EPSILON = 1.0E-6D;
    private static final double SWORD_XP_BONUS_MULTIPLIER = 0.20D;
    private static final double PICKAXE_XP_BONUS_MULTIPLIER = 0.50D;
    private static final double AXE_XP_BONUS_MULTIPLIER = 0.50D;
    private static final Map<UUID, Double> XP_BONUS_REMAINDERS = new HashMap<>();

    private CelestiumExperienceManager() {
    }

    public static void tickServer(MinecraftServer server) {
        XP_BONUS_REMAINDERS.entrySet().removeIf(entry -> server.getPlayerManager().getPlayer(entry.getKey()) == null);
    }

    public static void tickPlayer(ServerPlayerEntity player) {
        if (!player.isAlive() || !hasHeldXpBonusItem(player)) {
            XP_BONUS_REMAINDERS.remove(player.getUuid());
            return;
        }

        double remainder = XP_BONUS_REMAINDERS.getOrDefault(player.getUuid(), 0.0D);
        if (remainder <= XP_REMAINDER_EPSILON) {
            XP_BONUS_REMAINDERS.remove(player.getUuid());
        }
    }

    public static int applyHeldXpBonus(ServerPlayerEntity player, int baseExperience) {
        if (baseExperience <= 0) {
            return baseExperience;
        }

        if (!hasHeldXpBonusItem(player)) {
            XP_BONUS_REMAINDERS.remove(player.getUuid());
            return baseExperience;
        }

        double carriedRemainder = XP_BONUS_REMAINDERS.getOrDefault(player.getUuid(), 0.0D);
        XpAdjustment adjustment = adjustExperience(
                baseExperience,
                CelestiumSwordManager.isCelestiumSwordHeldForXp(player) ? 1 : 0,
                CelestiumPickaxeManager.isCelestiumPickaxeHeldForXp(player) ? 1 : 0,
                CelestiumAxeManager.isCelestiumAxeHeldForXp(player) ? 1 : 0,
                carriedRemainder
        );

        if (adjustment.remainder() <= XP_REMAINDER_EPSILON) {
            XP_BONUS_REMAINDERS.remove(player.getUuid());
        } else {
            XP_BONUS_REMAINDERS.put(player.getUuid(), adjustment.remainder());
        }

        return adjustment.adjustedExperience();
    }

    static XpAdjustment adjustExperience(int baseExperience, int swordCopies, int pickaxeCopies, int axeCopies, double carriedRemainder) {
        if (baseExperience <= 0) {
            return new XpAdjustment(baseExperience, 0.0D);
        }

        double totalBonusMultiplier = 0.0D;
        if (swordCopies > 0) {
            totalBonusMultiplier += SWORD_XP_BONUS_MULTIPLIER;
        }
        if (pickaxeCopies > 0) {
            totalBonusMultiplier += PICKAXE_XP_BONUS_MULTIPLIER;
        }
        if (axeCopies > 0) {
            totalBonusMultiplier += AXE_XP_BONUS_MULTIPLIER;
        }

        if (totalBonusMultiplier <= 0.0D) {
            return new XpAdjustment(baseExperience, 0.0D);
        }

        double totalBonusExperience = baseExperience * totalBonusMultiplier + carriedRemainder;
        int bonusExperience = (int) Math.floor(totalBonusExperience + XP_REMAINDER_EPSILON);
        double remainder = Math.max(0.0D, totalBonusExperience - bonusExperience);
        return new XpAdjustment(baseExperience + bonusExperience, remainder);
    }

    private static boolean hasHeldXpBonusItem(ServerPlayerEntity player) {
        return CelestiumSwordManager.isCelestiumSwordHeldForXp(player)
                || CelestiumPickaxeManager.isCelestiumPickaxeHeldForXp(player)
                || CelestiumAxeManager.isCelestiumAxeHeldForXp(player);
    }

    record XpAdjustment(int adjustedExperience, double remainder) {
    }
}
