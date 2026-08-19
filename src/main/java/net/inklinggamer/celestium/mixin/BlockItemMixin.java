package net.inklinggamer.celestium.mixin;

import net.inklinggamer.celestium.player.CelestiumAxeManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public abstract class BlockItemMixin {
    @Inject(method = "place(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/InteractionResult;", at = @At("RETURN"))
    private void celestium$trackPlacedLogs(BlockPlaceContext context, CallbackInfoReturnable<InteractionResult> cir) {
        if (!cir.getReturnValue().consumesAction() || !(context.getLevel() instanceof ServerLevel world)) {
            return;
        }

        BlockPos pos = context.getClickedPos();
        BlockState placedState = world.getBlockState(pos);
        CelestiumAxeManager.onBlockPlaced(world, pos, placedState);
    }
}
