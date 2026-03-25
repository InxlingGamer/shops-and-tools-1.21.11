package net.inklinggamer.shopsandtools.mixin;

import net.inklinggamer.shopsandtools.item.CelestiumHoeHelper;
import net.inklinggamer.shopsandtools.player.CelestiumHoeManager;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin {
    @Redirect(
            method = "tickChunk",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/BlockState;randomTick(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/random/Random;)V"
            )
    )
    private void shopsandtools$boostCelestiumHoeCropGrowth(BlockState state, ServerWorld world, BlockPos pos, Random random) {
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
