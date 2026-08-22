package net.inklinggamer.celestium.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class CelestiumHelmetItem extends ArmorItem {

    public CelestiumHelmetItem(Settings settings) {
        super(ModArmorMaterials.CELESTIUM, Type.HELMET, settings);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (world.isClient() || !(entity instanceof PlayerEntity player)) {
            return;
        }

        // Verify the helmet is actually equipped on the head
        if (player.getEquippedStack(EquipmentSlot.HEAD) != stack) {
            return;
        }


        // =====================================
        // 1. APPLY BUFFS (Saturation & Breathing)
        // =====================================
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.WATER_BREATHING, 20, 0, false, false, false));

        // =====================================
        // 2. PREVENT DEBUFFS (Blindness, Darkness, Nausea)
        // =====================================
        player.removeStatusEffect(StatusEffects.BLINDNESS);
        player.removeStatusEffect(StatusEffects.DARKNESS);
        player.removeStatusEffect(StatusEffects.NAUSEA);

        // =====================================
        // 3. SCAN FOR ORES (Once per second)
        // =====================================
        if (world.getTime() % 20 == 0) {

            if (player.getHungerManager().getFoodLevel() < 19) {
                player.getHungerManager().setFoodLevel(19);
            }

        }
    }
}
