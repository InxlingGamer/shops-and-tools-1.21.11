package net.inklinggamer.shopsandtools.mixin.client;

import net.inklinggamer.shopsandtools.client.CelestiumPickaxeClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class ClientPlayerInteractionManagerMixin {
    @Inject(method = "attackBlock", at = @At("HEAD"))
    private void shopsandtools$trackCelestiumPickaxeFaceOnAttack(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        CelestiumPickaxeClient.onBreakingAttempt(pos, direction);
    }

    @Inject(method = "updateBlockBreakingProgress", at = @At("HEAD"))
    private void shopsandtools$trackCelestiumPickaxeFaceWhileBreaking(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        CelestiumPickaxeClient.onBreakingAttempt(pos, direction);
    }

    @Inject(method = "cancelBlockBreaking", at = @At("HEAD"))
    private void shopsandtools$clearCelestiumPickaxeBreaking(CallbackInfo ci) {
        CelestiumPickaxeClient.clearBreakingState();
    }
}
