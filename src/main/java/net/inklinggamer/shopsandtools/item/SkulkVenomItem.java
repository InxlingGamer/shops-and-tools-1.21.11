package net.inklinggamer.shopsandtools.item;

import net.inklinggamer.shopsandtools.advancement.ModAdvancementActions;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class SkulkVenomItem extends Item {

    public SkulkVenomItem(Settings settings) {
        super(settings);
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerWorld world, Entity entity, EquipmentSlot slot) {
        // We only want this to affect players, not zombies that happen to pick it up
        if (!(entity instanceof PlayerEntity player)) {
            return;
        }

        // We only run the logic once per second (every 20 ticks) so it doesn't drain 20 XP per second!
        if (world.getTime() % 20 == 0) {

            // Check if the player has any XP points or levels remaining
            boolean hasXp = player.totalExperience > 0 || player.experienceLevel > 0;

            if (hasXp) {
                // 1. Drain 2 XP point per second
                player.addExperience(-2);

                // 2. Apply positive buffs!
                // We use a duration of 40 ticks (2 seconds) so the effect doesn't flicker on the screen.
                // Note: Amplifier 2 = Level 3. Amplifier 1 = Level 2.
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 40, 2, false, false, true));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 40, 2, false, false, true));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 40, 1, false, false, true));
                if (player instanceof ServerPlayerEntity serverPlayer) {
                    ModAdvancementActions.triggerAStrangeEnergy(serverPlayer);
                }
            } else {
                // 3. Out of XP! The parasite turns on the player.
                // Applies Wither II (Amplifier 1)
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 40, 1, false, false, true));
            }
        }
    }
}
