package net.inklinggamer.shopsandtools.util;

import net.inklinggamer.shopsandtools.ShopsAndTools;
import net.minecraft.block.Block;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class ModTags {

    public static class Blocks {

        public static final TagKey<Block> NEEDS_CELESTIUM_TOOL = createTag("needs_celestium_tool");
        public static final TagKey<Block> INCORRECT_FOR_CELESTIUM_TOOL = createTag("incorrect_for_celestium_tool");
        public static final TagKey<Block> CELESTIUM_SHOVEL_AREA_MINEABLE = createTag("celestium_shovel_area_mineable");


        private static TagKey<Block> createTag(String name) {
            return TagKey.of(RegistryKeys.BLOCK, Identifier.of(ShopsAndTools.MOD_ID, name));
        }
    }
}
