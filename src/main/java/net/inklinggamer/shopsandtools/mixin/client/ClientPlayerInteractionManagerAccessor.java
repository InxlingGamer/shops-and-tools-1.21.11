package net.inklinggamer.shopsandtools.mixin.client;

import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientPlayerInteractionManager.class)
public interface ClientPlayerInteractionManagerAccessor {
    @Accessor("currentBreakingPos")
    BlockPos shopsandtools$getCurrentBreakingPos();

    @Accessor("currentBreakingProgress")
    float shopsandtools$getCurrentBreakingProgress();

    @Accessor("breakingBlock")
    boolean shopsandtools$isBreakingBlock();
}
