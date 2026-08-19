package net.inklinggamer.celestium.mixin;

import net.inklinggamer.celestium.advancement.ModAdvancementActions;
import net.inklinggamer.celestium.item.ModItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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

@Mixin(AnvilMenu.class)
public abstract class AnvilScreenHandlerMixin extends AbstractContainerMenu {
    @Unique
    private static final int celestium$noTooExpensiveLimit = Integer.MAX_VALUE;

    protected AnvilScreenHandlerMixin(@Nullable MenuType<?> type, int syncId) {
        super(type, syncId);
    }

    @Shadow
    private int repairItemCountCost;

    @Shadow
    @Final
    private DataSlot cost;

    @Shadow
    private boolean onlyRenaming;

    @Inject(method = "createResult", at = @At("HEAD"), cancellable = true)
    private void celestium$createCelestiumElytraResult(CallbackInfo ci) {
        ItemStack firstInput = this.getSlot(0).getItem();
        ItemStack secondInput = this.getSlot(1).getItem();
        ItemStack chestplate = this.celestium$getChestplateInput(firstInput, secondInput);
        ItemStack elytra = this.celestium$getElytraInput(firstInput, secondInput);

        if (chestplate.isEmpty() || elytra.isEmpty()) {
            return;
        }

        ItemStack result = chestplate.transmuteCopy(ModItems.CELESTIUM_ELYTRA_CHESTPLATE, 1);

        this.onlyRenaming = false;
        this.repairItemCountCost = 0;
        this.cost.set(200);
        this.getSlot(2).set(result);
        ci.cancel();
    }

    @Unique
    private ItemStack celestium$getChestplateInput(ItemStack firstInput, ItemStack secondInput) {
        if (firstInput.is(ModItems.CELESTIUM_CHESTPLATE) && secondInput.is(Items.ELYTRA)) {
            return firstInput;
        }

        if (secondInput.is(ModItems.CELESTIUM_CHESTPLATE) && firstInput.is(Items.ELYTRA)) {
            return secondInput;
        }

        return ItemStack.EMPTY;
    }

    @Unique
    private ItemStack celestium$getElytraInput(ItemStack firstInput, ItemStack secondInput) {
        if (firstInput.is(Items.ELYTRA) && secondInput.is(ModItems.CELESTIUM_CHESTPLATE)) {
            return firstInput;
        }

        if (secondInput.is(Items.ELYTRA) && firstInput.is(ModItems.CELESTIUM_CHESTPLATE)) {
            return secondInput;
        }

        return ItemStack.EMPTY;
    }

    @ModifyConstant(method = "createResult", constant = @Constant(intValue = 40, ordinal = 1))
    private int celestium$removeRenameOnlyTooExpensiveLimit(int vanillaLimit) {
        return celestium$noTooExpensiveLimit;
    }

    @ModifyConstant(method = "createResult", constant = @Constant(intValue = 40, ordinal = 2))
    private int celestium$removeOutputTooExpensiveLimit(int vanillaLimit) {
        return celestium$noTooExpensiveLimit;
    }

    @Inject(method = "onTake", at = @At("TAIL"))
    private void celestium$triggerBoundToTheSky(Player player, ItemStack stack, CallbackInfo ci) {
        if (stack.is(ModItems.CELESTIUM_ELYTRA_CHESTPLATE) && player instanceof ServerPlayer serverPlayer) {
            ModAdvancementActions.triggerBoundToTheSky(serverPlayer);
        }
    }
}
