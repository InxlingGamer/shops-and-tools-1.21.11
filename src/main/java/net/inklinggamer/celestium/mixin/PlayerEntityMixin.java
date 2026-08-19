package net.inklinggamer.celestium.mixin;

import net.inklinggamer.celestium.player.CelestiumAxeManager;
import net.inklinggamer.celestium.player.CelestiumBootsManager;
import net.inklinggamer.celestium.player.CelestiumLeggingsManager;
import net.inklinggamer.celestium.player.CelestiumSwordManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerEntityMixin {
    @Unique
    private LivingEntity celestium$celestiumWeaponTarget;

    @Unique
    private float celestium$celestiumWeaponInitialCombinedHealth;

    @Unique
    private float celestium$celestiumWeaponAttackCooldownProgress;

    @Unique
    private boolean celestium$celestiumSwordAttack;

    @Unique
    private boolean celestium$celestiumAxeAttack;

    @Inject(method = "onClimbable", at = @At("RETURN"), cancellable = true)
    private void celestium$allowWallClimbing(CallbackInfoReturnable<Boolean> cir) {
        Player player = (Player) (Object) this;
        if (!player.level().isClientSide()
                && !cir.getReturnValueZ()
                && CelestiumBootsManager.shouldWallClimb(player)) {
            cir.setReturnValue(true);
        }
    }

    @Redirect(
            method = "causeFallDamage",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/entity/player/Abilities;mayfly:Z",
                    opcode = Opcodes.GETFIELD
            )
    )
    private boolean celestium$restoreFallDamageForCelestiumLeggings(Abilities abilities) {
        if (abilities.mayfly && CelestiumLeggingsManager.hasActiveFlightPermission((Player) (Object) this)) {
            return false;
        }

        return abilities.mayfly;
    }

    @Inject(method = "attack", at = @At("HEAD"))
    private void celestium$captureCelestiumWeaponTarget(Entity target, CallbackInfo ci) {
        Player player = (Player) (Object) this;
        if (player.level().isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
            celestium$clearCelestiumAttackState();
            return;
        }

        this.celestium$celestiumSwordAttack = CelestiumSwordManager.isCelestiumSwordEquipped(player);
        this.celestium$celestiumAxeAttack = CelestiumAxeManager.isCelestiumAxeEquipped(player);
        this.celestium$celestiumWeaponAttackCooldownProgress = player.getAttackStrengthScale(0.5F);
        CelestiumSwordManager.beginRageWeaponAttack(serverPlayer);

        if (!(this.celestium$celestiumSwordAttack || this.celestium$celestiumAxeAttack)
                || !(target instanceof LivingEntity livingTarget)) {
            this.celestium$celestiumWeaponTarget = null;
            return;
        }

        this.celestium$celestiumWeaponTarget = livingTarget;
        this.celestium$celestiumWeaponInitialCombinedHealth = livingTarget.getHealth() + livingTarget.getAbsorptionAmount();
    }

    @Inject(method = "attack", at = @At("RETURN"))
    private void celestium$applyCelestiumWeaponEffects(Entity target, CallbackInfo ci) {
        Player player = (Player) (Object) this;
        if (!(player instanceof ServerPlayer serverPlayer)) {
            celestium$clearCelestiumAttackState();
            return;
        }

        CelestiumSwordManager.endRageWeaponAttack(serverPlayer);
        LivingEntity livingTarget = this.celestium$celestiumWeaponTarget;
        float attackCooldownProgress = this.celestium$celestiumWeaponAttackCooldownProgress;
        boolean swordAttack = this.celestium$celestiumSwordAttack;
        boolean axeAttack = this.celestium$celestiumAxeAttack;

        if (livingTarget == null) {
            celestium$clearCelestiumAttackState();
            return;
        }

        float initialCombinedHealth = this.celestium$celestiumWeaponInitialCombinedHealth;
        float remainingCombinedHealth = Math.max(0.0F, livingTarget.getHealth() + livingTarget.getAbsorptionAmount());
        float dealtDamage = Math.max(0.0F, initialCombinedHealth - remainingCombinedHealth);

        celestium$clearCelestiumAttackState();

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
    private void celestium$clearCelestiumAttackState() {
        this.celestium$celestiumWeaponTarget = null;
        this.celestium$celestiumWeaponInitialCombinedHealth = 0.0F;
        this.celestium$celestiumWeaponAttackCooldownProgress = 0.0F;
        this.celestium$celestiumSwordAttack = false;
        this.celestium$celestiumAxeAttack = false;
    }
}
