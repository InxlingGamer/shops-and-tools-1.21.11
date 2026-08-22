package net.inklinggamer.celestium.mixin;

import net.inklinggamer.celestium.advancement.ModAdvancementActions;
import net.inklinggamer.celestium.item.ModItems;
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
import org.jetbrains.annotations.Nullable;

@Mixin(AnvilScreenHandler.class)
public abstract class AnvilScreenHandlerMixin extends ScreenHandler {
    @Unique
    private static final int celestium$noTooExpensiveLimit = Integer.MAX_VALUE;

    protected AnvilScreenHandlerMixin(@Nullable ScreenHandlerType<?> type, int syncId) {
        super(type, syncId);
    }

    @Shadow
    private int repairItemUsage;

    @Shadow
    @Final
    private Property levelCost;

    @Inject(method = "updateResult", at = @At("HEAD"), cancellable = true)
    private void celestium$createCelestiumElytraResult(CallbackInfo ci) {
        ItemStack firstInput = this.getSlot(0).getStack();
        ItemStack secondInput = this.getSlot(1).getStack();
        ItemStack chestplate = this.celestium$getChestplateInput(firstInput, secondInput);
        ItemStack elytra = this.celestium$getElytraInput(firstInput, secondInput);

        if (chestplate.isEmpty() || elytra.isEmpty()) {
            return;
        }

        ItemStack result = chestplate.copyComponentsToNewStack(ModItems.CELESTIUM_ELYTRA_CHESTPLATE, 1);

        this.repairItemUsage = 0;
        this.levelCost.set(200);
        this.getSlot(2).setStackNoCallbacks(result);
        ci.cancel();
    }

    @Unique
    private ItemStack celestium$getChestplateInput(ItemStack firstInput, ItemStack secondInput) {
        if (firstInput.isOf(ModItems.CELESTIUM_CHESTPLATE) && secondInput.isOf(Items.ELYTRA)) {
            return firstInput;
        }

        if (secondInput.isOf(ModItems.CELESTIUM_CHESTPLATE) && firstInput.isOf(Items.ELYTRA)) {
            return secondInput;
        }

        return ItemStack.EMPTY;
    }

    @Unique
    private ItemStack celestium$getElytraInput(ItemStack firstInput, ItemStack secondInput) {
        if (firstInput.isOf(Items.ELYTRA) && secondInput.isOf(ModItems.CELESTIUM_CHESTPLATE)) {
            return firstInput;
        }

        if (secondInput.isOf(Items.ELYTRA) && firstInput.isOf(ModItems.CELESTIUM_CHESTPLATE)) {
            return secondInput;
        }

        return ItemStack.EMPTY;
    }

    @ModifyConstant(method = "updateResult", constant = @Constant(intValue = 40, ordinal = 1))
    private int celestium$removeRenameOnlyTooExpensiveLimit(int vanillaLimit) {
        return celestium$noTooExpensiveLimit;
    }

    @ModifyConstant(method = "updateResult", constant = @Constant(intValue = 40, ordinal = 2))
    private int celestium$removeOutputTooExpensiveLimit(int vanillaLimit) {
        return celestium$noTooExpensiveLimit;
    }

    @Inject(method = "onTakeOutput", at = @At("TAIL"))
    private void celestium$triggerBoundToTheSky(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
        if (stack.isOf(ModItems.CELESTIUM_ELYTRA_CHESTPLATE) && player instanceof ServerPlayerEntity serverPlayer) {
            ModAdvancementActions.triggerBoundToTheSky(serverPlayer);
        }
    }
}
