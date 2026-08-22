package net.inklinggamer.celestium.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.inklinggamer.celestium.item.ModItems;
import net.inklinggamer.celestium.item.ModToolMaterials;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.ItemTags;

import java.util.concurrent.CompletableFuture;

public class ModItemTagProvider extends FabricTagProvider.ItemTagProvider {
    public ModItemTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
        // Tool / weapon type tags
        getOrCreateTagBuilder(ItemTags.SWORDS).add(ModItems.CELESTIUM_SWORD);
        getOrCreateTagBuilder(ItemTags.PICKAXES).add(ModItems.CELESTIUM_PICKAXE);
        getOrCreateTagBuilder(ItemTags.AXES).add(ModItems.CELESTIUM_AXE);
        getOrCreateTagBuilder(ItemTags.SHOVELS).add(ModItems.CELESTIUM_SHOVEL);
        getOrCreateTagBuilder(ItemTags.HOES).add(ModItems.CELESTIUM_HOE);

        // Armor tags
        getOrCreateTagBuilder(ItemTags.ARMOR_ENCHANTABLE)
                .add(ModItems.CELESTIUM_HELMET, ModItems.CELESTIUM_CHESTPLATE, ModItems.CELESTIUM_ELYTRA_CHESTPLATE, ModItems.CELESTIUM_LEGGINGS, ModItems.CELESTIUM_BOOTS);

        getOrCreateTagBuilder(ItemTags.EQUIPPABLE_ENCHANTABLE)
                .add(ModItems.CELESTIUM_HELMET, ModItems.CELESTIUM_CHESTPLATE, ModItems.CELESTIUM_ELYTRA_CHESTPLATE, ModItems.CELESTIUM_LEGGINGS, ModItems.CELESTIUM_BOOTS, ModItems.CELESTIUM_HORSE_ARMOR);

        getOrCreateTagBuilder(ItemTags.HEAD_ARMOR_ENCHANTABLE).add(ModItems.CELESTIUM_HELMET);
        getOrCreateTagBuilder(ItemTags.CHEST_ARMOR_ENCHANTABLE).add(ModItems.CELESTIUM_CHESTPLATE, ModItems.CELESTIUM_ELYTRA_CHESTPLATE);
        getOrCreateTagBuilder(ItemTags.LEG_ARMOR_ENCHANTABLE).add(ModItems.CELESTIUM_LEGGINGS);
        getOrCreateTagBuilder(ItemTags.FOOT_ARMOR_ENCHANTABLE).add(ModItems.CELESTIUM_BOOTS);

        // Weapon enchant tags
        getOrCreateTagBuilder(ItemTags.WEAPON_ENCHANTABLE)
                .add(ModItems.CELESTIUM_SWORD, ModItems.CELESTIUM_AXE, ModItems.CELESTIUM_SPEAR);

        getOrCreateTagBuilder(ItemTags.SHARP_WEAPON_ENCHANTABLE)
                .add(ModItems.CELESTIUM_SWORD, ModItems.CELESTIUM_AXE);

        getOrCreateTagBuilder(ItemTags.TRIDENT_ENCHANTABLE)
                .add(ModItems.CELESTIUM_SPEAR);

        // Mining enchant tags
        getOrCreateTagBuilder(ItemTags.MINING_ENCHANTABLE)
                .add(ModItems.CELESTIUM_PICKAXE, ModItems.CELESTIUM_AXE, ModItems.CELESTIUM_SHOVEL, ModItems.CELESTIUM_HOE);

        getOrCreateTagBuilder(ItemTags.MINING_LOOT_ENCHANTABLE)
                .add(ModItems.CELESTIUM_PICKAXE, ModItems.CELESTIUM_AXE, ModItems.CELESTIUM_SHOVEL, ModItems.CELESTIUM_HOE);

        // General durability / mending / vanishing style tags
        getOrCreateTagBuilder(ItemTags.DURABILITY_ENCHANTABLE)
                .add(ModItems.CELESTIUM_HELMET, ModItems.CELESTIUM_CHESTPLATE, ModItems.CELESTIUM_ELYTRA_CHESTPLATE, ModItems.CELESTIUM_LEGGINGS, ModItems.CELESTIUM_BOOTS)
                .add(ModItems.CELESTIUM_SWORD, ModItems.CELESTIUM_PICKAXE, ModItems.CELESTIUM_AXE, ModItems.CELESTIUM_SHOVEL, ModItems.CELESTIUM_HOE, ModItems.CELESTIUM_SPEAR, ModItems.CELESTIUM_HORSE_ARMOR);

        getOrCreateTagBuilder(ItemTags.VANISHING_ENCHANTABLE)
                .add(ModItems.CELESTIUM_HELMET, ModItems.CELESTIUM_CHESTPLATE, ModItems.CELESTIUM_ELYTRA_CHESTPLATE, ModItems.CELESTIUM_LEGGINGS, ModItems.CELESTIUM_BOOTS)
                .add(ModItems.CELESTIUM_SWORD, ModItems.CELESTIUM_PICKAXE, ModItems.CELESTIUM_AXE, ModItems.CELESTIUM_SHOVEL, ModItems.CELESTIUM_HOE, ModItems.CELESTIUM_SPEAR, ModItems.CELESTIUM_HORSE_ARMOR);

        // Repair tag
        getOrCreateTagBuilder(ModToolMaterials.CELESTIUM_REPAIR).add(ModItems.CELESTIUM);
    }
}
