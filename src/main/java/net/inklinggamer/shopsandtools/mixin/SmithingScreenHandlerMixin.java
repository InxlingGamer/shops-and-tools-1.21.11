package net.inklinggamer.shopsandtools.mixin;

import net.inklinggamer.shopsandtools.advancement.ModAdvancementActions;
import net.inklinggamer.shopsandtools.item.CelestiumSmithingResultHelper;
import net.inklinggamer.shopsandtools.item.ModItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SmithingMenu.class)
public abstract class SmithingScreenHandlerMixin extends AbstractContainerMenu {
    @Shadow
    @Final
    private Level level;

    protected SmithingScreenHandlerMixin(@Nullable MenuType<?> type, int syncId) {
        super(type, syncId);
    }

    @Inject(method = "createResult", at = @At("RETURN"))
    private void shopsandtools$postProcessCelestiumSmithingResult(CallbackInfo ci) {
        ItemStack result = this.getSlot(SmithingMenu.RESULT_SLOT).getItem();
        if (result.isEmpty()) {
            return;
        }

        // Vanilla smithing has already transferred the carried result components,
        // so we only need to adjust the finished output stack here.
        ItemStack upgradedResult = CelestiumSmithingResultHelper.postProcess(result, this.level.registryAccess());
        if (ItemStack.matches(result, upgradedResult)) {
            return;
        }

        this.getSlot(SmithingMenu.RESULT_SLOT).set(upgradedResult);
    }

    @Inject(method = "onTake", at = @At("TAIL"))
    private void shopsandtools$triggerTouchGrass(Player player, ItemStack stack, CallbackInfo ci) {
        if (stack.is(ModItems.CELESTIUM_HOE) && player instanceof ServerPlayer serverPlayer) {
            ModAdvancementActions.triggerTouchGrass(serverPlayer);
        }
    }
}
