package net.inklinggamer.shopsandtools.item;

import net.minecraft.block.BlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;

public final class CelestiumAxeHelper {
    public static final int EFFICIENCY_LEVEL = 10;
    public static final int UNBREAKING_LEVEL = 5;
    public static final int SHARPNESS_LEVEL = 10;

    private CelestiumAxeHelper() {
    }

    public static boolean isCelestiumAxe(ItemStack stack) {
        return stack.isOf(ModItems.CELESTIUM_AXE);
    }

    public static void initializeSmithingResult(ItemStack stack, DynamicRegistryManager registryManager) {
        if (!isCelestiumAxe(stack)) {
            return;
        }

        Registry<Enchantment> enchantmentRegistry = registryManager.getOrThrow(RegistryKeys.ENCHANTMENT);
        RegistryEntry<Enchantment> efficiency = shopsandtools$getEnchantment(enchantmentRegistry, Enchantments.EFFICIENCY);
        RegistryEntry<Enchantment> unbreaking = shopsandtools$getEnchantment(enchantmentRegistry, Enchantments.UNBREAKING);
        RegistryEntry<Enchantment> sharpness = shopsandtools$getEnchantment(enchantmentRegistry, Enchantments.SHARPNESS);

        EnchantmentHelper.apply(stack, builder -> {
            builder.set(efficiency, EFFICIENCY_LEVEL);
            builder.set(unbreaking, UNBREAKING_LEVEL);
            builder.set(sharpness, SHARPNESS_LEVEL);
        });
    }

    public static boolean isEligibleWoodBlock(BlockState state) {
        return state.isIn(BlockTags.LOGS_THAT_BURN);
    }

    private static RegistryEntry<Enchantment> shopsandtools$getEnchantment(Registry<Enchantment> registry, net.minecraft.registry.RegistryKey<Enchantment> key) {
        Enchantment enchantment = registry.getValueOrThrow(key);
        return registry.getEntry(enchantment);
    }
}
