package net.inklinggamer.shopsandtools.mixin.client;

import net.inklinggamer.shopsandtools.client.CelestiumPickaxeClient;
import net.inklinggamer.shopsandtools.client.CelestiumShovelClient;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class ClientPlayerInteractionManagerMixin {
    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "attackBlock", at = @At("HEAD"))
    private void shopsandtools$trackCelestiumPickaxeFaceOnAttack(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        CelestiumPickaxeClient.onBreakingAttempt(pos, direction);
        CelestiumShovelClient.onBreakingAttempt(pos, direction);
    }

    @Inject(method = "updateBlockBreakingProgress", at = @At("HEAD"))
    private void shopsandtools$trackCelestiumPickaxeFaceWhileBreaking(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        CelestiumPickaxeClient.onBreakingAttempt(pos, direction);
        CelestiumShovelClient.onBreakingAttempt(pos, direction);
    }

    @Inject(method = "cancelBlockBreaking", at = @At("HEAD"))
    private void shopsandtools$clearCelestiumPickaxeBreaking(CallbackInfo ci) {
        CelestiumPickaxeClient.clearBreakingState();
        CelestiumShovelClient.clearBreakingState();
    }

    @Redirect(
            method = {"attackBlock", "updateBlockBreakingProgress", "method_41930"},
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/BlockState;calcBlockBreakingDelta(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;)F"
            )
    )
    private float shopsandtools$useSlowestAreaMiningDelta(BlockState state, PlayerEntity player, BlockView world, BlockPos pos) {
        float vanillaDelta = state.calcBlockBreakingDelta(player, world, pos);
        float adjustedDelta = CelestiumPickaxeClient.getAreaMiningDelta(this.client, pos, vanillaDelta);
        return CelestiumShovelClient.getAreaMiningDelta(this.client, pos, adjustedDelta);
    }

    @Redirect(
            method = "method_41932",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;breakBlock(Lnet/minecraft/util/math/BlockPos;)Z"
            )
    )
    private boolean shopsandtools$deferAreaMiningBreakPrediction(ClientPlayerInteractionManager interactionManager, BlockPos pos) {
        if (CelestiumPickaxeClient.shouldDeferBreakPrediction(this.client, pos)) {
            CelestiumPickaxeClient.playDeferredBreakSound(this.client, pos);
            return false;
        }

        if (CelestiumShovelClient.shouldDeferBreakPrediction(this.client, pos)) {
            CelestiumShovelClient.playDeferredBreakSound(this.client, pos);
            return false;
        }

        return interactionManager.breakBlock(pos);
    }
}
