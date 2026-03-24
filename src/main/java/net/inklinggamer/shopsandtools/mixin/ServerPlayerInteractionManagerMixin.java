package net.inklinggamer.shopsandtools.mixin;

import net.inklinggamer.shopsandtools.player.CelestiumPickaxeManager;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerInteractionManager.class)
public abstract class ServerPlayerInteractionManagerMixin {
    @Shadow
    @Final
    protected ServerPlayerEntity player;

    @Inject(method = "processBlockBreakingAction", at = @At("HEAD"))
    private void shopsandtools$trackCelestiumPickaxeMiningFace(BlockPos pos, PlayerActionC2SPacket.Action action, Direction direction, int worldHeight, int sequence, CallbackInfo ci) {
        if (action == PlayerActionC2SPacket.Action.START_DESTROY_BLOCK) {
            CelestiumPickaxeManager.beginMiningSelection(this.player, pos, direction);
            return;
        }

        if (action == PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK) {
            CelestiumPickaxeManager.clearMiningSelection(this.player);
        }
    }

    @Inject(method = "tryBreakBlock", at = @At("RETURN"))
    private void shopsandtools$breakCelestiumPickaxeArea(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ()) {
            CelestiumPickaxeManager.onBlockBroken(this.player, (ServerPlayerInteractionManager) (Object) this, pos);
        }
    }
}
