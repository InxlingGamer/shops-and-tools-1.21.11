package net.inklinggamer.shopsandtools.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.inklinggamer.shopsandtools.item.ModItems;
import net.inklinggamer.shopsandtools.item.ModToolMaterials;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import java.util.concurrent.CompletableFuture;

public class ModItemTagProvider extends FabricTagsProvider.ItemTagsProvider {
    public ModItemTagProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider wrapperLookup) {
        // Tool / weapon type tags
        builder(ItemTags.SWORDS).add(itemKey(ModItems.CELESTIUM_SWORD));
        builder(ItemTags.PICKAXES).add(itemKey(ModItems.CELESTIUM_PICKAXE));
        builder(ItemTags.AXES).add(itemKey(ModItems.CELESTIUM_AXE));
        builder(ItemTags.SHOVELS).add(itemKey(ModItems.CELESTIUM_SHOVEL));
        builder(ItemTags.HOES).add(itemKey(ModItems.CELESTIUM_HOE));
        builder(ItemTags.SPEARS).add(itemKey(ModItems.CELESTIUM_SPEAR));

        // Armor tags
        builder(ItemTags.ARMOR_ENCHANTABLE)
                .add(itemKey(ModItems.CELESTIUM_HELMET), itemKey(ModItems.CELESTIUM_CHESTPLATE), itemKey(ModItems.CELESTIUM_ELYTRA_CHESTPLATE), itemKey(ModItems.CELESTIUM_LEGGINGS), itemKey(ModItems.CELESTIUM_BOOTS));

        builder(ItemTags.EQUIPPABLE_ENCHANTABLE)
                .add(itemKey(ModItems.CELESTIUM_HELMET), itemKey(ModItems.CELESTIUM_CHESTPLATE), itemKey(ModItems.CELESTIUM_ELYTRA_CHESTPLATE), itemKey(ModItems.CELESTIUM_LEGGINGS), itemKey(ModItems.CELESTIUM_BOOTS), itemKey(ModItems.CELESTIUM_HORSE_ARMOR));

        builder(ItemTags.HEAD_ARMOR_ENCHANTABLE).add(itemKey(ModItems.CELESTIUM_HELMET));
        builder(ItemTags.CHEST_ARMOR_ENCHANTABLE).add(itemKey(ModItems.CELESTIUM_CHESTPLATE), itemKey(ModItems.CELESTIUM_ELYTRA_CHESTPLATE));
        builder(ItemTags.LEG_ARMOR_ENCHANTABLE).add(itemKey(ModItems.CELESTIUM_LEGGINGS));
        builder(ItemTags.FOOT_ARMOR_ENCHANTABLE).add(itemKey(ModItems.CELESTIUM_BOOTS));

        // Weapon enchant tags
        builder(ItemTags.WEAPON_ENCHANTABLE)
                .add(itemKey(ModItems.CELESTIUM_SWORD), itemKey(ModItems.CELESTIUM_AXE), itemKey(ModItems.CELESTIUM_SPEAR));

        builder(ItemTags.MELEE_WEAPON_ENCHANTABLE)
                .add(itemKey(ModItems.CELESTIUM_SWORD), itemKey(ModItems.CELESTIUM_AXE), itemKey(ModItems.CELESTIUM_SPEAR));

        builder(ItemTags.SHARP_WEAPON_ENCHANTABLE)
                .add(itemKey(ModItems.CELESTIUM_SWORD), itemKey(ModItems.CELESTIUM_AXE));

        builder(ItemTags.SWEEPING_ENCHANTABLE)
                .add(itemKey(ModItems.CELESTIUM_SWORD));

        builder(ItemTags.TRIDENT_ENCHANTABLE)
                .add(itemKey(ModItems.CELESTIUM_SPEAR));

        builder(ItemTags.LUNGE_ENCHANTABLE)
                .add(itemKey(ModItems.CELESTIUM_SPEAR));

        // Mining enchant tags
        builder(ItemTags.MINING_ENCHANTABLE)
                .add(itemKey(ModItems.CELESTIUM_PICKAXE), itemKey(ModItems.CELESTIUM_AXE), itemKey(ModItems.CELESTIUM_SHOVEL), itemKey(ModItems.CELESTIUM_HOE));

        builder(ItemTags.MINING_LOOT_ENCHANTABLE)
                .add(itemKey(ModItems.CELESTIUM_PICKAXE), itemKey(ModItems.CELESTIUM_AXE), itemKey(ModItems.CELESTIUM_SHOVEL), itemKey(ModItems.CELESTIUM_HOE));

        // General durability / mending / vanishing style tags
        builder(ItemTags.DURABILITY_ENCHANTABLE)
                .add(itemKey(ModItems.CELESTIUM_HELMET), itemKey(ModItems.CELESTIUM_CHESTPLATE), itemKey(ModItems.CELESTIUM_ELYTRA_CHESTPLATE), itemKey(ModItems.CELESTIUM_LEGGINGS), itemKey(ModItems.CELESTIUM_BOOTS))
                .add(itemKey(ModItems.CELESTIUM_SWORD), itemKey(ModItems.CELESTIUM_PICKAXE), itemKey(ModItems.CELESTIUM_AXE), itemKey(ModItems.CELESTIUM_SHOVEL), itemKey(ModItems.CELESTIUM_HOE), itemKey(ModItems.CELESTIUM_SPEAR), itemKey(ModItems.CELESTIUM_HORSE_ARMOR));

        builder(ItemTags.VANISHING_ENCHANTABLE)
                .add(itemKey(ModItems.CELESTIUM_HELMET), itemKey(ModItems.CELESTIUM_CHESTPLATE), itemKey(ModItems.CELESTIUM_ELYTRA_CHESTPLATE), itemKey(ModItems.CELESTIUM_LEGGINGS), itemKey(ModItems.CELESTIUM_BOOTS))
                .add(itemKey(ModItems.CELESTIUM_SWORD), itemKey(ModItems.CELESTIUM_PICKAXE), itemKey(ModItems.CELESTIUM_AXE), itemKey(ModItems.CELESTIUM_SHOVEL), itemKey(ModItems.CELESTIUM_HOE), itemKey(ModItems.CELESTIUM_SPEAR), itemKey(ModItems.CELESTIUM_HORSE_ARMOR));

        // Repair tag
        builder(ModToolMaterials.CELESTIUM_REPAIR).add(itemKey(ModItems.CELESTIUM));
    }

    private static ResourceKey<Item> itemKey(Item item) {
        return BuiltInRegistries.ITEM.getResourceKey(item).orElseThrow();
    }
}
