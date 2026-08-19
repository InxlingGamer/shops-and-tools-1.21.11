package net.inklinggamer.celestium.mixin.client;

import net.inklinggamer.celestium.client.CelestiumPickaxeClient;
import net.inklinggamer.celestium.client.CelestiumShovelClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public abstract class ClientPlayerInteractionManagerMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "startDestroyBlock", at = @At("HEAD"))
    private void celestium$trackCelestiumPickaxeFaceOnAttack(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        CelestiumPickaxeClient.onBreakingAttempt(pos, direction);
        CelestiumShovelClient.onBreakingAttempt(pos, direction);
    }

    @Inject(method = "continueDestroyBlock", at = @At("HEAD"))
    private void celestium$trackCelestiumPickaxeFaceWhileBreaking(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        CelestiumPickaxeClient.onBreakingAttempt(pos, direction);
        CelestiumShovelClient.onBreakingAttempt(pos, direction);
    }

    @Inject(method = "stopDestroyBlock", at = @At("HEAD"))
    private void celestium$clearCelestiumPickaxeBreaking(CallbackInfo ci) {
        CelestiumPickaxeClient.clearBreakingState();
        CelestiumShovelClient.clearBreakingState();
    }

    @Redirect(
            method = {"continueDestroyBlock", "lambda$startDestroyBlock$1"},
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/state/BlockState;getDestroyProgress(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)F"
            )
    )
    private float celestium$useSlowestAreaMiningDelta(BlockState state, Player player, BlockGetter world, BlockPos pos) {
        float vanillaDelta = state.getDestroyProgress(player, world, pos);
        float adjustedDelta = CelestiumPickaxeClient.getAreaMiningDelta(this.minecraft, pos, vanillaDelta);
        return CelestiumShovelClient.getAreaMiningDelta(this.minecraft, pos, adjustedDelta);
    }

    @Redirect(
            method = "lambda$continueDestroyBlock$1",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;destroyBlock(Lnet/minecraft/core/BlockPos;)Z"
            )
    )
    private boolean celestium$deferAreaMiningBreakPrediction(MultiPlayerGameMode interactionManager, BlockPos pos) {
        if (CelestiumPickaxeClient.shouldDeferBreakPrediction(this.minecraft, pos)) {
            CelestiumPickaxeClient.playDeferredBreakSound(this.minecraft, pos);
            return false;
        }

        if (CelestiumShovelClient.shouldDeferBreakPrediction(this.minecraft, pos)) {
            CelestiumShovelClient.playDeferredBreakSound(this.minecraft, pos);
            return false;
        }

        return interactionManager.destroyBlock(pos);
    }
}
