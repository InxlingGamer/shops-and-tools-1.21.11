package net.inklinggamer.celestium.mixin.client;

import net.inklinggamer.celestium.client.CelestiumBootsClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class PlayerEntityClientMixin {
    @Inject(method = "isClimbing", at = @At("RETURN"), cancellable = true)
    private void celestium$allowSyncedLocalWallClimbing(CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ()
                && (Object) this instanceof PlayerEntity player
                && CelestiumBootsClient.shouldUseSyncedWallClimb(player)) {
            cir.setReturnValue(true);
        }
    }
}
