package net.inklinggamer.celestium.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.inklinggamer.celestium.block.ModBlocks;
import net.inklinggamer.celestium.util.ModTags;
import net.minecraft.block.Blocks;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.BlockTags;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagProvider extends FabricTagProvider.BlockTagProvider {
    public ModBlockTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
        getOrCreateTagBuilder(BlockTags.PICKAXE_MINEABLE)
                .add(ModBlocks.CELESTIUM_BLOCK);

        getOrCreateTagBuilder(BlockTags.INCORRECT_FOR_DIAMOND_TOOL)
                .add(ModBlocks.CELESTIUM_BLOCK);

        // Celestium is the highest tool tier. Its inverse mining tag must exist,
        // but remains empty so no block is incorrectly marked unharvestable.
        getOrCreateTagBuilder(ModTags.Blocks.INCORRECT_FOR_CELESTIUM_TOOL);

        getOrCreateTagBuilder(ModTags.Blocks.CELESTIUM_SHOVEL_AREA_MINEABLE)
                .add(Blocks.DIRT)
                .add(Blocks.GRASS_BLOCK)
                .add(Blocks.COARSE_DIRT)
                .add(Blocks.PODZOL)
                .add(Blocks.ROOTED_DIRT)
                .add(Blocks.MYCELIUM)
                .add(Blocks.DIRT_PATH)
                .add(Blocks.MUD)
                .add(Blocks.CLAY)
                .add(Blocks.SAND)
                .add(Blocks.RED_SAND)
                .add(Blocks.GRAVEL)
                .add(Blocks.SOUL_SAND)
                .add(Blocks.SOUL_SOIL);

        getOrCreateTagBuilder(ModTags.Blocks.CELESTIUM_HOE_SUPPORTED_CROPS)
                .add(Blocks.WHEAT)
                .add(Blocks.CARROTS)
                .add(Blocks.POTATOES)
                .add(Blocks.BEETROOTS)
                .add(Blocks.NETHER_WART);
    }
}
