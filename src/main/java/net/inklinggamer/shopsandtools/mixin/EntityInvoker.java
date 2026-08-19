package net.inklinggamer.shopsandtools.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Entity.class)
public interface EntityInvoker {
    @Invoker("markHurt")
    void shopsandtools$invokeScheduleVelocityUpdate();

    @Invoker("playStepSound")
    void shopsandtools$invokePlayStepSound(BlockPos pos, BlockState state);
}
