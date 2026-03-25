package net.inklinggamer.shopsandtools.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.inklinggamer.shopsandtools.block.ModBlocks;
import net.inklinggamer.shopsandtools.util.ModTags;
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
        valueLookupBuilder(BlockTags.PICKAXE_MINEABLE)
                .add(ModBlocks.CELESTIUM_BLOCK);

        valueLookupBuilder(BlockTags.INCORRECT_FOR_DIAMOND_TOOL)
                .add(ModBlocks.CELESTIUM_BLOCK);

        valueLookupBuilder(ModTags.Blocks.CELESTIUM_SHOVEL_AREA_MINEABLE)
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

        valueLookupBuilder(ModTags.Blocks.CELESTIUM_HOE_SUPPORTED_CROPS)
                .add(Blocks.WHEAT)
                .add(Blocks.CARROTS)
                .add(Blocks.POTATOES)
                .add(Blocks.BEETROOTS)
                .add(Blocks.NETHER_WART);
    }
}
