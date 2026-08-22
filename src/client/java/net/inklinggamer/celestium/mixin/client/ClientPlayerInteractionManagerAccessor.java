package net.inklinggamer.celestium.mixin.client;

import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientPlayerInteractionManager.class)
public interface ClientPlayerInteractionManagerAccessor {
    @Accessor("currentBreakingPos")
    BlockPos celestium$getCurrentBreakingPos();

    @Accessor("currentBreakingProgress")
    float celestium$getCurrentBreakingProgress();

    @Accessor("breakingBlock")
    boolean celestium$isBreakingBlock();
}
