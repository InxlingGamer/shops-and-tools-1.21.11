package net.inklinggamer.celestium.mixin.client;

import net.inklinggamer.celestium.client.CelestiumBootsClient;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(net.minecraft.world.entity.LivingEntity.class)
public abstract class LivingEntityClientMixin {
    @Inject(method = "isSuppressingSlidingDownLadder", at = @At("RETURN"), cancellable = true)
    private void celestium$allowSyncedLocalWallClimbDescent(CallbackInfoReturnable<Boolean> cir) {
        Object self = this;
        if (cir.getReturnValueZ()
                && self instanceof Player player
                && CelestiumBootsClient.shouldUseSyncedWallClimb(player)) {
            cir.setReturnValue(false);
        }
    }
}
