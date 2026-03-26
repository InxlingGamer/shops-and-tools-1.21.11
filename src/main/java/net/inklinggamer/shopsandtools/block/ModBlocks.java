package net.inklinggamer.shopsandtools.block;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.inklinggamer.shopsandtools.ShopsAndTools;
import net.inklinggamer.shopsandtools.item.CelestiumBlockItem;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

public class ModBlocks {

    public static final Block CELESTIUM_BLOCK = registerBlock("celestium_block", createCelestiumSettings());

    static AbstractBlock.Settings createCelestiumSettings() {
        return copyBlockSettings(Blocks.PEARLESCENT_FROGLIGHT)
                .strength(Blocks.OBSIDIAN.getHardness(), Blocks.OBSIDIAN.getBlastResistance())
                .requiresTool()
                .sounds(BlockSoundGroup.AMETHYST_BLOCK);
    }

    static AbstractBlock.Settings copyBlockSettings(AbstractBlock block) {
        return AbstractBlock.Settings.copy(block);
    }

    private static Block registerBlock(String name, AbstractBlock.Settings blockSettings) {
        RegistryKey<Block> key = RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(ShopsAndTools.MOD_ID, name));
        Block block = new Block(blockSettings.registryKey(key));

        Registry.register(Registries.BLOCK, key, block);

        registerBlockItem(name, block);

        return block;
    }

    private static void registerBlockItem(String name, Block block) {
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(ShopsAndTools.MOD_ID, name));

        BlockItem item = "celestium_block".equals(name)
                ? new CelestiumBlockItem(block, new Item.Settings().registryKey(key).useBlockPrefixedTranslationKey())
                : new BlockItem(block, new Item.Settings().registryKey(key).useBlockPrefixedTranslationKey());

        Registry.register(Registries.ITEM, key, item);
    }

    public static void registerModBlocks() {
        ShopsAndTools.LOGGER.info("Registering Mod Blocks for " + ShopsAndTools.MOD_ID);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.BUILDING_BLOCKS).register(fabricItemGroupEntries -> {
            fabricItemGroupEntries.add(ModBlocks.CELESTIUM_BLOCK);
        });
    }
}
