package net.inklinggamer.shopsandtools.player;

import net.inklinggamer.shopsandtools.ShopsAndTools;
import net.inklinggamer.shopsandtools.item.CelestiumHorseArmorItem;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;

public final class CelestiumHorseArmorManager {
    private static final double EPSILON = 1.0E-6D;
    private static final float FULL_HEALTH_EPSILON = 0.001F;
    static final Identifier BONUS_HEALTH_MODIFIER_ID = Identifier.fromNamespaceAndPath(ShopsAndTools.MOD_ID, "celestium_horse_armor_bonus_health");
    static final Identifier STEP_HEIGHT_MODIFIER_ID = Identifier.fromNamespaceAndPath(ShopsAndTools.MOD_ID, "celestium_horse_armor_step_height");
    private static final double STEP_HEIGHT_BONUS = 2.0D;
    private static final int SPEED_DURATION_TICKS = 40;
    private static final int SPEED_REFRESH_THRESHOLD_TICKS = 10;
    private static final int SPEED_AMPLIFIER = 2;
    private static final int FROST_WALKER_RADIUS = 3;
    private static final int FROST_WALKER_MIN_FREEZE_TICKS = 60;
    private static final int FROST_WALKER_MAX_FREEZE_TICKS = 120;

    private CelestiumHorseArmorManager() {
    }

    public static void tickHorse(AbstractHorse horse) {
        if (!(horse.level() instanceof ServerLevel world)) {
            return;
        }

        if (!horse.isAlive()) {
            removeArmorBuffs(horse);
            return;
        }

        if (!isCelestiumHorseArmorEquipped(horse)) {
            removeArmorBuffs(horse);
            return;
        }

        applyBonusHealth(horse);
        applyStepHeight(horse);
        refreshSpeed(horse);
        freezeWaterAroundHorse(world, horse);
    }

    public static boolean isCelestiumHorseArmorEquipped(AbstractHorse horse) {
        return isCelestiumHorseArmor(horse.getItemBySlot(EquipmentSlot.BODY));
    }

    static boolean isCelestiumHorseArmor(net.minecraft.world.item.ItemStack stack) {
        return isCelestiumHorseArmorClass(stack.getItem().getClass());
    }

    static boolean isCelestiumHorseArmorClass(Class<?> itemClass) {
        return CelestiumHorseArmorItem.class.isAssignableFrom(itemClass);
    }

    static double getBonusHealthAmount(double baseMaxHealth) {
        return baseMaxHealth;
    }

    static boolean shouldSyncModifierValue(Double currentValue, double expectedValue) {
        return currentValue == null || Math.abs(currentValue - expectedValue) > EPSILON;
    }

    static Double syncModifierValue(Double currentValue, double expectedValue) {
        return shouldSyncModifierValue(currentValue, expectedValue) ? expectedValue : currentValue;
    }

    static Double removeModifierValue(Double currentValue) {
        return null;
    }

    static boolean wasAtFullHealth(float currentHealth, float maxHealthBeforeChange) {
        return currentHealth >= maxHealthBeforeChange - FULL_HEALTH_EPSILON;
    }

    static float clampHealthToMax(float currentHealth, float maxHealth) {
        return Math.min(currentHealth, maxHealth);
    }

    static void syncTemporaryModifier(AttributeInstance attribute, Identifier modifierId, double value) {
        AttributeModifier currentModifier = attribute.getModifier(modifierId);
        if (currentModifier != null && Math.abs(currentModifier.amount() - value) <= EPSILON) {
            return;
        }

        attribute.removeModifier(modifierId);
        attribute.addTransientModifier(new AttributeModifier(modifierId, value, AttributeModifier.Operation.ADD_VALUE));
    }

    static void removeTemporaryModifier(AttributeInstance attribute, Identifier modifierId) {
        attribute.removeModifier(modifierId);
    }

    private static void applyBonusHealth(AbstractHorse horse) {
        AttributeInstance maxHealthAttribute = horse.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealthAttribute == null) {
            return;
        }

        boolean hasModifier = maxHealthAttribute.hasModifier(BONUS_HEALTH_MODIFIER_ID);
        float oldMaxHealth = horse.getMaxHealth();
        boolean wasFullHealth = wasAtFullHealth(horse.getHealth(), oldMaxHealth);
        syncTemporaryModifier(maxHealthAttribute, BONUS_HEALTH_MODIFIER_ID, getBonusHealthAmount(maxHealthAttribute.getBaseValue()));

        if (!hasModifier && wasFullHealth) {
            horse.setHealth(horse.getMaxHealth());
        }
    }

    private static void applyStepHeight(AbstractHorse horse) {
        AttributeInstance stepHeightAttribute = horse.getAttribute(Attributes.STEP_HEIGHT);
        if (stepHeightAttribute != null) {
            syncTemporaryModifier(stepHeightAttribute, STEP_HEIGHT_MODIFIER_ID, STEP_HEIGHT_BONUS);
        }
    }

    private static void removeArmorBuffs(AbstractHorse horse) {
        AttributeInstance maxHealthAttribute = horse.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealthAttribute != null && maxHealthAttribute.hasModifier(BONUS_HEALTH_MODIFIER_ID)) {
            removeTemporaryModifier(maxHealthAttribute, BONUS_HEALTH_MODIFIER_ID);
            horse.setHealth(clampHealthToMax(horse.getHealth(), horse.getMaxHealth()));
        }

        AttributeInstance stepHeightAttribute = horse.getAttribute(Attributes.STEP_HEIGHT);
        if (stepHeightAttribute != null) {
            removeTemporaryModifier(stepHeightAttribute, STEP_HEIGHT_MODIFIER_ID);
        }
    }

    private static void refreshSpeed(AbstractHorse horse) {
        MobEffectInstance currentSpeed = horse.getEffect(MobEffects.SPEED);
        if (currentSpeed == null || currentSpeed.getAmplifier() != SPEED_AMPLIFIER || currentSpeed.getDuration() <= SPEED_REFRESH_THRESHOLD_TICKS) {
            horse.addEffect(new MobEffectInstance(MobEffects.SPEED, SPEED_DURATION_TICKS, SPEED_AMPLIFIER, false, false, false));
        }
    }

    private static void freezeWaterAroundHorse(ServerLevel world, AbstractHorse horse) {
        if (!horse.onGround()) {
            return;
        }

        BlockState frostedIce = Blocks.FROSTED_ICE.defaultBlockState();
        BlockPos center = horse.blockPosition().below();
        BlockPos.MutableBlockPos abovePos = new BlockPos.MutableBlockPos();

        for (BlockPos targetPos : BlockPos.betweenClosed(center.offset(-FROST_WALKER_RADIUS, 0, -FROST_WALKER_RADIUS), center.offset(FROST_WALKER_RADIUS, 0, FROST_WALKER_RADIUS))) {
            if (targetPos.distToCenterSqr(horse.getX(), horse.getY(), horse.getZ()) > FROST_WALKER_RADIUS * FROST_WALKER_RADIUS) {
                continue;
            }

            abovePos.set(targetPos.getX(), targetPos.getY() + 1, targetPos.getZ());
            if (!world.getBlockState(abovePos).isAir()) {
                continue;
            }

            BlockState state = world.getBlockState(targetPos);
            if (!state.is(Blocks.WATER) || !state.getFluidState().isSource()) {
                continue;
            }

            if (!frostedIce.canSurvive(world, targetPos) || !world.isUnobstructed(frostedIce, targetPos, CollisionContext.empty())) {
                continue;
            }

            world.setBlockAndUpdate(targetPos, frostedIce);
            world.scheduleTick(targetPos, Blocks.FROSTED_ICE, Mth.nextInt(horse.getRandom(), FROST_WALKER_MIN_FREEZE_TICKS, FROST_WALKER_MAX_FREEZE_TICKS));
        }
    }
}
