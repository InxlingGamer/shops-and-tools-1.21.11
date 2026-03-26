package net.inklinggamer.shopsandtools.item;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;

public final class CelestiumSmithingResultHelper {
    private CelestiumSmithingResultHelper() {
    }

    public static ItemStack postProcess(ItemStack result, DynamicRegistryManager registryManager) {
        if (result.isEmpty()) {
            return result;
        }

        if (!isCelestiumSmithingResult(result)) {
            return result;
        }

        ItemStack upgradedResult = result.copy();

        if (result.isOf(ModItems.CELESTIUM_BOOTS)) {
            RegistryEntry<Enchantment> featherFalling = getEnchantment(registryManager, Enchantments.FEATHER_FALLING);
            EnchantmentHelper.apply(upgradedResult, builder -> builder.set(featherFalling, 5));
        }

        if (result.isOf(ModItems.CELESTIUM_SWORD)) {
            RegistryEntry<Enchantment> sharpness = getEnchantment(registryManager, Enchantments.SHARPNESS);
            EnchantmentHelper.apply(upgradedResult, builder -> builder.set(sharpness, 10));
        }

        if (result.isOf(ModItems.CELESTIUM_SPEAR)) {
            CelestiumSpearHelper.initializeSmithingResult(upgradedResult, registryManager);
        }

        if (result.isOf(ModItems.CELESTIUM_PICKAXE)) {
            CelestiumPickaxeHelper.initializeSmithingResult(upgradedResult, registryManager);
        }

        if (result.isOf(ModItems.CELESTIUM_SHOVEL)) {
            CelestiumShovelHelper.initializeSmithingResult(upgradedResult, registryManager);
        }

        if (result.isOf(ModItems.CELESTIUM_AXE)) {
            CelestiumAxeHelper.initializeSmithingResult(upgradedResult, registryManager);
        }

        if (result.isOf(ModItems.CELESTIUM_HOE)) {
            CelestiumHoeHelper.initializeSmithingResult(upgradedResult, registryManager);
        }

        return upgradedResult;
    }

    private static boolean isCelestiumSmithingResult(ItemStack stack) {
        return stack.isOf(ModItems.CELESTIUM_BOOTS)
                || stack.isOf(ModItems.CELESTIUM_SWORD)
                || stack.isOf(ModItems.CELESTIUM_SPEAR)
                || stack.isOf(ModItems.CELESTIUM_PICKAXE)
                || stack.isOf(ModItems.CELESTIUM_SHOVEL)
                || stack.isOf(ModItems.CELESTIUM_AXE)
                || stack.isOf(ModItems.CELESTIUM_HOE);
    }

    private static RegistryEntry<Enchantment> getEnchantment(DynamicRegistryManager registryManager, net.minecraft.registry.RegistryKey<Enchantment> key) {
        Registry<Enchantment> enchantmentRegistry = registryManager.getOrThrow(RegistryKeys.ENCHANTMENT);
        Enchantment enchantment = enchantmentRegistry.getValueOrThrow(key);
        return enchantmentRegistry.getEntry(enchantment);
    }
}
