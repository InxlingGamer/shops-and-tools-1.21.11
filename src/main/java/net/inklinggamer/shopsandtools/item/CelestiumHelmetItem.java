package net.inklinggamer.shopsandtools.item;

import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.concurrent.ThreadLocalRandom;

public class CelestiumHelmetItem extends Item {

    public CelestiumHelmetItem(Settings settings) {
        super(settings);
    }

    // Notice the newly updated method signature for 1.21+!
    @Override
    public void inventoryTick(ItemStack stack, ServerWorld world, Entity entity, EquipmentSlot slot) {
        // Since the method now provides a ServerWorld directly, it only runs on the server!
        // We just need to check if the entity wearing it is a player.
        if (!(entity instanceof PlayerEntity player)) {
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