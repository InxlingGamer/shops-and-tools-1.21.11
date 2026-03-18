package net.inklinggamer.shopsandtools.item;

import net.inklinggamer.shopsandtools.ShopsAndTools;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public class CelestiumChestItem extends Item {
    private static final Identifier BONUS_HEALTH_MODIFIER_ID = Identifier.of(ShopsAndTools.MOD_ID, "celestium_chest_bonus_health");
    private static final EntityAttributeModifier BONUS_HEALTH_MODIFIER = new EntityAttributeModifier(BONUS_HEALTH_MODIFIER_ID, 10.0D, EntityAttributeModifier.Operation.ADD_VALUE);
    private static final float REGENERATION_THRESHOLD = 10.0F;
    private static final float FIRE_RESISTANCE_THRESHOLD = 6.0F;
    private static final int EFFECT_DURATION_TICKS = 100;
    private static final int EFFECT_REFRESH_THRESHOLD_TICKS = 20;

    public CelestiumChestItem(Settings settings) {
        super(settings);
    }

    public static void tickPlayer(PlayerEntity player) {
        EntityAttributeInstance maxHealthAttribute = player.getAttributeInstance(EntityAttributes.MAX_HEALTH);
        if (maxHealthAttribute == null) {
            return;
        }

        ItemStack chestStack = player.getEquippedStack(EquipmentSlot.CHEST);

        if (!(chestStack.getItem() instanceof CelestiumChestItem)) {
            removeBonusHealth(player, maxHealthAttribute);
            return;
        }

        applyBonusHealth(player, maxHealthAttribute);

        if (player.getHealth() <= REGENERATION_THRESHOLD) {
            refreshEffect(player, StatusEffects.REGENERATION);
        }

        if (player.getHealth() <= FIRE_RESISTANCE_THRESHOLD) {
            refreshEffect(player, StatusEffects.FIRE_RESISTANCE);
        }
    }

    private static void applyBonusHealth(PlayerEntity player, EntityAttributeInstance maxHealthAttribute) {
        if (maxHealthAttribute.hasModifier(BONUS_HEALTH_MODIFIER_ID)) {
            return;
        }

        float oldMaxHealth = player.getMaxHealth();
        boolean wasFullHealth = player.getHealth() >= oldMaxHealth - 0.001F;
        maxHealthAttribute.addTemporaryModifier(BONUS_HEALTH_MODIFIER);

        if (wasFullHealth) {
            player.setHealth(player.getMaxHealth());
        }
    }

    private static void removeBonusHealth(PlayerEntity player, EntityAttributeInstance maxHealthAttribute) {
        if (!maxHealthAttribute.hasModifier(BONUS_HEALTH_MODIFIER_ID)) {
            return;
        }

        maxHealthAttribute.removeModifier(BONUS_HEALTH_MODIFIER_ID);
        float maxHealth = player.getMaxHealth();
        if (player.getHealth() > maxHealth) {
            player.setHealth(maxHealth);
        }
    }

    private static void refreshEffect(PlayerEntity player, RegistryEntry<StatusEffect> effect) {
        StatusEffectInstance currentEffect = player.getStatusEffect(effect);

        if (currentEffect == null || currentEffect.getAmplifier() != 0 || currentEffect.getDuration() <= EFFECT_REFRESH_THRESHOLD_TICKS) {
            player.addStatusEffect(new StatusEffectInstance(effect, EFFECT_DURATION_TICKS, 0, false, false, false));
        }
    }
}
