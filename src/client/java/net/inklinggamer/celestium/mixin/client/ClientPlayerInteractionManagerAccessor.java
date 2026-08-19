package net.inklinggamer.celestium.mixin.client;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MultiPlayerGameMode.class)
public interface ClientPlayerInteractionManagerAccessor {
    @Accessor("destroyBlockPos")
    BlockPos celestium$getCurrentBreakingPos();

    @Accessor("destroyProgress")
    float celestium$getCurrentBreakingProgress();

    @Accessor("isDestroying")
    boolean celestium$isBreakingBlock();
}
