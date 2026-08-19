package net.inklinggamer.celestium.item;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

public final class CelestiumSpearHelper {
    public static final int SHARPNESS_LEVEL = 10;
    public static final int LUNGE_LEVEL = 5;
    public static final int MENDING_LEVEL = 1;

    private CelestiumSpearHelper() {
    }

    public static boolean isCelestiumSpear(ItemStack stack) {
        return stack.is(ModItems.CELESTIUM_SPEAR);
    }

    public static boolean isCelestiumSpearEquipped(Player player) {
        return isCelestiumSpear(player.getMainHandItem());
    }

    public static boolean isCelestiumSpearHeld(Player player) {
        return isCelestiumSpear(player.getMainHandItem()) || isCelestiumSpear(player.getOffhandItem());
    }

    public static void initializeSmithingResult(ItemStack stack, RegistryAccess registryManager) {
        if (!isCelestiumSpear(stack)) {
            return;
        }

        Registry<Enchantment> enchantmentRegistry = registryManager.lookupOrThrow(Registries.ENCHANTMENT);
        Holder<Enchantment> sharpness = celestium$getEnchantment(enchantmentRegistry, Enchantments.SHARPNESS);
        Holder<Enchantment> lunge = celestium$getEnchantment(enchantmentRegistry, Enchantments.LUNGE);
        Holder<Enchantment> mending = celestium$getEnchantment(enchantmentRegistry, Enchantments.MENDING);

        EnchantmentHelper.updateEnchantments(stack, builder -> {
            builder.set(sharpness, SHARPNESS_LEVEL);
            builder.set(lunge, LUNGE_LEVEL);
            builder.set(mending, MENDING_LEVEL);
        });
    }

    private static Holder<Enchantment> celestium$getEnchantment(Registry<Enchantment> registry, net.minecraft.resources.ResourceKey<Enchantment> key) {
        Enchantment enchantment = registry.getValueOrThrow(key);
        return registry.wrapAsHolder(enchantment);
    }
}
