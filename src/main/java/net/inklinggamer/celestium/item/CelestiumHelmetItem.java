package net.inklinggamer.celestium.item;

import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import java.util.concurrent.ThreadLocalRandom;

public class CelestiumHelmetItem extends Item {

    public CelestiumHelmetItem(Properties settings) {
        super(settings);
    }

    // Notice the newly updated method signature for 1.21+!
    @Override
    public void inventoryTick(ItemStack stack, ServerLevel world, Entity entity, EquipmentSlot slot) {
        // Since the method now provides a ServerWorld directly, it only runs on the server!
        // We just need to check if the entity wearing it is a player.
        if (!(entity instanceof Player player)) {
            return;
        }

        // Verify the helmet is actually equipped on the head
        if (player.getItemBySlot(EquipmentSlot.HEAD) != stack) {
            return;
        }


        // =====================================
        // 1. APPLY BUFFS (Saturation & Breathing)
        // =====================================
        player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 20, 0, false, false, false));

        // =====================================
        // 2. PREVENT DEBUFFS (Blindness, Darkness, Nausea)
        // =====================================
        player.removeEffect(MobEffects.BLINDNESS);
        player.removeEffect(MobEffects.DARKNESS);
        player.removeEffect(MobEffects.NAUSEA);

        // =====================================
        // 3. SCAN FOR ORES (Once per second)
        // =====================================
        if (world.getGameTime() % 20 == 0) {

            if (player.getFoodData().getFoodLevel() < 19) {
                player.getFoodData().setFoodLevel(19);
            }

        }
    }
}