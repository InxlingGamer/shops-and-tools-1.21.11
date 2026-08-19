package net.inklinggamer.celestium.item;

import java.util.function.Function;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import org.jspecify.annotations.Nullable;

public final class CelestiumSmithingResultHelper {
    private CelestiumSmithingResultHelper() {
    }

    public static ItemStack postProcess(ItemStack result, RegistryAccess registryManager) {
        return postProcess(result, key -> getEnchantment(registryManager, key), registryManager);
    }

    static ItemStack postProcess(ItemStack result, Function<ResourceKey<Enchantment>, Holder<Enchantment>> enchantmentLookup) {
        return postProcess(result, enchantmentLookup, null);
    }

    private static ItemStack postProcess(
            ItemStack result,
            Function<ResourceKey<Enchantment>, Holder<Enchantment>> enchantmentLookup,
            @Nullable RegistryAccess registryManager
    ) {
        if (result.isEmpty()) {
            return result;
        }

        if (!isCelestiumSmithingResult(result)) {
            return result;
        }

        ItemStack upgradedResult = result.copy();
        removeArmorTrimIfPresent(upgradedResult);

        if (result.is(ModItems.CELESTIUM_BOOTS)) {
            Holder<Enchantment> featherFalling = enchantmentLookup.apply(Enchantments.FEATHER_FALLING);
            EnchantmentHelper.updateEnchantments(upgradedResult, builder -> builder.set(featherFalling, 5));
        }

        if (result.is(ModItems.CELESTIUM_SWORD)) {
            Holder<Enchantment> sharpness = enchantmentLookup.apply(Enchantments.SHARPNESS);
            EnchantmentHelper.updateEnchantments(upgradedResult, builder -> builder.set(sharpness, 10));
        }

        if (result.is(ModItems.CELESTIUM_SPEAR)) {
            CelestiumSpearHelper.initializeSmithingResult(upgradedResult, requireRegistryManager(registryManager, result));
        }

        if (result.is(ModItems.CELESTIUM_PICKAXE)) {
            CelestiumPickaxeHelper.initializeSmithingResult(upgradedResult, requireRegistryManager(registryManager, result));
        }

        if (result.is(ModItems.CELESTIUM_SHOVEL)) {
            CelestiumShovelHelper.initializeSmithingResult(upgradedResult, requireRegistryManager(registryManager, result));
        }

        if (result.is(ModItems.CELESTIUM_AXE)) {
            CelestiumAxeHelper.initializeSmithingResult(upgradedResult, requireRegistryManager(registryManager, result));
        }

        if (result.is(ModItems.CELESTIUM_HOE)) {
            CelestiumHoeHelper.initializeSmithingResult(upgradedResult, requireRegistryManager(registryManager, result));
        }

        return upgradedResult;
    }

    private static boolean isCelestiumSmithingResult(ItemStack stack) {
        return isWearableCelestiumArmor(stack)
                || stack.is(ModItems.CELESTIUM_SWORD)
                || stack.is(ModItems.CELESTIUM_SPEAR)
                || stack.is(ModItems.CELESTIUM_PICKAXE)
                || stack.is(ModItems.CELESTIUM_SHOVEL)
                || stack.is(ModItems.CELESTIUM_AXE)
                || stack.is(ModItems.CELESTIUM_HOE);
    }

    private static boolean isWearableCelestiumArmor(ItemStack stack) {
        return stack.is(ModItems.CELESTIUM_HELMET)
                || stack.is(ModItems.CELESTIUM_CHESTPLATE)
                || stack.is(ModItems.CELESTIUM_LEGGINGS)
                || stack.is(ModItems.CELESTIUM_BOOTS);
    }

    private static void removeArmorTrimIfPresent(ItemStack stack) {
        if (isWearableCelestiumArmor(stack)) {
            stack.remove(DataComponents.TRIM);
        }
    }

    private static RegistryAccess requireRegistryManager(@Nullable RegistryAccess registryManager, ItemStack result) {
        if (registryManager == null) {
            throw new IllegalStateException("Registry manager required to post-process smithing result for " + result.getItem());
        }

        return registryManager;
    }

    private static Holder<Enchantment> getEnchantment(RegistryAccess registryManager, ResourceKey<Enchantment> key) {
        Registry<Enchantment> enchantmentRegistry = registryManager.lookupOrThrow(Registries.ENCHANTMENT);
        Enchantment enchantment = enchantmentRegistry.getValueOrThrow(key);
        return enchantmentRegistry.wrapAsHolder(enchantment);
    }
}
