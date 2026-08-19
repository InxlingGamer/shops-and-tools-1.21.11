package net.inklinggamer.shopsandtools.mixin.client;

import net.inklinggamer.shopsandtools.client.CelestiumBootsClient;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityClientMixin {
    @Inject(method = "isClimbing", at = @At("RETURN"), cancellable = true)
    private void shopsandtools$allowSyncedLocalWallClimbing(CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() && CelestiumBootsClient.shouldUseSyncedWallClimb((PlayerEntity) (Object) this)) {
            cir.setReturnValue(true);
        }
    }
}
