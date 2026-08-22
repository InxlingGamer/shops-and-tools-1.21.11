package net.inklinggamer.celestium.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.inklinggamer.celestium.item.CelestiumHoeHelper;
import net.inklinggamer.celestium.player.CelestiumHoeManager;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin {
    @Inject(
            method = "tickChunk",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/BlockState;randomTick(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/random/Random;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void celestium$boostCelestiumHoeCropGrowth(
            WorldChunk chunk,
            int randomTickSpeed,
            CallbackInfo callbackInfo,
            @Local BlockPos pos,
            @Local BlockState state
    ) {
        ServerWorld world = (ServerWorld) (Object) this;
        if (!CelestiumHoeManager.shouldApplyGrowthBoost(world, pos, state)) {
            return;
        }

        Random random = world.getRandom();
        for (int index = 0; index < CelestiumHoeHelper.EXTRA_RANDOM_TICKS; index++) {
            BlockState currentState = world.getBlockState(pos);
            if (!CelestiumHoeManager.shouldApplyGrowthBoost(world, pos, currentState)) {
                break;
            }

            currentState.randomTick(world, pos, random);
        }
    }
}
