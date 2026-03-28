package net.inklinggamer.shopsandtools.mixin;

import net.inklinggamer.shopsandtools.advancement.ModAdvancementActions;
import net.inklinggamer.shopsandtools.item.CelestiumSmithingResultHelper;
import net.inklinggamer.shopsandtools.item.ModItems;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.SmithingScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SmithingScreenHandler.class)
public abstract class SmithingScreenHandlerMixin extends ScreenHandler {
    @Shadow
    @Final
    private World world;

    protected SmithingScreenHandlerMixin(@Nullable ScreenHandlerType<?> type, int syncId) {
        super(type, syncId);
    }

    @Inject(method = "updateResult", at = @At("RETURN"))
    private void shopsandtools$postProcessCelestiumSmithingResult(CallbackInfo ci) {
        ItemStack result = this.getSlot(SmithingScreenHandler.OUTPUT_ID).getStack();
        if (result.isEmpty()) {
            return;
        }

        // Vanilla smithing has already transferred the carried result components,
        // so we only need to adjust the finished output stack here.
        ItemStack upgradedResult = CelestiumSmithingResultHelper.postProcess(result, this.world.getRegistryManager());
        if (ItemStack.areEqual(result, upgradedResult)) {
            return;
        }

        this.getSlot(SmithingScreenHandler.OUTPUT_ID).setStackNoCallbacks(upgradedResult);
    }

    @Inject(method = "onTakeOutput", at = @At("TAIL"))
    private void shopsandtools$triggerTouchGrass(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
        if (stack.isOf(ModItems.CELESTIUM_HOE) && player instanceof ServerPlayerEntity serverPlayer) {
            ModAdvancementActions.triggerTouchGrass(serverPlayer);
        }
    }
}
