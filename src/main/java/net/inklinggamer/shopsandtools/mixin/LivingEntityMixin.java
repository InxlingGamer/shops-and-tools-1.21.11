package net.inklinggamer.shopsandtools.mixin;

import net.inklinggamer.shopsandtools.item.ModItems;
import net.inklinggamer.shopsandtools.player.CelestiumBootsManager;
import net.inklinggamer.shopsandtools.player.CelestiumLeggingsManager;
import net.inklinggamer.shopsandtools.player.CelestiumSwordManager;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    private static final int CELESTIUM_FEATHER_FALLING_IMMUNITY_LEVEL = 5;

    @Inject(method = "damage", at = @At("RETURN"))
    private void shopsandtools$applyCelestiumRetaliation(ServerWorld world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ()) {
            CelestiumLeggingsManager.onPlayerDamaged((LivingEntity) (Object) this, source);
            shopsandtools$awardCelestiumRage(source);
        }
    }

    private void shopsandtools$awardCelestiumRage(DamageSource source) {
        Object self = this;
        if (!(self instanceof net.minecraft.entity.mob.MobEntity mob) || mob.isAlive()) {
            return;
        }

        if (source.getAttacker() instanceof ServerPlayerEntity player) {
            CelestiumSwordManager.onRageWeaponMobKilled(player);
        }
    }

    @Inject(method = "computeFallDamage", at = @At("RETURN"), cancellable = true)
    private void shopsandtools$grantCelestiumBootsFullFallProtection(double fallDistance, float damagePerDistance, CallbackInfoReturnable<Integer> cir) {
        Object self = this;
        if (!(self instanceof PlayerEntity player)) {
            return;
        }

        ItemStack boots = player.getEquippedStack(net.minecraft.entity.EquipmentSlot.FEET);
        if (!boots.isOf(ModItems.CELESTIUM_BOOTS)) {
            return;
        }

        Registry<Enchantment> enchantmentRegistry = player.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);
        Enchantment featherFallingValue = enchantmentRegistry.getValueOrThrow(Enchantments.FEATHER_FALLING);
        RegistryEntry<Enchantment> featherFalling = enchantmentRegistry.getEntry(featherFallingValue);
        if (EnchantmentHelper.getLevel(featherFalling, boots) >= CELESTIUM_FEATHER_FALLING_IMMUNITY_LEVEL) {
            cir.setReturnValue(0);
        }
    }

    @Inject(method = "isHoldingOntoLadder", at = @At("RETURN"), cancellable = true)
    private void shopsandtools$allowCelestiumWallClimbDescent(CallbackInfoReturnable<Boolean> cir) {
        Object self = this;
        if (cir.getReturnValueZ()
                && self instanceof PlayerEntity player
                && CelestiumBootsManager.shouldWallClimb(player)) {
            cir.setReturnValue(false);
        }
    }
}
