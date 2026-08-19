package net.inklinggamer.celestium.item;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.state.BlockState;

public final class CelestiumAxeHelper {
    public static final int EFFICIENCY_LEVEL = 10;
    public static final int UNBREAKING_LEVEL = 5;
    public static final int SHARPNESS_LEVEL = 10;
    private static final TagKey<net.minecraft.world.level.block.Block> LOGS_THAT_BURN =
            TagKey.create(Registries.BLOCK, Identifier.withDefaultNamespace("logs_that_burn"));

    private CelestiumAxeHelper() {
    }

    public static boolean isCelestiumAxe(ItemStack stack) {
        return stack.is(ModItems.CELESTIUM_AXE);
    }

    public static void initializeSmithingResult(ItemStack stack, RegistryAccess registryManager) {
        if (!isCelestiumAxe(stack)) {
            return;
        }

        Registry<Enchantment> enchantmentRegistry = registryManager.lookupOrThrow(Registries.ENCHANTMENT);
        Holder<Enchantment> efficiency = celestium$getEnchantment(enchantmentRegistry, Enchantments.EFFICIENCY);
        Holder<Enchantment> unbreaking = celestium$getEnchantment(enchantmentRegistry, Enchantments.UNBREAKING);
        Holder<Enchantment> sharpness = celestium$getEnchantment(enchantmentRegistry, Enchantments.SHARPNESS);

        EnchantmentHelper.updateEnchantments(stack, builder -> {
            builder.set(efficiency, EFFICIENCY_LEVEL);
            builder.set(unbreaking, UNBREAKING_LEVEL);
            builder.set(sharpness, SHARPNESS_LEVEL);
        });
    }

    public static boolean isEligibleWoodBlock(BlockState state) {
        return state.is(LOGS_THAT_BURN);
    }

    private static Holder<Enchantment> celestium$getEnchantment(Registry<Enchantment> registry, net.minecraft.resources.ResourceKey<Enchantment> key) {
        Enchantment enchantment = registry.getValueOrThrow(key);
        return registry.wrapAsHolder(enchantment);
    }
}
