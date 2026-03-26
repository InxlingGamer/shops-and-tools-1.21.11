package net.inklinggamer.shopsandtools.advancement;

import net.inklinggamer.shopsandtools.ShopsAndTools;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;

public final class CelestiumAdvancementHelper {
    public static final String CELESTIUM_HELMET_ID = ShopsAndTools.MOD_ID + ":celestium_helmet";
    public static final String CELESTIUM_CHESTPLATE_ID = ShopsAndTools.MOD_ID + ":celestium_chestplate";
    public static final String CELESTIUM_ELYTRA_CHESTPLATE_ID = ShopsAndTools.MOD_ID + ":celestium_elytra_chestplate";
    public static final String CELESTIUM_LEGGINGS_ID = ShopsAndTools.MOD_ID + ":celestium_leggings";
    public static final String CELESTIUM_BOOTS_ID = ShopsAndTools.MOD_ID + ":celestium_boots";

    private CelestiumAdvancementHelper() {
    }

    public static boolean isFullyAscended(PlayerEntity player) {
        return isFullyAscended(
                getItemId(player.getEquippedStack(EquipmentSlot.HEAD)),
                getItemId(player.getEquippedStack(EquipmentSlot.CHEST)),
                getItemId(player.getEquippedStack(EquipmentSlot.LEGS)),
                getItemId(player.getEquippedStack(EquipmentSlot.FEET))
        );
    }

    public static boolean isFullyAscended(String headItemId, String chestItemId, String legsItemId, String feetItemId) {
        return CELESTIUM_HELMET_ID.equals(headItemId)
                && isCelestiumChestItem(chestItemId)
                && CELESTIUM_LEGGINGS_ID.equals(legsItemId)
                && CELESTIUM_BOOTS_ID.equals(feetItemId);
    }

    public static boolean isCelestiumChestItem(String itemId) {
        return CELESTIUM_CHESTPLATE_ID.equals(itemId) || CELESTIUM_ELYTRA_CHESTPLATE_ID.equals(itemId);
    }

    private static String getItemId(ItemStack stack) {
        return Registries.ITEM.getId(stack.getItem()).toString();
    }
}
