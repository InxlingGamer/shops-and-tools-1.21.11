package net.inklinggamer.shopsandtools.mixin;

import net.inklinggamer.shopsandtools.advancement.ModAdvancementActions;
import net.inklinggamer.shopsandtools.item.ModItems;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.Property;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.jspecify.annotations.Nullable;

@Mixin(AnvilScreenHandler.class)
public abstract class AnvilScreenHandlerMixin extends ScreenHandler {
    @Unique
    private static final int shopsandtools$noTooExpensiveLimit = Integer.MAX_VALUE;

    protected AnvilScreenHandlerMixin(@Nullable ScreenHandlerType<?> type, int syncId) {
        super(type, syncId);
    }

    @Shadow
    private int repairItemUsage;

    @Shadow
    @Final
    private Property levelCost;

    @Shadow
    private boolean keepSecondSlot;

    @Inject(method = "updateResult", at = @At("HEAD"), cancellable = true)
    private void shopsandtools$createCelestiumElytraResult(CallbackInfo ci) {
        ItemStack firstInput = this.getSlot(0).getStack();
        ItemStack secondInput = this.getSlot(1).getStack();
        ItemStack chestplate = this.shopsandtools$getChestplateInput(firstInput, secondInput);
        ItemStack elytra = this.shopsandtools$getElytraInput(firstInput, secondInput);

        if (chestplate.isEmpty() || elytra.isEmpty()) {
            return;
        }

        ItemStack result = chestplate.copyComponentsToNewStack(ModItems.CELESTIUM_ELYTRA_CHESTPLATE, 1);

        this.keepSecondSlot = false;
        this.repairItemUsage = 0;
        this.levelCost.set(200);
        this.getSlot(2).setStackNoCallbacks(result);
        ci.cancel();
    }

    @Unique
    private ItemStack shopsandtools$getChestplateInput(ItemStack firstInput, ItemStack secondInput) {
        if (firstInput.isOf(ModItems.CELESTIUM_CHESTPLATE) && secondInput.isOf(Items.ELYTRA)) {
            return firstInput;
        }

        if (secondInput.isOf(ModItems.CELESTIUM_CHESTPLATE) && firstInput.isOf(Items.ELYTRA)) {
            return secondInput;
        }

        return ItemStack.EMPTY;
    }

    @Unique
    private ItemStack shopsandtools$getElytraInput(ItemStack firstInput, ItemStack secondInput) {
        if (firstInput.isOf(Items.ELYTRA) && secondInput.isOf(ModItems.CELESTIUM_CHESTPLATE)) {
            return firstInput;
        }

        if (secondInput.isOf(Items.ELYTRA) && firstInput.isOf(ModItems.CELESTIUM_CHESTPLATE)) {
            return secondInput;
        }

        return ItemStack.EMPTY;
    }

    @ModifyConstant(method = "updateResult", constant = @Constant(intValue = 40, ordinal = 1))
    private int shopsandtools$removeRenameOnlyTooExpensiveLimit(int vanillaLimit) {
        return shopsandtools$noTooExpensiveLimit;
    }

    @ModifyConstant(method = "updateResult", constant = @Constant(intValue = 40, ordinal = 2))
    private int shopsandtools$removeOutputTooExpensiveLimit(int vanillaLimit) {
        return shopsandtools$noTooExpensiveLimit;
    }

    @Inject(method = "onTakeOutput", at = @At("TAIL"))
    private void shopsandtools$triggerBoundToTheSky(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
        if (stack.isOf(ModItems.CELESTIUM_ELYTRA_CHESTPLATE) && player instanceof ServerPlayerEntity serverPlayer) {
            ModAdvancementActions.triggerBoundToTheSky(serverPlayer);
        }
    }
}
