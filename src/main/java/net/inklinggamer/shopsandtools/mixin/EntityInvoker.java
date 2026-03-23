package net.inklinggamer.shopsandtools.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Entity.class)
public interface EntityInvoker {
    @Invoker("scheduleVelocityUpdate")
    void shopsandtools$invokeScheduleVelocityUpdate();

    @Invoker("playStepSound")
    void shopsandtools$invokePlayStepSound(BlockPos pos, BlockState state);
}
