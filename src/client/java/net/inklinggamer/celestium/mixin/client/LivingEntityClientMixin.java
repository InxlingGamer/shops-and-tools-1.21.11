package net.inklinggamer.celestium.mixin.client;

import net.inklinggamer.celestium.client.CelestiumBootsClient;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(net.minecraft.entity.LivingEntity.class)
public abstract class LivingEntityClientMixin {
    @Inject(method = "isHoldingOntoLadder", at = @At("RETURN"), cancellable = true)
    private void celestium$allowSyncedLocalWallClimbDescent(CallbackInfoReturnable<Boolean> cir) {
        Object self = this;
        if (cir.getReturnValueZ()
                && self instanceof PlayerEntity player
                && CelestiumBootsClient.shouldUseSyncedWallClimb(player)) {
            cir.setReturnValue(false);
        }
    }
}
