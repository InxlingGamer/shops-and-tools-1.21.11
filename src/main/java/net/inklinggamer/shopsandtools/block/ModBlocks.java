package net.inklinggamer.shopsandtools.block;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.inklinggamer.shopsandtools.ShopsAndTools;
import net.inklinggamer.shopsandtools.item.CelestiumBlockItem;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class ModBlocks {

    public static final Block CELESTIUM_BLOCK = registerBlock("celestium_block", createCelestiumSettings());

    static BlockBehaviour.Properties createCelestiumSettings() {
        return copyBlockSettings(Blocks.PEARLESCENT_FROGLIGHT)
                .strength(Blocks.OBSIDIAN.defaultDestroyTime(), Blocks.OBSIDIAN.getExplosionResistance())
                .requiresCorrectToolForDrops()
                .sound(SoundType.AMETHYST);
    }

    static BlockBehaviour.Properties copyBlockSettings(BlockBehaviour block) {
        return BlockBehaviour.Properties.ofFullCopy(block);
    }

    private static Block registerBlock(String name, BlockBehaviour.Properties blockSettings) {
        ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(ShopsAndTools.MOD_ID, name));
        Block block = new Block(blockSettings.setId(key));

        Registry.register(BuiltInRegistries.BLOCK, key, block);

        registerBlockItem(name, block);

        return block;
    }

    private static void registerBlockItem(String name, Block block) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(ShopsAndTools.MOD_ID, name));

        BlockItem item = "celestium_block".equals(name)
                ? new CelestiumBlockItem(block, new Item.Properties().setId(key).useBlockDescriptionPrefix())
                : new BlockItem(block, new Item.Properties().setId(key).useBlockDescriptionPrefix());

        Registry.register(BuiltInRegistries.ITEM, key, item);
    }

    public static void registerModBlocks() {
        ShopsAndTools.LOGGER.info("Registering Mod Blocks for " + ShopsAndTools.MOD_ID);

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.BUILDING_BLOCKS).register(fabricItemGroupEntries -> {
            fabricItemGroupEntries.accept(ModBlocks.CELESTIUM_BLOCK);
        });
    }
}
