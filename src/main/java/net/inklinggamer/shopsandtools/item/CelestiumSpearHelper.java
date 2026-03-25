package net.inklinggamer.shopsandtools.item;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;

public final class CelestiumSpearHelper {
    public static final int SHARPNESS_LEVEL = 10;
    public static final int LUNGE_LEVEL = 5;
    public static final int MENDING_LEVEL = 1;

    private CelestiumSpearHelper() {
    }

    public static boolean isCelestiumSpear(ItemStack stack) {
        return stack.isOf(ModItems.CELESTIUM_SPEAR);
    }

    public static boolean isCelestiumSpearEquipped(PlayerEntity player) {
        return isCelestiumSpear(player.getMainHandStack());
    }

    public static boolean isCelestiumSpearHeld(PlayerEntity player) {
        return isCelestiumSpear(player.getMainHandStack()) || isCelestiumSpear(player.getOffHandStack());
    }

    public static void initializeSmithingResult(ItemStack stack, DynamicRegistryManager registryManager) {
        if (!isCelestiumSpear(stack)) {
            return;
        }

        Registry<Enchantment> enchantmentRegistry = registryManager.getOrThrow(RegistryKeys.ENCHANTMENT);
        RegistryEntry<Enchantment> sharpness = shopsandtools$getEnchantment(enchantmentRegistry, Enchantments.SHARPNESS);
        RegistryEntry<Enchantment> lunge = shopsandtools$getEnchantment(enchantmentRegistry, Enchantments.LUNGE);
        RegistryEntry<Enchantment> mending = shopsandtools$getEnchantment(enchantmentRegistry, Enchantments.MENDING);

        EnchantmentHelper.apply(stack, builder -> {
            builder.set(sharpness, SHARPNESS_LEVEL);
            builder.set(lunge, LUNGE_LEVEL);
            builder.set(mending, MENDING_LEVEL);
        });
    }

    private static RegistryEntry<Enchantment> shopsandtools$getEnchantment(Registry<Enchantment> registry, net.minecraft.registry.RegistryKey<Enchantment> key) {
        Enchantment enchantment = registry.getValueOrThrow(key);
        return registry.getEntry(enchantment);
    }
}
