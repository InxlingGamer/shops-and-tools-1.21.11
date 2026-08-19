package net.inklinggamer.shopsandtools.mixin.client;

import net.inklinggamer.shopsandtools.client.CelestiumPickaxeClient;
import net.inklinggamer.shopsandtools.client.CelestiumShovelClient;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class MinecraftClientMixin {
    @Inject(method = "startUseItem", at = @At("HEAD"), cancellable = true)
    private void shopsandtools$handleCelestiumPickaxeToggle(CallbackInfo ci) {
        if (CelestiumPickaxeClient.handleRightClickToggle((Minecraft) (Object) this)) {
            ci.cancel();
            return;
        }

        if (CelestiumShovelClient.handleRightClickToggle((Minecraft) (Object) this)) {
            ci.cancel();
        }
    }

    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void shopsandtools$handleCelestiumShovelSlam(CallbackInfoReturnable<Boolean> cir) {
        if (CelestiumShovelClient.handleGroundSlamAttempt((Minecraft) (Object) this)) {
            cir.setReturnValue(false);
        }
    }
}
