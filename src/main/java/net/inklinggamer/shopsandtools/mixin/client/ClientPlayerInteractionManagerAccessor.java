package net.inklinggamer.shopsandtools.mixin.client;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MultiPlayerGameMode.class)
public interface ClientPlayerInteractionManagerAccessor {
    @Accessor("destroyBlockPos")
    BlockPos shopsandtools$getCurrentBreakingPos();

    @Accessor("destroyProgress")
    float shopsandtools$getCurrentBreakingProgress();

    @Accessor("isDestroying")
    boolean shopsandtools$isBreakingBlock();
}
