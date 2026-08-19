package net.inklinggamer.celestium.item;

import net.inklinggamer.celestium.Celestium;
import net.inklinggamer.celestium.util.ModTags;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;

public class ModToolMaterials {

    // 1. We must define a Tag for what item repairs Celestium tools in an anvil
    public static final TagKey<Item> CELESTIUM_REPAIR = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Celestium.MOD_ID, "celestium_repair"));

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
