package net.inklinggamer.shopsandtools.mixin;

import net.inklinggamer.shopsandtools.player.CelestiumAxeManager;
import net.inklinggamer.shopsandtools.player.CelestiumBootsManager;
import net.inklinggamer.shopsandtools.player.CelestiumLeggingsManager;
import net.inklinggamer.shopsandtools.player.CelestiumSwordManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {
    @Unique
    private LivingEntity shopsandtools$celestiumWeaponTarget;

    @Unique
    private float shopsandtools$celestiumWeaponInitialCombinedHealth;

    @Unique
    private float shopsandtools$celestiumWeaponAttackCooldownProgress;

    @Unique
    private boolean shopsandtools$celestiumSwordAttack;

    @Unique
    private boolean shopsandtools$celestiumAxeAttack;

    @Inject(method = "isClimbing", at = @At("RETURN"), cancellable = true)
    private void shopsandtools$allowWallClimbing(CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() && CelestiumBootsManager.shouldWallClimb((PlayerEntity) (Object) this)) {
            cir.setReturnValue(true);
        }
    }

    @Redirect(
            method = "handleFallDamage",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/entity/player/PlayerAbilities;allowFlying:Z",
                    opcode = Opcodes.GETFIELD
            )
    )
    private boolean shopsandtools$restoreFallDamageForCelestiumLeggings(PlayerAbilities abilities) {
        if (abilities.allowFlying && CelestiumLeggingsManager.hasActiveFlightPermission((PlayerEntity) (Object) this)) {
            return false;
        }

        return abilities.allowFlying;
    }

    @Inject(method = "attack", at = @At("HEAD"))
    private void shopsandtools$captureCelestiumWeaponTarget(Entity target, CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        if (player.getEntityWorld().isClient() || !(player instanceof ServerPlayerEntity serverPlayer)) {
            shopsandtools$clearCelestiumAttackState();
            return;
        }

        this.shopsandtools$celestiumSwordAttack = CelestiumSwordManager.isCelestiumSwordEquipped(player);
        this.shopsandtools$celestiumAxeAttack = CelestiumAxeManager.isCelestiumAxeEquipped(player);
        this.shopsandtools$celestiumWeaponAttackCooldownProgress = player.getAttackCooldownProgress(0.5F);
        CelestiumSwordManager.beginRageWeaponAttack(serverPlayer);

        if (!(this.shopsandtools$celestiumSwordAttack || this.shopsandtools$celestiumAxeAttack)
                || !(target instanceof LivingEntity livingTarget)) {
            this.shopsandtools$celestiumWeaponTarget = null;
            return;
        }

        this.shopsandtools$celestiumWeaponTarget = livingTarget;
        this.shopsandtools$celestiumWeaponInitialCombinedHealth = livingTarget.getHealth() + livingTarget.getAbsorptionAmount();
    }

    @Inject(method = "attack", at = @At("RETURN"))
    private void shopsandtools$applyCelestiumWeaponEffects(Entity target, CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        if (!(player instanceof ServerPlayerEntity serverPlayer)) {
            shopsandtools$clearCelestiumAttackState();
            return;
        }

        CelestiumSwordManager.endRageWeaponAttack(serverPlayer);
        LivingEntity livingTarget = this.shopsandtools$celestiumWeaponTarget;
        float attackCooldownProgress = this.shopsandtools$celestiumWeaponAttackCooldownProgress;
        boolean swordAttack = this.shopsandtools$celestiumSwordAttack;
        boolean axeAttack = this.shopsandtools$celestiumAxeAttack;

        if (livingTarget == null) {
            shopsandtools$clearCelestiumAttackState();
            return;
        }

        float initialCombinedHealth = this.shopsandtools$celestiumWeaponInitialCombinedHealth;
        float remainingCombinedHealth = Math.max(0.0F, livingTarget.getHealth() + livingTarget.getAbsorptionAmount());
        float dealtDamage = Math.max(0.0F, initialCombinedHealth - remainingCombinedHealth);

        shopsandtools$clearCelestiumAttackState();

        if (dealtDamage > 0.0F) {
            if (swordAttack) {
                CelestiumSwordManager.onDirectSwordDamage(serverPlayer, dealtDamage);
            }

            if (axeAttack) {
                CelestiumAxeManager.onDirectAxeDamage(serverPlayer, livingTarget, attackCooldownProgress, dealtDamage);
            }
        }
    }

    @Unique
    private void shopsandtools$clearCelestiumAttackState() {
        this.shopsandtools$celestiumWeaponTarget = null;
        this.shopsandtools$celestiumWeaponInitialCombinedHealth = 0.0F;
        this.shopsandtools$celestiumWeaponAttackCooldownProgress = 0.0F;
        this.shopsandtools$celestiumSwordAttack = false;
        this.shopsandtools$celestiumAxeAttack = false;
    }
}
