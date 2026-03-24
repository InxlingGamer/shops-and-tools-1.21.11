package net.inklinggamer.shopsandtools.mixin;

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
    private LivingEntity shopsandtools$celestiumSwordTarget;

    @Unique
    private float shopsandtools$celestiumSwordInitialCombinedHealth;

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
    private void shopsandtools$captureCelestiumSwordTarget(Entity target, CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        if (player.getEntityWorld().isClient() || !(player instanceof ServerPlayerEntity serverPlayer)) {
            this.shopsandtools$celestiumSwordTarget = null;
            return;
        }

        CelestiumSwordManager.beginSwordAttack(serverPlayer);
        if (!CelestiumSwordManager.isCelestiumSwordEquipped(player) || !(target instanceof LivingEntity livingTarget)) {
            this.shopsandtools$celestiumSwordTarget = null;
            return;
        }

        this.shopsandtools$celestiumSwordTarget = livingTarget;
        this.shopsandtools$celestiumSwordInitialCombinedHealth = livingTarget.getHealth() + livingTarget.getAbsorptionAmount();
    }

    @Inject(method = "attack", at = @At("RETURN"))
    private void shopsandtools$applyCelestiumSwordEffects(Entity target, CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        if (!(player instanceof ServerPlayerEntity serverPlayer)) {
            this.shopsandtools$celestiumSwordTarget = null;
            this.shopsandtools$celestiumSwordInitialCombinedHealth = 0.0F;
            return;
        }

        CelestiumSwordManager.endSwordAttack(serverPlayer);
        if (this.shopsandtools$celestiumSwordTarget == null) {
            this.shopsandtools$celestiumSwordInitialCombinedHealth = 0.0F;
            return;
        }

        LivingEntity livingTarget = this.shopsandtools$celestiumSwordTarget;
        float initialCombinedHealth = this.shopsandtools$celestiumSwordInitialCombinedHealth;
        float remainingCombinedHealth = Math.max(0.0F, livingTarget.getHealth() + livingTarget.getAbsorptionAmount());
        float dealtDamage = Math.max(0.0F, initialCombinedHealth - remainingCombinedHealth);

        this.shopsandtools$celestiumSwordTarget = null;
        this.shopsandtools$celestiumSwordInitialCombinedHealth = 0.0F;

        if (dealtDamage > 0.0F) {
            CelestiumSwordManager.onDirectSwordDamage(serverPlayer, dealtDamage);
        }
    }
}
