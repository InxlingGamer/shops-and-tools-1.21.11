package net.inklinggamer.celestium.item;

import net.inklinggamer.celestium.advancement.ModAdvancementActions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class SkulkVenomItem extends Item {

    public SkulkVenomItem(Properties settings) {
        super(settings);
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel world, Entity entity, EquipmentSlot slot) {
        // We only want this to affect players, not zombies that happen to pick it up
        if (!(entity instanceof Player player)) {
            return;
        }

        // We only run the logic once per second (every 20 ticks) so it doesn't drain 20 XP per second!
        if (world.getGameTime() % 20 == 0) {

            // Check if the player has any XP points or levels remaining
            boolean hasXp = player.totalExperience > 0 || player.experienceLevel > 0;

            if (hasXp) {
                // 1. Drain 2 XP point per second
                player.giveExperiencePoints(-2);

                // 2. Apply positive buffs!
                // We use a duration of 40 ticks (2 seconds) so the effect doesn't flicker on the screen.
                // Note: Amplifier 2 = Level 3. Amplifier 1 = Level 2.
                player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 40, 2, false, false, true));
                player.addEffect(new MobEffectInstance(MobEffects.HASTE, 40, 2, false, false, true));
                player.addEffect(new MobEffectInstance(MobEffects.SPEED, 40, 1, false, false, true));
                if (player instanceof ServerPlayer serverPlayer) {
                    ModAdvancementActions.triggerAStrangeEnergy(serverPlayer);
                }
            } else {
                // 3. Out of XP! The parasite turns on the player.
                // Applies Wither II (Amplifier 1)
                player.addEffect(new MobEffectInstance(MobEffects.WITHER, 40, 1, false, false, true));
            }
        }
    }
}
