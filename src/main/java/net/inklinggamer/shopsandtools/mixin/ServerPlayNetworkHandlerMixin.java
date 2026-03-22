package net.inklinggamer.shopsandtools.mixin;

import net.inklinggamer.shopsandtools.player.CelestiumLeggingsManager;
import net.minecraft.network.packet.c2s.play.UpdatePlayerAbilitiesC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {
    @Shadow
    public ServerPlayerEntity player;

    @Inject(method = "onUpdatePlayerAbilities", at = @At("HEAD"), cancellable = true)
    private void shopsandtools$handleCelestiumDoubleJump(UpdatePlayerAbilitiesC2SPacket packet, CallbackInfo ci) {
        if (CelestiumLeggingsManager.handleFlightToggle(this.player, packet)) {
            ci.cancel();
        }
    }
}
