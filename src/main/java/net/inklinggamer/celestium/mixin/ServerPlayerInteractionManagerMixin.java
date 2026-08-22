package net.inklinggamer.celestium.mixin;

import net.inklinggamer.celestium.player.CelestiumAxeManager;
import net.inklinggamer.celestium.player.CelestiumPickaxeManager;
import net.inklinggamer.celestium.player.CelestiumShovelManager;
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
    private final Deque<celestium$BrokenBlockSnapshot> celestium$brokenBlockSnapshots = new ArrayDeque<>();

    @Inject(method = "processBlockBreakingAction", at = @At("HEAD"))
    private void celestium$trackCelestiumPickaxeMiningFace(BlockPos pos, PlayerActionC2SPacket.Action action, Direction direction, int worldHeight, int sequence, CallbackInfo ci) {
        if (action == PlayerActionC2SPacket.Action.START_DESTROY_BLOCK) {
            CelestiumPickaxeManager.beginMiningSelection(this.player, pos, direction);
            CelestiumShovelManager.beginMiningSelection(this.player, pos, direction);
            return;
        }

        if (action == PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK) {
            CelestiumPickaxeManager.clearMiningSelection(this.player);
            CelestiumShovelManager.clearMiningSelection(this.player);
        }
    }

    @Redirect(
            method = "processBlockBreakingAction",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/BlockState;calcBlockBreakingDelta(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;)F"
            )
    )
    private float celestium$useSlowestAreaMiningDelta(BlockState state, PlayerEntity player, BlockView world, BlockPos pos) {
        float vanillaDelta = state.calcBlockBreakingDelta(player, world, pos);
        float adjustedDelta = CelestiumPickaxeManager.getAreaMiningDelta(this.player, pos, vanillaDelta);
        return CelestiumShovelManager.getAreaMiningDelta(this.player, pos, adjustedDelta);
    }

    @Redirect(
            method = "continueMining",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/BlockState;calcBlockBreakingDelta(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;)F"
            )
    )
    private float celestium$useSlowestAreaMiningDeltaWhileContinuing(BlockState state, PlayerEntity player, BlockView world, BlockPos pos) {
        float vanillaDelta = state.calcBlockBreakingDelta(player, world, pos);
        float adjustedDelta = CelestiumPickaxeManager.getAreaMiningDelta(this.player, pos, vanillaDelta);
        return CelestiumShovelManager.getAreaMiningDelta(this.player, pos, adjustedDelta);
    }

    @Inject(method = "tryBreakBlock", at = @At("HEAD"))
    private void celestium$captureBrokenBlockContext(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        BlockState brokenState = this.player.getEntityWorld().getBlockState(pos);
        BlockEntity brokenBlockEntity = this.player.getEntityWorld().getBlockEntity(pos);
        ItemStack breakingTool = this.player.getMainHandStack().copy();
        this.celestium$brokenBlockSnapshots.push(new celestium$BrokenBlockSnapshot(pos.toImmutable(), brokenState, brokenBlockEntity, breakingTool));
    }

    @Inject(method = "tryBreakBlock", at = @At("RETURN"))
    private void celestium$breakCelestiumPickaxeArea(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        celestium$BrokenBlockSnapshot snapshot = this.celestium$brokenBlockSnapshots.isEmpty()
                ? null
                : this.celestium$brokenBlockSnapshots.pop();
        if (cir.getReturnValueZ()) {
            CelestiumPickaxeManager.onBlockBroken(
                    this.player,
                    (ServerPlayerInteractionManager) (Object) this,
                    pos,
                    snapshot != null && snapshot.pos().equals(pos) ? snapshot.state() : this.player.getEntityWorld().getBlockState(pos),
                    snapshot != null && snapshot.pos().equals(pos) ? snapshot.tool() : this.player.getMainHandStack().copy()
            );
            CelestiumShovelManager.onBlockBroken(
                    this.player,
                    (ServerPlayerInteractionManager) (Object) this,
                    pos,
                    snapshot != null && snapshot.pos().equals(pos) ? snapshot.state() : null,
                    snapshot != null && snapshot.pos().equals(pos) ? snapshot.blockEntity() : null,
                    snapshot != null && snapshot.pos().equals(pos) ? snapshot.tool() : ItemStack.EMPTY
            );
            if (snapshot != null && snapshot.pos().equals(pos)) {
                CelestiumAxeManager.onBlockBroken(this.player, pos, snapshot.state(), snapshot.blockEntity(), snapshot.tool());
            }
        } else if (snapshot != null && !snapshot.pos().equals(pos)) {
            this.celestium$brokenBlockSnapshots.clear();
        }
    }

    @org.spongepowered.asm.mixin.Unique
    private record celestium$BrokenBlockSnapshot(BlockPos pos, BlockState state, BlockEntity blockEntity, ItemStack tool) {
    }
}
