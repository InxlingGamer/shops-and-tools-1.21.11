package net.inklinggamer.shopsandtools.entity;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

public final class WardenCombatManager {
    private static final float PROJECTILE_IMMUNITY_HEALTH_RATIO = 0.70F;
    private static final float EXTRA_MACE_DURABILITY_PER_DAMAGE = 3.0F;
    private static final float BLOCKED_PROJECTILE_SOUND_VOLUME = 1.0F;
    private static final float BLOCKED_PROJECTILE_SOUND_PITCH = 1.0F;

    private WardenCombatManager() {
    }

    public static void register() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register(WardenCombatManager::allowWardenDamage);
    }

    public static void onResolvedMaceHit(ServerPlayerEntity player, ItemStack weaponStack, boolean wardenTarget, float damageDealt) {
        if (!wardenTarget || damageDealt <= 0.0F || weaponStack.isEmpty() || !weaponStack.isOf(Items.MACE) || !weaponStack.isDamageable()) {
            return;
        }

        int extraDurability = calculateExtraMaceDurability(damageDealt);
        if (extraDurability <= 0) {
            return;
        }

        DirectDurabilityResult result = resolveDirectDurabilityPenalty(weaponStack.getDamage(), weaponStack.getMaxDamage(), extraDurability);
        if (result.breaksItem()) {
            player.equipStack(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            player.sendEquipmentBreakStatus(weaponStack.getItem(), EquipmentSlot.MAINHAND);
            return;
        }

        weaponStack.setDamage(result.resultingDamage());
    }

    static int calculateExtraMaceDurability(float damageDealt) {
        return Math.round(Math.max(0.0F, damageDealt) * EXTRA_MACE_DURABILITY_PER_DAMAGE);
    }

    static boolean shouldBlockProjectileDamage(float currentHealth, float maxHealth, float incomingDamage, boolean directProjectile) {
        if (!directProjectile || maxHealth <= 0.0F) {
            return false;
        }

        float thresholdHealth = maxHealth * PROJECTILE_IMMUNITY_HEALTH_RATIO;
        float clampedIncomingDamage = Math.max(0.0F, incomingDamage);
        return currentHealth < thresholdHealth || currentHealth - clampedIncomingDamage < thresholdHealth;
    }

    static DirectDurabilityResult resolveDirectDurabilityPenalty(int currentDamage, int maxDamage, int extraDurability) {
        int clampedPenalty = Math.max(0, extraDurability);
        if (maxDamage <= 0 || clampedPenalty == 0) {
            return new DirectDurabilityResult(Math.max(0, currentDamage), false);
        }

        int resultingDamage = Math.max(0, currentDamage) + clampedPenalty;
        if (resultingDamage >= maxDamage) {
            return new DirectDurabilityResult(0, true);
        }

        return new DirectDurabilityResult(resultingDamage, false);
    }

    private static boolean allowWardenDamage(LivingEntity entity, DamageSource source, float amount) {
        if (!(entity instanceof WardenEntity warden)
                || !shouldBlockProjectileDamage(warden.getHealth(), warden.getMaxHealth(), amount, source.getSource() instanceof ProjectileEntity)) {
            return true;
        }

        if (source.getAttacker() instanceof ServerPlayerEntity player) {
            player.networkHandler.sendPacket(new PlaySoundS2CPacket(
                    SoundEvents.ITEM_SHIELD_BLOCK,
                    SoundCategory.PLAYERS,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    BLOCKED_PROJECTILE_SOUND_VOLUME,
                    BLOCKED_PROJECTILE_SOUND_PITCH,
                    player.getRandom().nextLong()
            ));
        }

        return false;
    }

    static record DirectDurabilityResult(int resultingDamage, boolean breaksItem) {
    }
}
