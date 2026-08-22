package net.inklinggamer.celestium.mixin;

import net.inklinggamer.celestium.item.ModItems;
import net.inklinggamer.celestium.item.CelestiumSpearHelper;
import net.inklinggamer.celestium.player.CelestiumBootsManager;
import net.inklinggamer.celestium.player.CelestiumHorseArmorManager;
import net.inklinggamer.celestium.player.CelestiumLeggingsManager;
import net.inklinggamer.celestium.player.CelestiumSpearManager;
import net.inklinggamer.celestium.player.CelestiumSwordManager;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    private static final int CELESTIUM_FEATHER_FALLING_IMMUNITY_LEVEL = 5;

    @Inject(method = "isClimbing", at = @At("RETURN"), cancellable = true)
    private void celestium$allowWallClimbing(CallbackInfoReturnable<Boolean> cir) {
        Object self = this;
        if (!cir.getReturnValueZ()
                && self instanceof PlayerEntity player
                && !player.getEntityWorld().isClient()
                && CelestiumBootsManager.shouldWallClimb(player)) {
            cir.setReturnValue(true);
        }
    }

    @Redirect(
            method = "tickFallFlying",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isOf(Lnet/minecraft/item/Item;)Z")
    )
    private boolean celestium$allowCelestiumElytraFlight(ItemStack stack, Item expectedItem) {
        return stack.isOf(expectedItem)
                || (expectedItem == Items.ELYTRA && stack.isOf(ModItems.CELESTIUM_ELYTRA_CHESTPLATE));
    }

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void celestium$grantCelestiumHorseArmorBurnImmunity(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        Object self = this;
        if (self instanceof AbstractHorseEntity horse
                && CelestiumHorseArmorManager.isCelestiumHorseArmorEquipped(horse)
                && source.isIn(DamageTypeTags.BURN_FROM_STEPPING)
                && !source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "damage", at = @At("RETURN"))
    private void celestium$applyCelestiumRetaliation(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ()) {
            CelestiumLeggingsManager.onPlayerDamaged((LivingEntity) (Object) this, source);
            celestium$applyCelestiumSpearEffects(source);
            celestium$awardCelestiumRage(source);
        }
    }

    private void celestium$applyCelestiumSpearEffects(DamageSource source) {
        Object self = this;
        if (!(self instanceof LivingEntity victim) || source.getAttacker() == victim) {
            return;
        }

        if (source.getAttacker() instanceof ServerPlayerEntity player && CelestiumSpearHelper.isCelestiumSpearEquipped(player)) {
            CelestiumSpearManager.onDirectSpearDamage(player, victim, 1.0F);
        }
    }

    private void celestium$awardCelestiumRage(DamageSource source) {
        Object self = this;
        if (!(self instanceof net.minecraft.entity.mob.MobEntity mob) || mob.isAlive()) {
            return;
        }

        if (source.getAttacker() instanceof ServerPlayerEntity player) {
            CelestiumSwordManager.onRageWeaponMobKilled(player);
        }
    }

    @Inject(method = "computeFallDamage", at = @At("RETURN"), cancellable = true)
    private void celestium$grantCelestiumBootsFullFallProtection(float fallDistance, float damagePerDistance, CallbackInfoReturnable<Integer> cir) {
        Object self = this;
        if (self instanceof AbstractHorseEntity horse && CelestiumHorseArmorManager.isCelestiumHorseArmorEquipped(horse)) {
            cir.setReturnValue(0);
            return;
        }

        if (!(self instanceof PlayerEntity player)) {
            return;
        }

        if (player.getVehicle() instanceof AbstractHorseEntity horse && CelestiumHorseArmorManager.isCelestiumHorseArmorEquipped(horse)) {
            cir.setReturnValue(0);
            return;
        }

        ItemStack boots = player.getEquippedStack(net.minecraft.entity.EquipmentSlot.FEET);
        if (!boots.isOf(ModItems.CELESTIUM_BOOTS)) {
            return;
        }

        Registry<Enchantment> enchantmentRegistry = player.getRegistryManager().get(RegistryKeys.ENCHANTMENT);
        Enchantment featherFallingValue = enchantmentRegistry.get(Enchantments.FEATHER_FALLING);
        RegistryEntry<Enchantment> featherFalling = enchantmentRegistry.getEntry(featherFallingValue);
        if (EnchantmentHelper.getLevel(featherFalling, boots) >= CELESTIUM_FEATHER_FALLING_IMMUNITY_LEVEL) {
            cir.setReturnValue(0);
        }
    }

    @Inject(method = "isHoldingOntoLadder", at = @At("RETURN"), cancellable = true)
    private void celestium$allowCelestiumWallClimbDescent(CallbackInfoReturnable<Boolean> cir) {
        Object self = this;
        if (cir.getReturnValueZ()
                && self instanceof PlayerEntity player
                && !player.getEntityWorld().isClient()
                && CelestiumBootsManager.shouldWallClimb(player)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    private void celestium$preventStunnedMobMovement(Vec3d movementInput, CallbackInfo ci) {
        Object self = this;
        if (!(self instanceof MobEntity mob) || !CelestiumSpearManager.isStunned(mob)) {
            return;
        }

        mob.getNavigation().stop();
        mob.setVelocity(Vec3d.ZERO);
        ci.cancel();
    }
}
