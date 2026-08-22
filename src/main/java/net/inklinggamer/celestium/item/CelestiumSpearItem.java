package net.inklinggamer.celestium.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;

public final class CelestiumSpearItem extends Item {
    private static final double LUNGE_IMPULSE_PER_LEVEL = 0.458D;
    private static final float LUNGE_EXHAUSTION_PER_LEVEL = 4.0F;

    public CelestiumSpearItem(Settings settings) {
        super(settings);
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        stack.damage(1, attacker, attacker.getPreferredEquipmentSlot(stack));
        if (attacker instanceof PlayerEntity player && !player.getEntityWorld().isClient()) {
            celestium$applyLunge(player);
        }
        return true;
    }

    private static void celestium$applyLunge(PlayerEntity player) {
        if (player.hasVehicle() || player.isFallFlying() || player.isTouchingWater()) {
            return;
        }
        if (!player.isCreative() && player.getHungerManager().getFoodLevel() < 7) {
            return;
        }

        Vec3d look = player.getRotationVector();
        Vec3d horizontal = new Vec3d(look.x, 0.0D, look.z);
        if (horizontal.lengthSquared() < 1.0E-8D) {
            return;
        }

        double magnitude = LUNGE_IMPULSE_PER_LEVEL * CelestiumSpearHelper.LUNGE_LEVEL;
        player.addVelocity(horizontal.normalize().multiply(magnitude));
        player.velocityModified = true;
        player.addExhaustion(LUNGE_EXHAUSTION_PER_LEVEL * CelestiumSpearHelper.LUNGE_LEVEL);
        player.getEntityWorld().playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.ITEM_TRIDENT_RIPTIDE_3,
                SoundCategory.PLAYERS,
                1.0F,
                1.0F
        );
    }

    @Override
    public int getEnchantability() {
        return ModToolMaterials.CELESTIUM.getEnchantability();
    }

    @Override
    public boolean canRepair(ItemStack stack, ItemStack ingredient) {
        return ModToolMaterials.CELESTIUM.getRepairIngredient().test(ingredient) || super.canRepair(stack, ingredient);
    }
}
