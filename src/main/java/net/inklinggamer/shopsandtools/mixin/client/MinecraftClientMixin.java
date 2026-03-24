package net.inklinggamer.shopsandtools.mixin.client;

import net.inklinggamer.shopsandtools.client.CelestiumPickaxeClient;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Inject(method = "doItemUse", at = @At("HEAD"), cancellable = true)
    private void shopsandtools$handleCelestiumPickaxeToggle(CallbackInfo ci) {
        if (CelestiumPickaxeClient.handleRightClickToggle((MinecraftClient) (Object) this)) {
            ci.cancel();
        }
    }
}
