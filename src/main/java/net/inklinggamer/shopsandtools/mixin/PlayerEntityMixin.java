package net.inklinggamer.shopsandtools.mixin;

import net.inklinggamer.shopsandtools.player.CelestiumBootsManager;
import net.inklinggamer.shopsandtools.player.CelestiumLeggingsManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.entity.player.PlayerEntity;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {
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
}
