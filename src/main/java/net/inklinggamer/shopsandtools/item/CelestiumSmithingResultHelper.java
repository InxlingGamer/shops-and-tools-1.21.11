package net.inklinggamer.shopsandtools.item;

import java.util.function.Function;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import org.jspecify.annotations.Nullable;

public final class CelestiumSmithingResultHelper {
    private CelestiumSmithingResultHelper() {
    }

    public static ItemStack postProcess(ItemStack result, DynamicRegistryManager registryManager) {
        return postProcess(result, key -> getEnchantment(registryManager, key), registryManager);
    }

    static ItemStack postProcess(ItemStack result, Function<RegistryKey<Enchantment>, RegistryEntry<Enchantment>> enchantmentLookup) {
        return postProcess(result, enchantmentLookup, null);
    }

    private static ItemStack postProcess(
            ItemStack result,
            Function<RegistryKey<Enchantment>, RegistryEntry<Enchantment>> enchantmentLookup,
            @Nullable DynamicRegistryManager registryManager
    ) {
        if (result.isEmpty()) {
            return result;
        }

        if (!isCelestiumSmithingResult(result)) {
            return result;
        }

        ItemStack upgradedResult = result.copy();
        removeArmorTrimIfPresent(upgradedResult);

        if (result.isOf(ModItems.CELESTIUM_BOOTS)) {
            RegistryEntry<Enchantment> featherFalling = enchantmentLookup.apply(Enchantments.FEATHER_FALLING);
            EnchantmentHelper.apply(upgradedResult, builder -> builder.set(featherFalling, 5));
        }

        if (result.isOf(ModItems.CELESTIUM_SWORD)) {
            RegistryEntry<Enchantment> sharpness = enchantmentLookup.apply(Enchantments.SHARPNESS);
            EnchantmentHelper.apply(upgradedResult, builder -> builder.set(sharpness, 10));
        }

        if (result.isOf(ModItems.CELESTIUM_SPEAR)) {
            CelestiumSpearHelper.initializeSmithingResult(upgradedResult, requireRegistryManager(registryManager, result));
        }

        if (result.isOf(ModItems.CELESTIUM_PICKAXE)) {
            CelestiumPickaxeHelper.initializeSmithingResult(upgradedResult, requireRegistryManager(registryManager, result));
        }

        if (result.isOf(ModItems.CELESTIUM_SHOVEL)) {
            CelestiumShovelHelper.initializeSmithingResult(upgradedResult, requireRegistryManager(registryManager, result));
        }

        if (result.isOf(ModItems.CELESTIUM_AXE)) {
            CelestiumAxeHelper.initializeSmithingResult(upgradedResult, requireRegistryManager(registryManager, result));
        }

        if (result.isOf(ModItems.CELESTIUM_HOE)) {
            CelestiumHoeHelper.initializeSmithingResult(upgradedResult, requireRegistryManager(registryManager, result));
        }

        return upgradedResult;
    }

    private static boolean isCelestiumSmithingResult(ItemStack stack) {
        return isWearableCelestiumArmor(stack)
                || stack.isOf(ModItems.CELESTIUM_SWORD)
                || stack.isOf(ModItems.CELESTIUM_SPEAR)
                || stack.isOf(ModItems.CELESTIUM_PICKAXE)
                || stack.isOf(ModItems.CELESTIUM_SHOVEL)
                || stack.isOf(ModItems.CELESTIUM_AXE)
                || stack.isOf(ModItems.CELESTIUM_HOE);
    }

    private static boolean isWearableCelestiumArmor(ItemStack stack) {
        return stack.isOf(ModItems.CELESTIUM_HELMET)
                || stack.isOf(ModItems.CELESTIUM_CHESTPLATE)
                || stack.isOf(ModItems.CELESTIUM_LEGGINGS)
                || stack.isOf(ModItems.CELESTIUM_BOOTS);
    }

    private static void removeArmorTrimIfPresent(ItemStack stack) {
        if (isWearableCelestiumArmor(stack)) {
            stack.remove(DataComponentTypes.TRIM);
        }
    }

    private static DynamicRegistryManager requireRegistryManager(@Nullable DynamicRegistryManager registryManager, ItemStack result) {
        if (registryManager == null) {
            throw new IllegalStateException("Registry manager required to post-process smithing result for " + result.getItem());
        }

        return registryManager;
    }

    private static RegistryEntry<Enchantment> getEnchantment(DynamicRegistryManager registryManager, RegistryKey<Enchantment> key) {
        Registry<Enchantment> enchantmentRegistry = registryManager.getOrThrow(RegistryKeys.ENCHANTMENT);
        Enchantment enchantment = enchantmentRegistry.getValueOrThrow(key);
        return enchantmentRegistry.getEntry(enchantment);
    }
}
