package net.inklinggamer.shopsandtools.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.inklinggamer.shopsandtools.block.ModBlocks;
import net.inklinggamer.shopsandtools.util.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import java.util.concurrent.CompletableFuture;

public class ModBlockTagProvider extends FabricTagsProvider.BlockTagsProvider {
    public ModBlockTagProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider wrapperLookup) {
        builder(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(blockKey(ModBlocks.CELESTIUM_BLOCK));

        builder(BlockTags.INCORRECT_FOR_DIAMOND_TOOL)
                .add(blockKey(ModBlocks.CELESTIUM_BLOCK));

        builder(ModTags.Blocks.CELESTIUM_SHOVEL_AREA_MINEABLE)
                .add(blockKey(Blocks.DIRT))
                .add(blockKey(Blocks.GRASS_BLOCK))
                .add(blockKey(Blocks.COARSE_DIRT))
                .add(blockKey(Blocks.PODZOL))
                .add(blockKey(Blocks.ROOTED_DIRT))
                .add(blockKey(Blocks.MYCELIUM))
                .add(blockKey(Blocks.DIRT_PATH))
                .add(blockKey(Blocks.MUD))
                .add(blockKey(Blocks.CLAY))
                .add(blockKey(Blocks.SAND))
                .add(blockKey(Blocks.RED_SAND))
                .add(blockKey(Blocks.GRAVEL))
                .add(blockKey(Blocks.SOUL_SAND))
                .add(blockKey(Blocks.SOUL_SOIL));

        builder(ModTags.Blocks.CELESTIUM_HOE_SUPPORTED_CROPS)
                .add(blockKey(Blocks.WHEAT))
                .add(blockKey(Blocks.CARROTS))
                .add(blockKey(Blocks.POTATOES))
                .add(blockKey(Blocks.BEETROOTS))
                .add(blockKey(Blocks.NETHER_WART));
    }

    private static ResourceKey<Block> blockKey(Block block) {
        return BuiltInRegistries.BLOCK.getResourceKey(block).orElseThrow();
    }
}
