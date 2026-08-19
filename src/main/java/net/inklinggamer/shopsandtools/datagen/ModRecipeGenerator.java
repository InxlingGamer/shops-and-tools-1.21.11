package net.inklinggamer.shopsandtools.datagen;

import net.inklinggamer.shopsandtools.ShopsAndTools;
import net.inklinggamer.shopsandtools.block.ModBlocks;
import net.inklinggamer.shopsandtools.item.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SmithingTransformRecipeBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

public class ModRecipeGenerator extends RecipeProvider {

    // Store these locally so we can safely use them in the generate() method
    private final HolderLookup.Provider wrapperLookup;
    private final RecipeOutput recipeExporter;

    public ModRecipeGenerator(HolderLookup.Provider registries, RecipeOutput exporter) {
        super(registries, exporter);
        this.wrapperLookup = registries;
        this.recipeExporter = exporter;
    }

    @Override
    public void buildRecipes() {
        HolderLookup.RegistryLookup<Item> itemLookup = this.wrapperLookup.lookupOrThrow(Registries.ITEM);

        ShapedRecipeBuilder.shaped(itemLookup, RecipeCategory.MISC, ModItems.CELESTIUM, 1)
                .pattern("#N#")
                .pattern("DBE")
                .pattern("#H#")
                .define('#', Items.AMETHYST_SHARD)
                .define('N', Items.NETHER_STAR)
                .define('D', Items.DRAGON_BREATH)
                .define('B', Items.BLAZE_POWDER)
                .define('E', ModItems.WARDEN_HEART)
                .define('H', Items.HEAVY_CORE)
                .unlockedBy(getHasName(Items.NETHER_STAR), has(Items.NETHER_STAR))

                .save(this.recipeExporter, ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(ShopsAndTools.MOD_ID, "celestium")));

        ShapedRecipeBuilder.shaped(itemLookup, RecipeCategory.MISC, ModBlocks.CELESTIUM_BLOCK, 1)
                .pattern("###")
                .pattern("###")
                .pattern("###")
                .define('#', ModItems.CELESTIUM)
                .unlockedBy(getHasName(ModItems.CELESTIUM), has(ModItems.CELESTIUM))
                .save(this.recipeExporter, ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(ShopsAndTools.MOD_ID, "celestium_block")));

        ShapelessRecipeBuilder.shapeless(itemLookup, RecipeCategory.MISC, ModItems.CELESTIUM, 9)
                .requires(ModBlocks.CELESTIUM_BLOCK)
                .unlockedBy(getHasName(ModBlocks.CELESTIUM_BLOCK), has(ModBlocks.CELESTIUM_BLOCK))
                .save(this.recipeExporter, ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(ShopsAndTools.MOD_ID, "celestium_from_block")));

        ShapedRecipeBuilder.shaped(itemLookup, RecipeCategory.MISC, ModItems.CELESTIUM_UPGRADE_TEMPLATE, 2)
                .pattern("#d#")
                .pattern("#e#")
                .pattern("###")
                .define('#', Items.NETHERITE_INGOT)
                .define('d', ModItems.CELESTIUM_UPGRADE_TEMPLATE)
                .define('e', Items.AMETHYST_BLOCK)
                .unlockedBy(getHasName(ModItems.CELESTIUM_UPGRADE_TEMPLATE), has(ModItems.CELESTIUM_UPGRADE_TEMPLATE))
                .save(this.recipeExporter, ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(ShopsAndTools.MOD_ID, "celestium_upgrade_template_duplication")));

        ShapedRecipeBuilder.shaped(itemLookup, RecipeCategory.MISC, ModItems.CELESTIUM_UPGRADE_TEMPLATE, 1)
                .pattern("#o#")
                .pattern("#p#")
                .pattern("#l#")
                .define('#', Items.NETHERITE_INGOT)
                .define('o', Items.NETHER_STAR)
                .define('p', Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE)
                .define('l', Items.HEAVY_CORE)
                .unlockedBy(getHasName(Items.NETHER_STAR), has(Items.NETHER_STAR))
                .save(this.recipeExporter, ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(ShopsAndTools.MOD_ID, "celestium_upgrade_template")));

        SmithingTransformRecipeBuilder.smithing(
                        Ingredient.of(ModItems.CELESTIUM_UPGRADE_TEMPLATE),
                        Ingredient.of(Items.NETHERITE_HELMET),
                        Ingredient.of(ModItems.CELESTIUM),
                        RecipeCategory.COMBAT,
                        ModItems.CELESTIUM_HELMET)
                .unlocks(getHasName(ModItems.CELESTIUM_UPGRADE_TEMPLATE), has(ModItems.CELESTIUM_UPGRADE_TEMPLATE))
                .save(this.recipeExporter, ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(ShopsAndTools.MOD_ID, "celestium_helmet")));

        SmithingTransformRecipeBuilder.smithing(
                        Ingredient.of(ModItems.CELESTIUM_UPGRADE_TEMPLATE),
                        Ingredient.of(Items.NETHERITE_CHESTPLATE),
                        Ingredient.of(ModItems.CELESTIUM),
                        RecipeCategory.COMBAT,
                        ModItems.CELESTIUM_CHESTPLATE)
                .unlocks(getHasName(ModItems.CELESTIUM_UPGRADE_TEMPLATE), has(ModItems.CELESTIUM_UPGRADE_TEMPLATE))
                .save(this.recipeExporter, ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(ShopsAndTools.MOD_ID, "celestium_chestplate")));

        SmithingTransformRecipeBuilder.smithing(
                        Ingredient.of(ModItems.CELESTIUM_UPGRADE_TEMPLATE),
                        Ingredient.of(Items.NETHERITE_LEGGINGS),
                        Ingredient.of(ModItems.CELESTIUM),
                        RecipeCategory.COMBAT,
                        ModItems.CELESTIUM_LEGGINGS)
                .unlocks(getHasName(ModItems.CELESTIUM_UPGRADE_TEMPLATE), has(ModItems.CELESTIUM_UPGRADE_TEMPLATE))
                .save(this.recipeExporter, ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(ShopsAndTools.MOD_ID, "celestium_leggings")));

        SmithingTransformRecipeBuilder.smithing(
                        Ingredient.of(ModItems.CELESTIUM_UPGRADE_TEMPLATE),
                        Ingredient.of(Items.NETHERITE_BOOTS),
                        Ingredient.of(ModItems.CELESTIUM),
                        RecipeCategory.COMBAT,
                        ModItems.CELESTIUM_BOOTS)
                .unlocks(getHasName(ModItems.CELESTIUM_UPGRADE_TEMPLATE), has(ModItems.CELESTIUM_UPGRADE_TEMPLATE))
                .save(this.recipeExporter, ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(ShopsAndTools.MOD_ID, "celestium_boots")));

        SmithingTransformRecipeBuilder.smithing(
                        Ingredient.of(ModItems.CELESTIUM_UPGRADE_TEMPLATE),
                        Ingredient.of(Items.NETHERITE_SWORD),
                        Ingredient.of(ModItems.CELESTIUM),
                        RecipeCategory.COMBAT,
                        ModItems.CELESTIUM_SWORD)
                .unlocks(getHasName(ModItems.CELESTIUM_UPGRADE_TEMPLATE), has(ModItems.CELESTIUM_UPGRADE_TEMPLATE))
                .save(this.recipeExporter, ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(ShopsAndTools.MOD_ID, "celestium_sword")));

        SmithingTransformRecipeBuilder.smithing(
                        Ingredient.of(ModItems.CELESTIUM_UPGRADE_TEMPLATE),
                        Ingredient.of(Items.NETHERITE_PICKAXE),
                        Ingredient.of(ModItems.CELESTIUM),
                        RecipeCategory.COMBAT,
                        ModItems.CELESTIUM_PICKAXE)
                .unlocks(getHasName(ModItems.CELESTIUM_UPGRADE_TEMPLATE), has(ModItems.CELESTIUM_UPGRADE_TEMPLATE))
                .save(this.recipeExporter, ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(ShopsAndTools.MOD_ID, "celestium_pickaxe")));

        SmithingTransformRecipeBuilder.smithing(
                        Ingredient.of(ModItems.CELESTIUM_UPGRADE_TEMPLATE),
                        Ingredient.of(Items.NETHERITE_AXE),
                        Ingredient.of(ModItems.CELESTIUM),
                        RecipeCategory.COMBAT,
                        ModItems.CELESTIUM_AXE)
                .unlocks(getHasName(ModItems.CELESTIUM_UPGRADE_TEMPLATE), has(ModItems.CELESTIUM_UPGRADE_TEMPLATE))
                .save(this.recipeExporter, ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(ShopsAndTools.MOD_ID, "celestium_axe")));

        SmithingTransformRecipeBuilder.smithing(
                        Ingredient.of(ModItems.CELESTIUM_UPGRADE_TEMPLATE),
                        Ingredient.of(Items.NETHERITE_SHOVEL),
                        Ingredient.of(ModItems.CELESTIUM),
                        RecipeCategory.COMBAT,
                        ModItems.CELESTIUM_SHOVEL)
                .unlocks(getHasName(ModItems.CELESTIUM_UPGRADE_TEMPLATE), has(ModItems.CELESTIUM_UPGRADE_TEMPLATE))
                .save(this.recipeExporter, ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(ShopsAndTools.MOD_ID, "celestium_shovel")));

        SmithingTransformRecipeBuilder.smithing(
                        Ingredient.of(ModItems.CELESTIUM_UPGRADE_TEMPLATE),
                        Ingredient.of(Items.NETHERITE_HOE),
                        Ingredient.of(ModItems.CELESTIUM),
                        RecipeCategory.COMBAT,
                        ModItems.CELESTIUM_HOE)
                .unlocks(getHasName(ModItems.CELESTIUM_UPGRADE_TEMPLATE), has(ModItems.CELESTIUM_UPGRADE_TEMPLATE))
                .save(this.recipeExporter, ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(ShopsAndTools.MOD_ID, "celestium_hoe")));

        SmithingTransformRecipeBuilder.smithing(
                        Ingredient.of(ModItems.CELESTIUM_UPGRADE_TEMPLATE),
                        Ingredient.of(Items.NETHERITE_SPEAR),
                        Ingredient.of(ModItems.CELESTIUM),
                        RecipeCategory.COMBAT,
                        ModItems.CELESTIUM_SPEAR)
                .unlocks(getHasName(ModItems.CELESTIUM_UPGRADE_TEMPLATE), has(ModItems.CELESTIUM_UPGRADE_TEMPLATE))
                .save(this.recipeExporter, ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(ShopsAndTools.MOD_ID, "celestium_spear")));

        SmithingTransformRecipeBuilder.smithing(
                        Ingredient.of(ModItems.CELESTIUM_UPGRADE_TEMPLATE),
                        Ingredient.of(Items.NETHERITE_HORSE_ARMOR),
                        Ingredient.of(ModItems.CELESTIUM),
                        RecipeCategory.COMBAT,
                        ModItems.CELESTIUM_HORSE_ARMOR)
                .unlocks(getHasName(ModItems.CELESTIUM_UPGRADE_TEMPLATE), has(ModItems.CELESTIUM_UPGRADE_TEMPLATE))
                .save(this.recipeExporter, ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(ShopsAndTools.MOD_ID, "celestium_horse_armor")));
    }
}
