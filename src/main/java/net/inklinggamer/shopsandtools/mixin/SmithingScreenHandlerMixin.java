package net.inklinggamer.shopsandtools.mixin;

import net.inklinggamer.shopsandtools.item.ModItems;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.SmithingScreenHandler;
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
    private void shopsandtools$upgradeCelestiumBootsToFeatherFallingFive(CallbackInfo ci) {
        ItemStack result = this.getSlot(SmithingScreenHandler.OUTPUT_ID).getStack();
        if (!EnchantmentHelper.canHaveEnchantments(result)) {
            return;
        }

        Registry<Enchantment> enchantmentRegistry = this.world.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);
        ItemStack upgradedResult = result.copy();

        if (result.isOf(ModItems.CELESTIUM_BOOTS)) {
            Enchantment featherFallingValue = enchantmentRegistry.getValueOrThrow(Enchantments.FEATHER_FALLING);
            RegistryEntry<Enchantment> featherFalling = enchantmentRegistry.getEntry(featherFallingValue);
            EnchantmentHelper.apply(upgradedResult, builder -> builder.set(featherFalling, 5));
        }

        if (result.isOf(ModItems.CELESTIUM_SWORD)) {
            Enchantment sharpnessValue = enchantmentRegistry.getValueOrThrow(Enchantments.SHARPNESS);
            RegistryEntry<Enchantment> sharpness = enchantmentRegistry.getEntry(sharpnessValue);
            EnchantmentHelper.apply(upgradedResult, builder -> builder.set(sharpness, 10));
        }

        this.getSlot(SmithingScreenHandler.OUTPUT_ID).setStackNoCallbacks(upgradedResult);
    }
}
