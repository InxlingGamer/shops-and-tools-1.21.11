package net.inklinggamer.shopsandtools.mixin;

import net.inklinggamer.shopsandtools.player.CelestiumAxeManager;
import net.inklinggamer.shopsandtools.player.CelestiumPickaxeManager;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.world.BlockView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayDeque;
import java.util.Deque;

@Mixin(ServerPlayerInteractionManager.class)
public abstract class ServerPlayerInteractionManagerMixin {
    @Shadow
    @Final
    protected ServerPlayerEntity player;

    @org.spongepowered.asm.mixin.Unique
    private final Deque<shopsandtools$BrokenBlockSnapshot> shopsandtools$brokenBlockSnapshots = new ArrayDeque<>();

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

    @Redirect(
            method = "processBlockBreakingAction",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/BlockState;calcBlockBreakingDelta(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;)F"
            )
    )
    private float shopsandtools$useSlowestAreaMiningDelta(BlockState state, PlayerEntity player, BlockView world, BlockPos pos) {
        float vanillaDelta = state.calcBlockBreakingDelta(player, world, pos);
        return CelestiumPickaxeManager.getAreaMiningDelta(this.player, pos, vanillaDelta);
    }

    @Redirect(
            method = "continueMining",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/BlockState;calcBlockBreakingDelta(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;)F"
            )
    )
    private float shopsandtools$useSlowestAreaMiningDeltaWhileContinuing(BlockState state, PlayerEntity player, BlockView world, BlockPos pos) {
        float vanillaDelta = state.calcBlockBreakingDelta(player, world, pos);
        return CelestiumPickaxeManager.getAreaMiningDelta(this.player, pos, vanillaDelta);
    }

    @Inject(method = "tryBreakBlock", at = @At("HEAD"))
    private void shopsandtools$captureBrokenBlockContext(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        BlockState brokenState = this.player.getEntityWorld().getBlockState(pos);
        BlockEntity brokenBlockEntity = this.player.getEntityWorld().getBlockEntity(pos);
        ItemStack breakingTool = this.player.getMainHandStack().copy();
        this.shopsandtools$brokenBlockSnapshots.push(new shopsandtools$BrokenBlockSnapshot(pos.toImmutable(), brokenState, brokenBlockEntity, breakingTool));
    }

    @Inject(method = "tryBreakBlock", at = @At("RETURN"))
    private void shopsandtools$breakCelestiumPickaxeArea(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        shopsandtools$BrokenBlockSnapshot snapshot = this.shopsandtools$brokenBlockSnapshots.isEmpty()
                ? null
                : this.shopsandtools$brokenBlockSnapshots.pop();
        if (cir.getReturnValueZ()) {
            CelestiumPickaxeManager.onBlockBroken(this.player, (ServerPlayerInteractionManager) (Object) this, pos);
            if (snapshot != null && snapshot.pos().equals(pos)) {
                CelestiumAxeManager.onBlockBroken(this.player, pos, snapshot.state(), snapshot.blockEntity(), snapshot.tool());
            }
        } else if (snapshot != null && !snapshot.pos().equals(pos)) {
            this.shopsandtools$brokenBlockSnapshots.clear();
        }
    }

    @org.spongepowered.asm.mixin.Unique
    private record shopsandtools$BrokenBlockSnapshot(BlockPos pos, BlockState state, BlockEntity blockEntity, ItemStack tool) {
    }
}
