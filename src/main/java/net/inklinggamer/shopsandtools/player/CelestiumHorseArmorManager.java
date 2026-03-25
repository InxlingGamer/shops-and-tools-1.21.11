package net.inklinggamer.shopsandtools.player;

import net.inklinggamer.shopsandtools.ShopsAndTools;
import net.inklinggamer.shopsandtools.item.CelestiumHorseArmorItem;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public final class CelestiumHorseArmorManager {
    private static final double EPSILON = 1.0E-6D;
    private static final float FULL_HEALTH_EPSILON = 0.001F;
    static final Identifier BONUS_HEALTH_MODIFIER_ID = Identifier.of(ShopsAndTools.MOD_ID, "celestium_horse_armor_bonus_health");
    static final Identifier STEP_HEIGHT_MODIFIER_ID = Identifier.of(ShopsAndTools.MOD_ID, "celestium_horse_armor_step_height");
    private static final double STEP_HEIGHT_BONUS = 2.0D;
    private static final int SPEED_DURATION_TICKS = 40;
    private static final int SPEED_REFRESH_THRESHOLD_TICKS = 10;
    private static final int SPEED_AMPLIFIER = 2;
    private static final int FROST_WALKER_RADIUS = 3;
    private static final int FROST_WALKER_MIN_FREEZE_TICKS = 60;
    private static final int FROST_WALKER_MAX_FREEZE_TICKS = 120;

    private CelestiumHorseArmorManager() {
    }

    public static void tickHorse(AbstractHorseEntity horse) {
        if (!(horse.getEntityWorld() instanceof ServerWorld world)) {
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

    public static boolean isCelestiumHorseArmorEquipped(AbstractHorseEntity horse) {
        return isCelestiumHorseArmor(horse.getEquippedStack(EquipmentSlot.BODY));
    }

    static boolean isCelestiumHorseArmor(net.minecraft.item.ItemStack stack) {
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

    static void syncTemporaryModifier(EntityAttributeInstance attribute, Identifier modifierId, double value) {
        EntityAttributeModifier currentModifier = attribute.getModifier(modifierId);
        if (currentModifier != null && Math.abs(currentModifier.value() - value) <= EPSILON) {
            return;
        }

        attribute.removeModifier(modifierId);
        attribute.addTemporaryModifier(new EntityAttributeModifier(modifierId, value, EntityAttributeModifier.Operation.ADD_VALUE));
    }

    static void removeTemporaryModifier(EntityAttributeInstance attribute, Identifier modifierId) {
        attribute.removeModifier(modifierId);
    }

    private static void applyBonusHealth(AbstractHorseEntity horse) {
        EntityAttributeInstance maxHealthAttribute = horse.getAttributeInstance(EntityAttributes.MAX_HEALTH);
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

    private static void applyStepHeight(AbstractHorseEntity horse) {
        EntityAttributeInstance stepHeightAttribute = horse.getAttributeInstance(EntityAttributes.STEP_HEIGHT);
        if (stepHeightAttribute != null) {
            syncTemporaryModifier(stepHeightAttribute, STEP_HEIGHT_MODIFIER_ID, STEP_HEIGHT_BONUS);
        }
    }

    private static void removeArmorBuffs(AbstractHorseEntity horse) {
        EntityAttributeInstance maxHealthAttribute = horse.getAttributeInstance(EntityAttributes.MAX_HEALTH);
        if (maxHealthAttribute != null && maxHealthAttribute.hasModifier(BONUS_HEALTH_MODIFIER_ID)) {
            removeTemporaryModifier(maxHealthAttribute, BONUS_HEALTH_MODIFIER_ID);
            horse.setHealth(clampHealthToMax(horse.getHealth(), horse.getMaxHealth()));
        }

        EntityAttributeInstance stepHeightAttribute = horse.getAttributeInstance(EntityAttributes.STEP_HEIGHT);
        if (stepHeightAttribute != null) {
            removeTemporaryModifier(stepHeightAttribute, STEP_HEIGHT_MODIFIER_ID);
        }
    }

    private static void refreshSpeed(AbstractHorseEntity horse) {
        StatusEffectInstance currentSpeed = horse.getStatusEffect(StatusEffects.SPEED);
        if (currentSpeed == null || currentSpeed.getAmplifier() != SPEED_AMPLIFIER || currentSpeed.getDuration() <= SPEED_REFRESH_THRESHOLD_TICKS) {
            horse.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, SPEED_DURATION_TICKS, SPEED_AMPLIFIER, false, false, false));
        }
    }

    private static void freezeWaterAroundHorse(ServerWorld world, AbstractHorseEntity horse) {
        if (!horse.isOnGround()) {
            return;
        }

        BlockState frostedIce = Blocks.FROSTED_ICE.getDefaultState();
        BlockPos center = horse.getBlockPos().down();
        BlockPos.Mutable abovePos = new BlockPos.Mutable();

        for (BlockPos targetPos : BlockPos.iterate(center.add(-FROST_WALKER_RADIUS, 0, -FROST_WALKER_RADIUS), center.add(FROST_WALKER_RADIUS, 0, FROST_WALKER_RADIUS))) {
            if (targetPos.getSquaredDistanceFromCenter(horse.getX(), horse.getY(), horse.getZ()) > FROST_WALKER_RADIUS * FROST_WALKER_RADIUS) {
                continue;
            }

            abovePos.set(targetPos.getX(), targetPos.getY() + 1, targetPos.getZ());
            if (!world.getBlockState(abovePos).isAir()) {
                continue;
            }

            BlockState state = world.getBlockState(targetPos);
            if (!state.isOf(Blocks.WATER) || !state.getFluidState().isStill()) {
                continue;
            }

            if (!frostedIce.canPlaceAt(world, targetPos) || !world.canPlace(frostedIce, targetPos, ShapeContext.absent())) {
                continue;
            }

            world.setBlockState(targetPos, frostedIce);
            world.scheduleBlockTick(targetPos, Blocks.FROSTED_ICE, MathHelper.nextInt(horse.getRandom(), FROST_WALKER_MIN_FREEZE_TICKS, FROST_WALKER_MAX_FREEZE_TICKS));
        }
    }
}
