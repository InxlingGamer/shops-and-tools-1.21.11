package net.inklinggamer.shopsandtools.item;

import net.inklinggamer.shopsandtools.ShopsAndTools;
import net.inklinggamer.shopsandtools.util.ModTags;
import net.minecraft.item.Item;
import net.minecraft.item.ToolMaterial;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class ModToolMaterials {

    // 1. We must define a Tag for what item repairs Celestium tools in an anvil
    public static final TagKey<Item> CELESTIUM_REPAIR = TagKey.of(RegistryKeys.ITEM, Identifier.of(ShopsAndTools.MOD_ID, "celestium_repair"));

    // 2. We instantiate the ToolMaterial directly using the new 1.21 format!
    public static final ToolMaterial CELESTIUM = new ToolMaterial(
            ModTags.Blocks.INCORRECT_FOR_CELESTIUM_TOOL, // What tools fail to mine this tier's blocks
            4064,                                 // Durability (Netherite is 2031)
            12.0f,                                // Mining Speed (Netherite is 9.0f)
            6.0f,                                 // Base Attack Damage Bonus
            30,                                   // Enchantability (Higher = better enchantments)
            CELESTIUM_REPAIR                      // The repair tag we made above
    );
}
