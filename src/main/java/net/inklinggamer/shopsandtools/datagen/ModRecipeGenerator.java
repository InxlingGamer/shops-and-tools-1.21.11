package net.inklinggamer.shopsandtools.datagen;

import net.inklinggamer.shopsandtools.ShopsAndTools;
import net.inklinggamer.shopsandtools.block.ModBlocks;
import net.inklinggamer.shopsandtools.item.ModItems;
import net.minecraft.data.recipe.*;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;

public class ModRecipeGenerator extends RecipeGenerator {

    // Store these locally so we can safely use them in the generate() method
    private final RegistryWrapper.WrapperLookup wrapperLookup;
    private final RecipeExporter recipeExporter;

    public ModRecipeGenerator(RegistryWrapper.WrapperLookup registries, RecipeExporter exporter) {
        super(registries, exporter);
        this.wrapperLookup = registries;
        this.recipeExporter = exporter;
    }

    @Override
    public void generate() {
        RegistryWrapper.Impl<Item> itemLookup = this.wrapperLookup.getOrThrow(RegistryKeys.ITEM);

        ShapedRecipeJsonBuilder.create(itemLookup, RecipeCategory.MISC, ModItems.CELESTIUM, 1)
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

                .offerTo(this.recipeExporter, RegistryKey.of(RegistryKeys.RECIPE, Identifier.of(ShopsAndTools.MOD_ID, "celestium")));

        ShapedRecipeJsonBuilder.create(itemLookup, RecipeCategory.MISC, ModBlocks.CELESTIUM_BLOCK, 1)
                .pattern("###")
                .pattern("###")
                .pattern("###")
                .input('#', ModItems.CELESTIUM)
                .criterion(hasItem(ModItems.CELESTIUM), conditionsFromItem(ModItems.CELESTIUM))
                .offerTo(this.recipeExporter, RegistryKey.of(RegistryKeys.RECIPE, Identifier.of(ShopsAndTools.MOD_ID, "celestium_block")));

        ShapelessRecipeJsonBuilder.create(itemLookup, RecipeCategory.MISC, ModItems.CELESTIUM, 9)
                .input(ModBlocks.CELESTIUM_BLOCK)
                .criterion(hasItem(ModBlocks.CELESTIUM_BLOCK), conditionsFromItem(ModBlocks.CELESTIUM_BLOCK))
                .offerTo(this.recipeExporter, RegistryKey.of(RegistryKeys.RECIPE, Identifier.of(ShopsAndTools.MOD_ID, "celestium_from_block")));

        ShapedRecipeJsonBuilder.create(itemLookup, RecipeCategory.MISC, ModItems.CELESTIUM_UPGRADE_TEMPLATE, 2)
                .pattern("#d#")
                .pattern("#e#")
                .pattern("###")
                .input('#', Items.NETHERITE_INGOT)
                .input('d', ModItems.CELESTIUM_UPGRADE_TEMPLATE)
                .input('e', Items.AMETHYST_BLOCK)
                .criterion(hasItem(ModItems.CELESTIUM_UPGRADE_TEMPLATE), conditionsFromItem(ModItems.CELESTIUM_UPGRADE_TEMPLATE))
                .offerTo(this.recipeExporter, RegistryKey.of(RegistryKeys.RECIPE, Identifier.of(ShopsAndTools.MOD_ID, "celestium_upgrade_template_duplication")));

        ShapedRecipeJsonBuilder.create(itemLookup, RecipeCategory.MISC, ModItems.CELESTIUM_UPGRADE_TEMPLATE, 1)
                .pattern("#o#")
                .pattern("#p#")
                .pattern("#l#")
                .input('#', Items.NETHERITE_INGOT)
                .input('o', Items.NETHER_STAR)
                .input('p', Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE)
                .input('l', Items.HEAVY_CORE)
                .criterion(hasItem(Items.NETHER_STAR), conditionsFromItem(Items.NETHER_STAR))
                .offerTo(this.recipeExporter, RegistryKey.of(RegistryKeys.RECIPE, Identifier.of(ShopsAndTools.MOD_ID, "celestium_upgrade_template")));

        SmithingTransformRecipeJsonBuilder.create(
                        Ingredient.ofItems(ModItems.CELESTIUM_UPGRADE_TEMPLATE),
                        Ingredient.ofItems(Items.NETHERITE_HELMET),
                        Ingredient.ofItems(ModItems.CELESTIUM),
                        RecipeCategory.COMBAT,
                        ModItems.CELESTIUM_HELMET)
                .criterion(hasItem(ModItems.CELESTIUM_UPGRADE_TEMPLATE), conditionsFromItem(ModItems.CELESTIUM_UPGRADE_TEMPLATE))
                .offerTo(this.recipeExporter, RegistryKey.of(RegistryKeys.RECIPE, Identifier.of(ShopsAndTools.MOD_ID, "celestium_helmet")));

        SmithingTransformRecipeJsonBuilder.create(
                        Ingredient.ofItems(ModItems.CELESTIUM_UPGRADE_TEMPLATE),
                        Ingredient.ofItems(Items.NETHERITE_CHESTPLATE),
                        Ingredient.ofItems(ModItems.CELESTIUM),
                        RecipeCategory.COMBAT,
                        ModItems.CELESTIUM_CHESTPLATE)
                .criterion(hasItem(ModItems.CELESTIUM_UPGRADE_TEMPLATE), conditionsFromItem(ModItems.CELESTIUM_UPGRADE_TEMPLATE))
                .offerTo(this.recipeExporter, RegistryKey.of(RegistryKeys.RECIPE, Identifier.of(ShopsAndTools.MOD_ID, "celestium_chestplate")));

        SmithingTransformRecipeJsonBuilder.create(
                        Ingredient.ofItems(ModItems.CELESTIUM_UPGRADE_TEMPLATE),
                        Ingredient.ofItems(Items.NETHERITE_LEGGINGS),
                        Ingredient.ofItems(ModItems.CELESTIUM),
                        RecipeCategory.COMBAT,
                        ModItems.CELESTIUM_LEGGINGS)
                .criterion(hasItem(ModItems.CELESTIUM_UPGRADE_TEMPLATE), conditionsFromItem(ModItems.CELESTIUM_UPGRADE_TEMPLATE))
                .offerTo(this.recipeExporter, RegistryKey.of(RegistryKeys.RECIPE, Identifier.of(ShopsAndTools.MOD_ID, "celestium_leggings")));

        SmithingTransformRecipeJsonBuilder.create(
                        Ingredient.ofItems(ModItems.CELESTIUM_UPGRADE_TEMPLATE),
                        Ingredient.ofItems(Items.NETHERITE_BOOTS),
                        Ingredient.ofItems(ModItems.CELESTIUM),
                        RecipeCategory.COMBAT,
                        ModItems.CELESTIUM_BOOTS)
                .criterion(hasItem(ModItems.CELESTIUM_UPGRADE_TEMPLATE), conditionsFromItem(ModItems.CELESTIUM_UPGRADE_TEMPLATE))
                .offerTo(this.recipeExporter, RegistryKey.of(RegistryKeys.RECIPE, Identifier.of(ShopsAndTools.MOD_ID, "celestium_boots")));

        SmithingTransformRecipeJsonBuilder.create(
                        Ingredient.ofItems(ModItems.CELESTIUM_UPGRADE_TEMPLATE),
                        Ingredient.ofItems(Items.NETHERITE_SWORD),
                        Ingredient.ofItems(ModItems.CELESTIUM),
                        RecipeCategory.COMBAT,
                        ModItems.CELESTIUM_SWORD)
                .criterion(hasItem(ModItems.CELESTIUM_UPGRADE_TEMPLATE), conditionsFromItem(ModItems.CELESTIUM_UPGRADE_TEMPLATE))
                .offerTo(this.recipeExporter, RegistryKey.of(RegistryKeys.RECIPE, Identifier.of(ShopsAndTools.MOD_ID, "celestium_sword")));

        SmithingTransformRecipeJsonBuilder.create(
                        Ingredient.ofItems(ModItems.CELESTIUM_UPGRADE_TEMPLATE),
                        Ingredient.ofItems(Items.NETHERITE_PICKAXE),
                        Ingredient.ofItems(ModItems.CELESTIUM),
                        RecipeCategory.COMBAT,
                        ModItems.CELESTIUM_PICKAXE)
                .criterion(hasItem(ModItems.CELESTIUM_UPGRADE_TEMPLATE), conditionsFromItem(ModItems.CELESTIUM_UPGRADE_TEMPLATE))
                .offerTo(this.recipeExporter, RegistryKey.of(RegistryKeys.RECIPE, Identifier.of(ShopsAndTools.MOD_ID, "celestium_pickaxe")));

        SmithingTransformRecipeJsonBuilder.create(
                        Ingredient.ofItems(ModItems.CELESTIUM_UPGRADE_TEMPLATE),
                        Ingredient.ofItems(Items.NETHERITE_AXE),
                        Ingredient.ofItems(ModItems.CELESTIUM),
                        RecipeCategory.COMBAT,
                        ModItems.CELESTIUM_AXE)
                .criterion(hasItem(ModItems.CELESTIUM_UPGRADE_TEMPLATE), conditionsFromItem(ModItems.CELESTIUM_UPGRADE_TEMPLATE))
                .offerTo(this.recipeExporter, RegistryKey.of(RegistryKeys.RECIPE, Identifier.of(ShopsAndTools.MOD_ID, "celestium_axe")));

        SmithingTransformRecipeJsonBuilder.create(
                        Ingredient.ofItems(ModItems.CELESTIUM_UPGRADE_TEMPLATE),
                        Ingredient.ofItems(Items.NETHERITE_SHOVEL),
                        Ingredient.ofItems(ModItems.CELESTIUM),
                        RecipeCategory.COMBAT,
                        ModItems.CELESTIUM_SHOVEL)
                .criterion(hasItem(ModItems.CELESTIUM_UPGRADE_TEMPLATE), conditionsFromItem(ModItems.CELESTIUM_UPGRADE_TEMPLATE))
                .offerTo(this.recipeExporter, RegistryKey.of(RegistryKeys.RECIPE, Identifier.of(ShopsAndTools.MOD_ID, "celestium_shovel")));

        SmithingTransformRecipeJsonBuilder.create(
                        Ingredient.ofItems(ModItems.CELESTIUM_UPGRADE_TEMPLATE),
                        Ingredient.ofItems(Items.NETHERITE_HOE),
                        Ingredient.ofItems(ModItems.CELESTIUM),
                        RecipeCategory.COMBAT,
                        ModItems.CELESTIUM_HOE)
                .criterion(hasItem(ModItems.CELESTIUM_UPGRADE_TEMPLATE), conditionsFromItem(ModItems.CELESTIUM_UPGRADE_TEMPLATE))
                .offerTo(this.recipeExporter, RegistryKey.of(RegistryKeys.RECIPE, Identifier.of(ShopsAndTools.MOD_ID, "celestium_hoe")));

        SmithingTransformRecipeJsonBuilder.create(
                        Ingredient.ofItems(ModItems.CELESTIUM_UPGRADE_TEMPLATE),
                        Ingredient.ofItems(Items.NETHERITE_SPEAR),
                        Ingredient.ofItems(ModItems.CELESTIUM),
                        RecipeCategory.COMBAT,
                        ModItems.CELESTIUM_SPEAR)
                .criterion(hasItem(ModItems.CELESTIUM_UPGRADE_TEMPLATE), conditionsFromItem(ModItems.CELESTIUM_UPGRADE_TEMPLATE))
                .offerTo(this.recipeExporter, RegistryKey.of(RegistryKeys.RECIPE, Identifier.of(ShopsAndTools.MOD_ID, "celestium_spear")));

        SmithingTransformRecipeJsonBuilder.create(
                        Ingredient.ofItems(ModItems.CELESTIUM_UPGRADE_TEMPLATE),
                        Ingredient.ofItems(Items.NETHERITE_HORSE_ARMOR),
                        Ingredient.ofItems(ModItems.CELESTIUM),
                        RecipeCategory.COMBAT,
                        ModItems.CELESTIUM_HORSE_ARMOR)
                .criterion(hasItem(ModItems.CELESTIUM_UPGRADE_TEMPLATE), conditionsFromItem(ModItems.CELESTIUM_UPGRADE_TEMPLATE))
                .offerTo(this.recipeExporter, RegistryKey.of(RegistryKeys.RECIPE, Identifier.of(ShopsAndTools.MOD_ID, "celestium_horse_armor")));
    }
}
