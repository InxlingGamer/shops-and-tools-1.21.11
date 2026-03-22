package net.inklinggamer.shopsandtools.mixin;

import net.inklinggamer.shopsandtools.player.CelestiumBootsManager;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {
    @Inject(method = "isClimbing", at = @At("RETURN"), cancellable = true)
    private void shopsandtools$allowWallClimbing(CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() && CelestiumBootsManager.shouldWallClimb((PlayerEntity) (Object) this)) {
            cir.setReturnValue(true);
        }
    }
}
