package net.inklinggamer.shopsandtools.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.inklinggamer.shopsandtools.item.ModItems;
import net.inklinggamer.shopsandtools.item.ModToolMaterials;
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
        valueLookupBuilder(ItemTags.SWORDS).add(ModItems.CELESTIUM_SWORD);
        valueLookupBuilder(ItemTags.PICKAXES).add(ModItems.CELESTIUM_PICKAXE);
        valueLookupBuilder(ItemTags.AXES).add(ModItems.CELESTIUM_AXE);
        valueLookupBuilder(ItemTags.SHOVELS).add(ModItems.CELESTIUM_SHOVEL);
        valueLookupBuilder(ItemTags.HOES).add(ModItems.CELESTIUM_HOE);
        valueLookupBuilder(ItemTags.SPEARS).add(ModItems.CELESTIUM_SPEAR);

        // Armor tags
        valueLookupBuilder(ItemTags.TRIMMABLE_ARMOR)
                .add(ModItems.CELESTIUM_HELMET, ModItems.CELESTIUM_CHESTPLATE, ModItems.CELESTIUM_ELYTRA_CHESTPLATE, ModItems.CELESTIUM_LEGGINGS, ModItems.CELESTIUM_BOOTS);

        valueLookupBuilder(ItemTags.ARMOR_ENCHANTABLE)
                .add(ModItems.CELESTIUM_HELMET, ModItems.CELESTIUM_CHESTPLATE, ModItems.CELESTIUM_ELYTRA_CHESTPLATE, ModItems.CELESTIUM_LEGGINGS, ModItems.CELESTIUM_BOOTS);

        valueLookupBuilder(ItemTags.EQUIPPABLE_ENCHANTABLE)
                .add(ModItems.CELESTIUM_HELMET, ModItems.CELESTIUM_CHESTPLATE, ModItems.CELESTIUM_ELYTRA_CHESTPLATE, ModItems.CELESTIUM_LEGGINGS, ModItems.CELESTIUM_BOOTS, ModItems.CELESTIUM_HORSE_ARMOR);

        valueLookupBuilder(ItemTags.HEAD_ARMOR_ENCHANTABLE).add(ModItems.CELESTIUM_HELMET);
        valueLookupBuilder(ItemTags.CHEST_ARMOR_ENCHANTABLE).add(ModItems.CELESTIUM_CHESTPLATE, ModItems.CELESTIUM_ELYTRA_CHESTPLATE);
        valueLookupBuilder(ItemTags.LEG_ARMOR_ENCHANTABLE).add(ModItems.CELESTIUM_LEGGINGS);
        valueLookupBuilder(ItemTags.FOOT_ARMOR_ENCHANTABLE).add(ModItems.CELESTIUM_BOOTS);

        // Weapon enchant tags
        valueLookupBuilder(ItemTags.WEAPON_ENCHANTABLE)
                .add(ModItems.CELESTIUM_SWORD, ModItems.CELESTIUM_AXE, ModItems.CELESTIUM_SPEAR);

        valueLookupBuilder(ItemTags.MELEE_WEAPON_ENCHANTABLE)
                .add(ModItems.CELESTIUM_SWORD, ModItems.CELESTIUM_AXE, ModItems.CELESTIUM_SPEAR);

        valueLookupBuilder(ItemTags.SHARP_WEAPON_ENCHANTABLE)
                .add(ModItems.CELESTIUM_SWORD, ModItems.CELESTIUM_AXE);

        valueLookupBuilder(ItemTags.SWEEPING_ENCHANTABLE)
                .add(ModItems.CELESTIUM_SWORD);

        valueLookupBuilder(ItemTags.TRIDENT_ENCHANTABLE)
                .add(ModItems.CELESTIUM_SPEAR);

        valueLookupBuilder(ItemTags.LUNGE_ENCHANTABLE)
                .add(ModItems.CELESTIUM_SPEAR);

        // Mining enchant tags
        valueLookupBuilder(ItemTags.MINING_ENCHANTABLE)
                .add(ModItems.CELESTIUM_PICKAXE, ModItems.CELESTIUM_AXE, ModItems.CELESTIUM_SHOVEL, ModItems.CELESTIUM_HOE);

        valueLookupBuilder(ItemTags.MINING_LOOT_ENCHANTABLE)
                .add(ModItems.CELESTIUM_PICKAXE, ModItems.CELESTIUM_AXE, ModItems.CELESTIUM_SHOVEL, ModItems.CELESTIUM_HOE);

        // General durability / mending / vanishing style tags
        valueLookupBuilder(ItemTags.DURABILITY_ENCHANTABLE)
                .add(ModItems.CELESTIUM_HELMET, ModItems.CELESTIUM_CHESTPLATE, ModItems.CELESTIUM_ELYTRA_CHESTPLATE, ModItems.CELESTIUM_LEGGINGS, ModItems.CELESTIUM_BOOTS)
                .add(ModItems.CELESTIUM_SWORD, ModItems.CELESTIUM_PICKAXE, ModItems.CELESTIUM_AXE, ModItems.CELESTIUM_SHOVEL, ModItems.CELESTIUM_HOE, ModItems.CELESTIUM_SPEAR, ModItems.CELESTIUM_HORSE_ARMOR);

        valueLookupBuilder(ItemTags.VANISHING_ENCHANTABLE)
                .add(ModItems.CELESTIUM_HELMET, ModItems.CELESTIUM_CHESTPLATE, ModItems.CELESTIUM_ELYTRA_CHESTPLATE, ModItems.CELESTIUM_LEGGINGS, ModItems.CELESTIUM_BOOTS)
                .add(ModItems.CELESTIUM_SWORD, ModItems.CELESTIUM_PICKAXE, ModItems.CELESTIUM_AXE, ModItems.CELESTIUM_SHOVEL, ModItems.CELESTIUM_HOE, ModItems.CELESTIUM_SPEAR, ModItems.CELESTIUM_HORSE_ARMOR);

        // Repair tag
        valueLookupBuilder(ModToolMaterials.CELESTIUM_REPAIR).add(ModItems.CELESTIUM);
    }
}
