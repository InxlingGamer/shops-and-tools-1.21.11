package net.inklinggamer.celestium.util;

import net.inklinggamer.celestium.Celestium;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class ModTags {

    public static class Blocks {

        public static final TagKey<Block> NEEDS_CELESTIUM_TOOL = createTag("needs_celestium_tool");
        public static final TagKey<Block> INCORRECT_FOR_CELESTIUM_TOOL = createTag("incorrect_for_celestium_tool");
        public static final TagKey<Block> CELESTIUM_SHOVEL_AREA_MINEABLE = createTag("celestium_shovel_area_mineable");
        public static final TagKey<Block> CELESTIUM_HOE_SUPPORTED_CROPS = createTag("celestium_hoe_supported_crops");


        private static TagKey<Block> createTag(String name) {
            return TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(Celestium.MOD_ID, name));
        }
    }
}
