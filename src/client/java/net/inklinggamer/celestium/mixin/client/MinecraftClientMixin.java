package net.inklinggamer.celestium.mixin.client;

import net.inklinggamer.celestium.client.CelestiumPickaxeClient;
import net.inklinggamer.celestium.client.CelestiumShovelClient;
import net.inklinggamer.celestium.client.CelestiumSpearClient;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Inject(method = "doItemUse", at = @At("HEAD"), cancellable = true)
    private void celestium$handleCelestiumPickaxeToggle(CallbackInfo ci) {
        if (CelestiumPickaxeClient.handleRightClickToggle((MinecraftClient) (Object) this)) {
            ci.cancel();
            return;
        }

        if (CelestiumShovelClient.handleRightClickToggle((MinecraftClient) (Object) this)) {
            ci.cancel();
        }
    }

    @Inject(method = "doAttack", at = @At("HEAD"), cancellable = true)
    private void celestium$handleCelestiumShovelSlam(CallbackInfoReturnable<Boolean> cir) {
        if (CelestiumShovelClient.handleGroundSlamAttempt((MinecraftClient) (Object) this)) {
            cir.setReturnValue(false);
            return;
        }

        if (CelestiumSpearClient.handleExtendedAttack((MinecraftClient) (Object) this)) {
            cir.setReturnValue(true);
        }
    }
}
