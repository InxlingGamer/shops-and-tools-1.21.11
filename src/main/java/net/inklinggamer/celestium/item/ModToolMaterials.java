package net.inklinggamer.celestium.item;

import net.inklinggamer.celestium.Celestium;
import net.inklinggamer.celestium.util.ModTags;
import net.minecraft.item.Item;
import net.minecraft.item.ToolMaterial;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.Identifier;

public final class ModToolMaterials {

    // 1. We must define a Tag for what item repairs Celestium tools in an anvil
    public static final TagKey<Item> CELESTIUM_REPAIR = TagKey.of(RegistryKeys.ITEM, Identifier.of(Celestium.MOD_ID, "celestium_repair"));

    public static final ToolMaterial CELESTIUM = new CelestiumToolMaterial();

    private ModToolMaterials() {
    }

    private static final class CelestiumToolMaterial implements ToolMaterial {
        @Override
        public int getDurability() {
            return 4064;
        }

        @Override
        public float getMiningSpeedMultiplier() {
            return 12.0F;
        }

        @Override
        public float getAttackDamage() {
            return 6.0F;
        }

        @Override
        public TagKey<net.minecraft.block.Block> getInverseTag() {
            return ModTags.Blocks.INCORRECT_FOR_CELESTIUM_TOOL;
        }

        @Override
        public int getEnchantability() {
            return 30;
        }

        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.fromTag(CELESTIUM_REPAIR);
        }
    }
}
