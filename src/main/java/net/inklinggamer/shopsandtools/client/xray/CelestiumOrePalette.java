package net.inklinggamer.shopsandtools.client.xray;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;

import java.util.Map;

public final class CelestiumOrePalette {
    private static final OreColor DIAMOND = new OreColor(102, 255, 255);
    private static final OreColor GOLD = new OreColor(255, 215, 0);
    private static final OreColor LAPIS = new OreColor(38, 97, 156);
    private static final OreColor REDSTONE = new OreColor(255, 48, 48);
    private static final OreColor EMERALD = new OreColor(0, 214, 96);
    private static final OreColor IRON = new OreColor(196, 196, 196);
    private static final OreColor COAL = new OreColor(125, 0, 150);
    private static final OreColor COPPER = new OreColor(242, 105, 0);
    private static final OreColor QUARTZ = new OreColor(245, 245, 245);
    private static final OreColor ANCIENT_DEBRIS = new OreColor(168, 0, 159);

    private static final Map<Block, OreColor> ORE_COLORS = Map.ofEntries(
            Map.entry(Blocks.DIAMOND_ORE, DIAMOND),
            Map.entry(Blocks.DEEPSLATE_DIAMOND_ORE, DIAMOND),
            Map.entry(Blocks.GOLD_ORE, GOLD),
            Map.entry(Blocks.DEEPSLATE_GOLD_ORE, GOLD),
            Map.entry(Blocks.NETHER_GOLD_ORE, GOLD),
            Map.entry(Blocks.LAPIS_ORE, LAPIS),
            Map.entry(Blocks.DEEPSLATE_LAPIS_ORE, LAPIS),
            Map.entry(Blocks.REDSTONE_ORE, REDSTONE),
            Map.entry(Blocks.DEEPSLATE_REDSTONE_ORE, REDSTONE),
            Map.entry(Blocks.EMERALD_ORE, EMERALD),
            Map.entry(Blocks.DEEPSLATE_EMERALD_ORE, EMERALD),
            Map.entry(Blocks.IRON_ORE, IRON),
            Map.entry(Blocks.DEEPSLATE_IRON_ORE, IRON),
            Map.entry(Blocks.COAL_ORE, COAL),
            Map.entry(Blocks.DEEPSLATE_COAL_ORE, COAL),
            Map.entry(Blocks.COPPER_ORE, COPPER),
            Map.entry(Blocks.DEEPSLATE_COPPER_ORE, COPPER),
            Map.entry(Blocks.NETHER_QUARTZ_ORE, QUARTZ),
            Map.entry(Blocks.ANCIENT_DEBRIS, ANCIENT_DEBRIS)
    );

    private CelestiumOrePalette() {
    }

    public static OreColor getColor(Block block) {
        return ORE_COLORS.get(block);
    }
}
