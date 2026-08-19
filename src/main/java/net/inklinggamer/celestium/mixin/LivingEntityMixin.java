package net.inklinggamer.celestium.mixin;

import net.inklinggamer.celestium.item.ModItems;
import net.inklinggamer.celestium.item.CelestiumSpearHelper;
import net.inklinggamer.celestium.player.CelestiumBootsManager;
import net.inklinggamer.celestium.player.CelestiumHorseArmorManager;
import net.inklinggamer.celestium.player.CelestiumLeggingsManager;
import net.inklinggamer.celestium.player.CelestiumSpearManager;
import net.inklinggamer.celestium.player.CelestiumSwordManager;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    private static final int CELESTIUM_FEATHER_FALLING_IMMUNITY_LEVEL = 5;

    @Inject(method = "hurtServer", at = @At("HEAD"), cancellable = true)
    private void celestium$grantCelestiumHorseArmorBurnImmunity(ServerLevel world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        Object self = this;
        if (self instanceof AbstractHorse horse
                && CelestiumHorseArmorManager.isCelestiumHorseArmorEquipped(horse)
                && source.is(DamageTypeTags.BURN_FROM_STEPPING)
                && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "hurtServer", at = @At("RETURN"))
    private void celestium$applyCelestiumRetaliation(ServerLevel world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ()) {
            CelestiumLeggingsManager.onPlayerDamaged((LivingEntity) (Object) this, source);
            celestium$applyCelestiumSpearEffects(source);
            celestium$awardCelestiumRage(source);
        }
    }

    private void celestium$applyCelestiumSpearEffects(DamageSource source) {
        Object self = this;
        if (!(self instanceof LivingEntity victim) || source.getEntity() == victim) {
            return;
        }

        if (source.getEntity() instanceof ServerPlayer player && CelestiumSpearHelper.isCelestiumSpearEquipped(player)) {
            CelestiumSpearManager.onDirectSpearDamage(player, victim, 1.0F);
        }
    }

    private void celestium$awardCelestiumRage(DamageSource source) {
        Object self = this;
        if (!(self instanceof net.minecraft.world.entity.Mob mob) || mob.isAlive()) {
            return;
        }

        if (source.getEntity() instanceof ServerPlayer player) {
            CelestiumSwordManager.onRageWeaponMobKilled(player);
        }
    }

    @Inject(method = "calculateFallDamage", at = @At("RETURN"), cancellable = true)
    private void celestium$grantCelestiumBootsFullFallProtection(double fallDistance, float damagePerDistance, CallbackInfoReturnable<Integer> cir) {
        Object self = this;
        if (self instanceof AbstractHorse horse && CelestiumHorseArmorManager.isCelestiumHorseArmorEquipped(horse)) {
            cir.setReturnValue(0);
            return;
        }

        if (!(self instanceof Player player)) {
            return;
        }

        if (player.getVehicle() instanceof AbstractHorse horse && CelestiumHorseArmorManager.isCelestiumHorseArmorEquipped(horse)) {
            cir.setReturnValue(0);
            return;
        }

        ItemStack boots = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.FEET);
        if (!boots.is(ModItems.CELESTIUM_BOOTS)) {
            return;
        }

        Registry<Enchantment> enchantmentRegistry = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        Enchantment featherFallingValue = enchantmentRegistry.getValueOrThrow(Enchantments.FEATHER_FALLING);
        Holder<Enchantment> featherFalling = enchantmentRegistry.wrapAsHolder(featherFallingValue);
        if (EnchantmentHelper.getItemEnchantmentLevel(featherFalling, boots) >= CELESTIUM_FEATHER_FALLING_IMMUNITY_LEVEL) {
            cir.setReturnValue(0);
        }
    }

    @Inject(method = "isSuppressingSlidingDownLadder", at = @At("RETURN"), cancellable = true)
    private void celestium$allowCelestiumWallClimbDescent(CallbackInfoReturnable<Boolean> cir) {
        Object self = this;
        if (cir.getReturnValueZ()
                && self instanceof Player player
                && !player.level().isClientSide()
                && CelestiumBootsManager.shouldWallClimb(player)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    private void celestium$preventStunnedMobMovement(Vec3 movementInput, CallbackInfo ci) {
        Object self = this;
        if (!(self instanceof Mob mob) || !CelestiumSpearManager.isStunned(mob)) {
            return;
        }

        mob.getNavigation().stop();
        mob.setDeltaMovement(Vec3.ZERO);
        ci.cancel();
    }
}
