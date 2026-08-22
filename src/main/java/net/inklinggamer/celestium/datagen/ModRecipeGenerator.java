package net.inklinggamer.celestium.datagen;

import net.inklinggamer.celestium.Celestium;
import net.inklinggamer.celestium.block.ModBlocks;
import net.inklinggamer.celestium.item.ModItems;
import net.minecraft.data.server.recipe.*;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.criterion.InventoryChangedCriterion;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public final class ModRecipeGenerator {

    private ModRecipeGenerator() {
    }

    public static void generate(RecipeExporter recipeExporter) {

        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModItems.CELESTIUM, 1)
                .pattern("#N#")
                .pattern("DBE")
                .pattern("#H#")
                .input('#', Items.AMETHYST_SHARD)
                .input('N', Items.NETHER_STAR)
                .input('D', Items.DRAGON_BREATH)
                .input('B', Items.BLAZE_POWDER)
                .input('E', ModItems.WARDEN_HEART)
                .input('H', Items.HEAVY_CORE)
                .criterion(hasItem(Items.NETHER_STAR), conditionsFromItem(Items.NETHER_STAR))

                .offerTo(recipeExporter, Identifier.of(Celestium.MOD_ID, "celestium"));

        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModBlocks.CELESTIUM_BLOCK, 1)
                .pattern("###")
                .pattern("###")
                .pattern("###")
                .input('#', ModItems.CELESTIUM)
                .criterion(hasItem(ModItems.CELESTIUM), conditionsFromItem(ModItems.CELESTIUM))
                .offerTo(recipeExporter, Identifier.of(Celestium.MOD_ID, "celestium_block"));

        ShapelessRecipeJsonBuilder.create(RecipeCategory.MISC, ModItems.CELESTIUM, 9)
                .input(ModBlocks.CELESTIUM_BLOCK)
                .criterion(hasItem(ModBlocks.CELESTIUM_BLOCK), conditionsFromItem(ModBlocks.CELESTIUM_BLOCK))
                .offerTo(recipeExporter, Identifier.of(Celestium.MOD_ID, "celestium_from_block"));

        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModItems.CELESTIUM_UPGRADE_TEMPLATE, 2)
                .pattern("#d#")
                .pattern("#e#")
                .pattern("###")
                .input('#', Items.NETHERITE_INGOT)
                .input('d', ModItems.CELESTIUM_UPGRADE_TEMPLATE)
                .input('e', Items.AMETHYST_BLOCK)
                .criterion(hasItem(ModItems.CELESTIUM_UPGRADE_TEMPLATE), conditionsFromItem(ModItems.CELESTIUM_UPGRADE_TEMPLATE))
                .offerTo(recipeExporter, Identifier.of(Celestium.MOD_ID, "celestium_upgrade_template_duplication"));

        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModItems.CELESTIUM_UPGRADE_TEMPLATE, 1)
                .pattern("#o#")
                .pattern("#p#")
                .pattern("#l#")
                .input('#', Items.NETHERITE_INGOT)
                .input('o', Items.NETHER_STAR)
                .input('p', Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE)
                .input('l', Items.HEAVY_CORE)
                .criterion(hasItem(Items.NETHER_STAR), conditionsFromItem(Items.NETHER_STAR))
                .offerTo(recipeExporter, Identifier.of(Celestium.MOD_ID, "celestium_upgrade_template"));

        SmithingTransformRecipeJsonBuilder.create(
                        Ingredient.ofItems(ModItems.CELESTIUM_UPGRADE_TEMPLATE),
                        Ingredient.ofItems(Items.NETHERITE_HELMET),
                        Ingredient.ofItems(ModItems.CELESTIUM),
                        RecipeCategory.COMBAT,
                        ModItems.CELESTIUM_HELMET)
                .criterion(hasItem(ModItems.CELESTIUM_UPGRADE_TEMPLATE), conditionsFromItem(ModItems.CELESTIUM_UPGRADE_TEMPLATE))
                .offerTo(recipeExporter, Identifier.of(Celestium.MOD_ID, "celestium_helmet"));

        SmithingTransformRecipeJsonBuilder.create(
                        Ingredient.ofItems(ModItems.CELESTIUM_UPGRADE_TEMPLATE),
                        Ingredient.ofItems(Items.NETHERITE_CHESTPLATE),
                        Ingredient.ofItems(ModItems.CELESTIUM),
                        RecipeCategory.COMBAT,
                        ModItems.CELESTIUM_CHESTPLATE)
                .criterion(hasItem(ModItems.CELESTIUM_UPGRADE_TEMPLATE), conditionsFromItem(ModItems.CELESTIUM_UPGRADE_TEMPLATE))
                .offerTo(recipeExporter, Identifier.of(Celestium.MOD_ID, "celestium_chestplate"));

        SmithingTransformRecipeJsonBuilder.create(
                        Ingredient.ofItems(ModItems.CELESTIUM_UPGRADE_TEMPLATE),
                        Ingredient.ofItems(Items.NETHERITE_LEGGINGS),
                        Ingredient.ofItems(ModItems.CELESTIUM),
                        RecipeCategory.COMBAT,
                        ModItems.CELESTIUM_LEGGINGS)
                .criterion(hasItem(ModItems.CELESTIUM_UPGRADE_TEMPLATE), conditionsFromItem(ModItems.CELESTIUM_UPGRADE_TEMPLATE))
                .offerTo(recipeExporter, Identifier.of(Celestium.MOD_ID, "celestium_leggings"));

        SmithingTransformRecipeJsonBuilder.create(
                        Ingredient.ofItems(ModItems.CELESTIUM_UPGRADE_TEMPLATE),
                        Ingredient.ofItems(Items.NETHERITE_BOOTS),
                        Ingredient.ofItems(ModItems.CELESTIUM),
                        RecipeCategory.COMBAT,
                        ModItems.CELESTIUM_BOOTS)
                .criterion(hasItem(ModItems.CELESTIUM_UPGRADE_TEMPLATE), conditionsFromItem(ModItems.CELESTIUM_UPGRADE_TEMPLATE))
                .offerTo(recipeExporter, Identifier.of(Celestium.MOD_ID, "celestium_boots"));

        SmithingTransformRecipeJsonBuilder.create(
                        Ingredient.ofItems(ModItems.CELESTIUM_UPGRADE_TEMPLATE),
                        Ingredient.ofItems(Items.NETHERITE_SWORD),
                        Ingredient.ofItems(ModItems.CELESTIUM),
                        RecipeCategory.COMBAT,
                        ModItems.CELESTIUM_SWORD)
                .criterion(hasItem(ModItems.CELESTIUM_UPGRADE_TEMPLATE), conditionsFromItem(ModItems.CELESTIUM_UPGRADE_TEMPLATE))
                .offerTo(recipeExporter, Identifier.of(Celestium.MOD_ID, "celestium_sword"));

        SmithingTransformRecipeJsonBuilder.create(
                        Ingredient.ofItems(ModItems.CELESTIUM_UPGRADE_TEMPLATE),
                        Ingredient.ofItems(Items.NETHERITE_PICKAXE),
                        Ingredient.ofItems(ModItems.CELESTIUM),
                        RecipeCategory.COMBAT,
                        ModItems.CELESTIUM_PICKAXE)
                .criterion(hasItem(ModItems.CELESTIUM_UPGRADE_TEMPLATE), conditionsFromItem(ModItems.CELESTIUM_UPGRADE_TEMPLATE))
                .offerTo(recipeExporter, Identifier.of(Celestium.MOD_ID, "celestium_pickaxe"));

        SmithingTransformRecipeJsonBuilder.create(
                        Ingredient.ofItems(ModItems.CELESTIUM_UPGRADE_TEMPLATE),
                        Ingredient.ofItems(Items.NETHERITE_AXE),
                        Ingredient.ofItems(ModItems.CELESTIUM),
                        RecipeCategory.COMBAT,
                        ModItems.CELESTIUM_AXE)
                .criterion(hasItem(ModItems.CELESTIUM_UPGRADE_TEMPLATE), conditionsFromItem(ModItems.CELESTIUM_UPGRADE_TEMPLATE))
                .offerTo(recipeExporter, Identifier.of(Celestium.MOD_ID, "celestium_axe"));

        SmithingTransformRecipeJsonBuilder.create(
                        Ingredient.ofItems(ModItems.CELESTIUM_UPGRADE_TEMPLATE),
                        Ingredient.ofItems(Items.NETHERITE_SHOVEL),
                        Ingredient.ofItems(ModItems.CELESTIUM),
                        RecipeCategory.COMBAT,
                        ModItems.CELESTIUM_SHOVEL)
                .criterion(hasItem(ModItems.CELESTIUM_UPGRADE_TEMPLATE), conditionsFromItem(ModItems.CELESTIUM_UPGRADE_TEMPLATE))
                .offerTo(recipeExporter, Identifier.of(Celestium.MOD_ID, "celestium_shovel"));

        SmithingTransformRecipeJsonBuilder.create(
                        Ingredient.ofItems(ModItems.CELESTIUM_UPGRADE_TEMPLATE),
                        Ingredient.ofItems(Items.NETHERITE_HOE),
                        Ingredient.ofItems(ModItems.CELESTIUM),
                        RecipeCategory.COMBAT,
                        ModItems.CELESTIUM_HOE)
                .criterion(hasItem(ModItems.CELESTIUM_UPGRADE_TEMPLATE), conditionsFromItem(ModItems.CELESTIUM_UPGRADE_TEMPLATE))
                .offerTo(recipeExporter, Identifier.of(Celestium.MOD_ID, "celestium_hoe"));

        SmithingTransformRecipeJsonBuilder.create(
                        Ingredient.ofItems(ModItems.CELESTIUM_UPGRADE_TEMPLATE),
                        Ingredient.ofItems(Items.TRIDENT),
                        Ingredient.ofItems(ModItems.CELESTIUM),
                        RecipeCategory.COMBAT,
                        ModItems.CELESTIUM_SPEAR)
                .criterion(hasItem(ModItems.CELESTIUM_UPGRADE_TEMPLATE), conditionsFromItem(ModItems.CELESTIUM_UPGRADE_TEMPLATE))
                .offerTo(recipeExporter, Identifier.of(Celestium.MOD_ID, "celestium_spear"));

        SmithingTransformRecipeJsonBuilder.create(
                        Ingredient.ofItems(ModItems.CELESTIUM_UPGRADE_TEMPLATE),
                        Ingredient.ofItems(Items.DIAMOND_HORSE_ARMOR),
                        Ingredient.ofItems(ModItems.CELESTIUM),
                        RecipeCategory.COMBAT,
                        ModItems.CELESTIUM_HORSE_ARMOR)
                .criterion(hasItem(ModItems.CELESTIUM_UPGRADE_TEMPLATE), conditionsFromItem(ModItems.CELESTIUM_UPGRADE_TEMPLATE))
                .offerTo(recipeExporter, Identifier.of(Celestium.MOD_ID, "celestium_horse_armor"));
    }

    private static String hasItem(ItemConvertible item) {
        return "has_" + Registries.ITEM.getId(item.asItem()).getPath();
    }

    private static AdvancementCriterion<?> conditionsFromItem(ItemConvertible item) {
        return InventoryChangedCriterion.Conditions.items(item);
    }
}
