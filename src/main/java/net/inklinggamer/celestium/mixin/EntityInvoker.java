package net.inklinggamer.celestium.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Entity.class)
public interface EntityInvoker {
    @Invoker("scheduleVelocityUpdate")
    void celestium$invokeScheduleVelocityUpdate();

    @Invoker("playStepSound")
    void celestium$invokePlayStepSound(BlockPos pos, BlockState state);
}
