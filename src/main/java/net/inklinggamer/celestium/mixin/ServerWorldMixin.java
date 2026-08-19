package net.inklinggamer.celestium.mixin;

import net.inklinggamer.celestium.item.CelestiumHoeHelper;
import net.inklinggamer.celestium.player.CelestiumHoeManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerLevel.class)
public abstract class ServerWorldMixin {
    @Redirect(
            method = "tickChunk",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/state/BlockState;randomTick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/util/RandomSource;)V"
            )
    )
    private void celestium$boostCelestiumHoeCropGrowth(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        boolean shouldBoost = CelestiumHoeManager.shouldApplyGrowthBoost(world, pos, state);
        state.randomTick(world, pos, random);

        if (!shouldBoost) {
            return;
        }

        for (int index = 0; index < CelestiumHoeHelper.EXTRA_RANDOM_TICKS; index++) {
            BlockState currentState = world.getBlockState(pos);
            if (!CelestiumHoeManager.shouldApplyGrowthBoost(world, pos, currentState)) {
                break;
            }

            currentState.randomTick(world, pos, random);
        }
    }
}
