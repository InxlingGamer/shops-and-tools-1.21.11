package net.inklinggamer.shopsandtools.mixin;

import net.inklinggamer.shopsandtools.player.CelestiumAxeManager;
import net.inklinggamer.shopsandtools.player.CelestiumPickaxeManager;
import net.inklinggamer.shopsandtools.player.CelestiumShovelManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
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

@Mixin(ServerPlayerGameMode.class)
public abstract class ServerPlayerInteractionManagerMixin {
    @Shadow
    @Final
    protected ServerPlayer player;

    @org.spongepowered.asm.mixin.Unique
    private final Deque<BrokenBlockSnapshot> shopsandtools$brokenBlockSnapshots = new ArrayDeque<>();

    @Inject(method = "handleBlockBreakAction", at = @At("HEAD"))
    private void shopsandtools$trackCelestiumPickaxeMiningFace(BlockPos pos, ServerboundPlayerActionPacket.Action action, Direction direction, int worldHeight, int sequence, CallbackInfo ci) {
        if (action == ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK) {
            CelestiumPickaxeManager.beginMiningSelection(this.player, pos, direction);
            CelestiumShovelManager.beginMiningSelection(this.player, pos, direction);
            return;
        }

        if (action == ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK) {
            CelestiumPickaxeManager.clearMiningSelection(this.player);
            CelestiumShovelManager.clearMiningSelection(this.player);
        }
    }

    @Redirect(
            method = "handleBlockBreakAction",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/state/BlockState;getDestroyProgress(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)F"
            )
    )
    private float shopsandtools$useSlowestAreaMiningDelta(BlockState state, Player player, BlockGetter world, BlockPos pos) {
        float vanillaDelta = state.getDestroyProgress(player, world, pos);
        float adjustedDelta = CelestiumPickaxeManager.getAreaMiningDelta(this.player, pos, vanillaDelta);
        return CelestiumShovelManager.getAreaMiningDelta(this.player, pos, adjustedDelta);
    }

    @Redirect(
            method = "incrementDestroyProgress",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/state/BlockState;getDestroyProgress(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)F"
            )
    )
    private float shopsandtools$useSlowestAreaMiningDeltaWhileContinuing(BlockState state, Player player, BlockGetter world, BlockPos pos) {
        float vanillaDelta = state.getDestroyProgress(player, world, pos);
        float adjustedDelta = CelestiumPickaxeManager.getAreaMiningDelta(this.player, pos, vanillaDelta);
        return CelestiumShovelManager.getAreaMiningDelta(this.player, pos, adjustedDelta);
    }

    @Inject(method = "destroyBlock", at = @At("HEAD"))
    private void shopsandtools$captureBrokenBlockContext(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        BlockState brokenState = this.player.level().getBlockState(pos);
        BlockEntity brokenBlockEntity = this.player.level().getBlockEntity(pos);
        ItemStack breakingTool = this.player.getMainHandItem().copy();
        this.shopsandtools$brokenBlockSnapshots.push(new BrokenBlockSnapshot(pos.immutable(), brokenState, brokenBlockEntity, breakingTool));
    }

    @Inject(method = "destroyBlock", at = @At("RETURN"))
    private void shopsandtools$breakCelestiumPickaxeArea(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        BrokenBlockSnapshot snapshot = this.shopsandtools$brokenBlockSnapshots.isEmpty()
                ? null
                : this.shopsandtools$brokenBlockSnapshots.pop();
        if (cir.getReturnValueZ()) {
            CelestiumPickaxeManager.onBlockBroken(
                    this.player,
                    (ServerPlayerGameMode) (Object) this,
                    pos,
                    snapshot != null && snapshot.pos().equals(pos) ? snapshot.state() : this.player.level().getBlockState(pos),
                    snapshot != null && snapshot.pos().equals(pos) ? snapshot.tool() : this.player.getMainHandItem().copy()
            );
            CelestiumShovelManager.onBlockBroken(
                    this.player,
                    (ServerPlayerGameMode) (Object) this,
                    pos,
                    snapshot != null && snapshot.pos().equals(pos) ? snapshot.state() : null,
                    snapshot != null && snapshot.pos().equals(pos) ? snapshot.blockEntity() : null,
                    snapshot != null && snapshot.pos().equals(pos) ? snapshot.tool() : ItemStack.EMPTY
            );
            if (snapshot != null && snapshot.pos().equals(pos)) {
                CelestiumAxeManager.onBlockBroken(this.player, pos, snapshot.state(), snapshot.blockEntity(), snapshot.tool());
            }
        } else if (snapshot != null && !snapshot.pos().equals(pos)) {
            this.shopsandtools$brokenBlockSnapshots.clear();
        }
    }

    @org.spongepowered.asm.mixin.Unique
    private record BrokenBlockSnapshot(BlockPos pos, BlockState state, BlockEntity blockEntity, ItemStack tool) {
    }
}
