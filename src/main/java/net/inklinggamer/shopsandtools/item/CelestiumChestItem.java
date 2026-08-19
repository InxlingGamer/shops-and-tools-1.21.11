package net.inklinggamer.shopsandtools.item;

import net.inklinggamer.shopsandtools.ShopsAndTools;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class CelestiumChestItem extends Item {
    private static final Identifier BONUS_HEALTH_MODIFIER_ID = Identifier.fromNamespaceAndPath(ShopsAndTools.MOD_ID, "celestium_chest_bonus_health");
    private static final AttributeModifier BONUS_HEALTH_MODIFIER = new AttributeModifier(BONUS_HEALTH_MODIFIER_ID, 10.0D, AttributeModifier.Operation.ADD_VALUE);
    private static final float REGENERATION_THRESHOLD = 10.0F;
    private static final float FIRE_RESISTANCE_THRESHOLD = 6.0F;
    private static final int EFFECT_DURATION_TICKS = 100;
    private static final int EFFECT_REFRESH_THRESHOLD_TICKS = 20;

    public CelestiumChestItem(Properties settings) {
        super(settings);
    }

    public static void tickPlayer(Player player) {
        AttributeInstance maxHealthAttribute = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealthAttribute == null) {
            return;
        }

        ItemStack chestStack = player.getItemBySlot(EquipmentSlot.CHEST);

        if (!(chestStack.getItem() instanceof CelestiumChestItem)) {
            removeBonusHealth(player, maxHealthAttribute);
            return;
        }

        applyBonusHealth(player, maxHealthAttribute);

        if (player.getHealth() <= REGENERATION_THRESHOLD) {
            refreshEffect(player, MobEffects.REGENERATION);
        }

        if (player.getHealth() <= FIRE_RESISTANCE_THRESHOLD) {
            refreshEffect(player, MobEffects.FIRE_RESISTANCE);
        }
    }

    private static void applyBonusHealth(Player player, AttributeInstance maxHealthAttribute) {
        if (maxHealthAttribute.hasModifier(BONUS_HEALTH_MODIFIER_ID)) {
            return;
        }

        float oldMaxHealth = player.getMaxHealth();
        boolean wasFullHealth = player.getHealth() >= oldMaxHealth - 0.001F;
        maxHealthAttribute.addTransientModifier(BONUS_HEALTH_MODIFIER);

        if (wasFullHealth) {
            player.setHealth(player.getMaxHealth());
        }
    }

    private static void removeBonusHealth(Player player, AttributeInstance maxHealthAttribute) {
        if (!maxHealthAttribute.hasModifier(BONUS_HEALTH_MODIFIER_ID)) {
            return;
        }

        maxHealthAttribute.removeModifier(BONUS_HEALTH_MODIFIER_ID);
        float maxHealth = player.getMaxHealth();
        if (player.getHealth() > maxHealth) {
            player.setHealth(maxHealth);
        }
    }

    private static void refreshEffect(Player player, Holder<MobEffect> effect) {
        MobEffectInstance currentEffect = player.getEffect(effect);

        if (currentEffect == null || currentEffect.getAmplifier() != 0 || currentEffect.getDuration() <= EFFECT_REFRESH_THRESHOLD_TICKS) {
            player.addEffect(new MobEffectInstance(effect, EFFECT_DURATION_TICKS, 0, false, false, false));
        }
    }
}
