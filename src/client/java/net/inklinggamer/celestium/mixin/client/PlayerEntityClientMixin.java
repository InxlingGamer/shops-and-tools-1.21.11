package net.inklinggamer.celestium.mixin.client;

import net.inklinggamer.celestium.client.CelestiumBootsClient;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerEntityClientMixin {
    @Inject(method = "onClimbable", at = @At("RETURN"), cancellable = true)
    private void celestium$allowSyncedLocalWallClimbing(CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() && CelestiumBootsClient.shouldUseSyncedWallClimb((Player) (Object) this)) {
            cir.setReturnValue(true);
        }
    }
}
