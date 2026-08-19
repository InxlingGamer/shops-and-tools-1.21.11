package net.inklinggamer.celestium.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Entity.class)
public interface EntityInvoker {
    @Invoker("markHurt")
    void celestium$invokeScheduleVelocityUpdate();

    @Invoker("playStepSound")
    void celestium$invokePlayStepSound(BlockPos pos, BlockState state);
}
