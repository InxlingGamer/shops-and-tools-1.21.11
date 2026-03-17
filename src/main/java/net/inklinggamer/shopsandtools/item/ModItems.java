package net.inklinggamer.shopsandtools.item;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.inklinggamer.shopsandtools.ShopsAndTools;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.EnchantableComponent;
import net.minecraft.item.*;
import net.minecraft.item.equipment.EquipmentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;


public class ModItems {

    // ==========================================
    // BASIC ITEMS & MATERIALS
    // ==========================================
    public static final Item CELESTIUM = registerItem("celestium", new Item(new Item.Settings().fireproof().component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true).registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(ShopsAndTools.MOD_ID,"celestium")))));

    public static final Item WARDEN_HEART = registerItem("warden_heart", new Item(new Item.Settings().component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true).registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(ShopsAndTools.MOD_ID,"warden_heart")))));

    public static final Item CELESTIUM_UPGRADE_TEMPLATE = registerItem("celestium_upgrade_template", new Item(new Item.Settings().fireproof().rarity(Rarity.UNCOMMON).registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(ShopsAndTools.MOD_ID,"celestium_upgrade_template")))));


    // ==========================================
    // ARMOR (Using the new .armor() component)
    // ==========================================
    public static final Item CELESTIUM_HELMET = registerItem("celestium_helmet", new CelestiumHelmetItem(new Item.Settings().fireproof().registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(ShopsAndTools.MOD_ID,"celestium_helmet"))).armor(ModArmorMaterials.CELESTIUM, EquipmentType.HELMET).component(DataComponentTypes.ENCHANTABLE, new EnchantableComponent(30))));

    public static final Item CELESTIUM_CHESTPLATE = registerItem("celestium_chestplate", new Item(new Item.Settings().fireproof().registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(ShopsAndTools.MOD_ID,"celestium_chestplate"))).armor(ModArmorMaterials.CELESTIUM, EquipmentType.CHESTPLATE)));

    public static final Item CELESTIUM_LEGGINGS = registerItem("celestium_leggings", new Item(new Item.Settings().fireproof().registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(ShopsAndTools.MOD_ID,"celestium_leggings"))).armor(ModArmorMaterials.CELESTIUM, EquipmentType.LEGGINGS)));

    public static final Item CELESTIUM_BOOTS = registerItem("celestium_boots", new Item(new Item.Settings().fireproof().registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(ShopsAndTools.MOD_ID,"celestium_boots"))).armor(ModArmorMaterials.CELESTIUM, EquipmentType.BOOTS)));

    public static final Item CELESTIUM_HORSE_ARMOR = registerItem("celestium_horse_armor", new Item(new Item.Settings().fireproof().registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(ShopsAndTools.MOD_ID,"celestium_horse_armor"))).horseArmor(ModArmorMaterials.CELESTIUM)));


    // ==========================================
    // WEAPONS & TOOLS (Using the new modifiers)
    // ==========================================
    public static final Item CELESTIUM_SWORD = registerItem("celestium_sword", new Item(new Item.Settings().fireproof().registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(ShopsAndTools.MOD_ID,"celestium_sword"))).sword(ModToolMaterials.CELESTIUM, 3.0F, -1.9F)));

    public static final Item CELESTIUM_PICKAXE = registerItem("celestium_pickaxe", new Item(new Item.Settings().fireproof().registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(ShopsAndTools.MOD_ID,"celestium_pickaxe"))).pickaxe(ModToolMaterials.CELESTIUM, 1.0F, -2.8F)));

    // I applied the exact float numbers from the vanilla Diamond Spear, but linked it to your Celestium material!
    public static final Item CELESTIUM_SPEAR = registerItem("celestium_spear", new Item(new Item.Settings().fireproof().registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(ShopsAndTools.MOD_ID,"celestium_spear"))).spear(ModToolMaterials.CELESTIUM, 1.05F, 1.075F, 0.5F, 3.0F, 7.5F, 6.5F, 5.1F, 10.0F, 4.6F)));

    // Shovels, Axes, and Hoes still use their standalone classes per the vanilla snippet
    public static final Item CELESTIUM_SHOVEL = registerItem("celestium_shovel", new ShovelItem(ModToolMaterials.CELESTIUM, 1.5F, -3.0F, new Item.Settings().fireproof().registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(ShopsAndTools.MOD_ID,"celestium_shovel")))));

    public static final Item CELESTIUM_AXE = registerItem("celestium_axe", new AxeItem(ModToolMaterials.CELESTIUM, 5.0F, -3.0F, new Item.Settings().fireproof().registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(ShopsAndTools.MOD_ID,"celestium_axe")))));

    public static final Item CELESTIUM_HOE = registerItem("celestium_hoe", new HoeItem(ModToolMaterials.CELESTIUM, -4.0F, 0.0F, new Item.Settings().fireproof().registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(ShopsAndTools.MOD_ID,"celestium_hoe")))));
    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, Identifier.of(ShopsAndTools.MOD_ID, name), item);
    }

    public static void registerModItems() {
        ShopsAndTools.LOGGER.info("Registering items for" + ShopsAndTools.MOD_ID);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(fabricItemGroupEntries -> {
            fabricItemGroupEntries.add(CELESTIUM);
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(fabricItemGroupEntries -> {
            fabricItemGroupEntries.add(CELESTIUM_HELMET);
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(fabricItemGroupEntries -> {
            fabricItemGroupEntries.add(CELESTIUM_CHESTPLATE);
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(fabricItemGroupEntries -> {
            fabricItemGroupEntries.add(CELESTIUM_LEGGINGS);
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(fabricItemGroupEntries -> {
            fabricItemGroupEntries.add(CELESTIUM_BOOTS);
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(fabricItemGroupEntries -> {
            fabricItemGroupEntries.add(CELESTIUM_SWORD);
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(fabricItemGroupEntries -> {
            fabricItemGroupEntries.add(CELESTIUM_PICKAXE);
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(fabricItemGroupEntries -> {
            fabricItemGroupEntries.add(CELESTIUM_AXE);
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(fabricItemGroupEntries -> {
            fabricItemGroupEntries.add(CELESTIUM_SHOVEL);
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(fabricItemGroupEntries -> {
            fabricItemGroupEntries.add(CELESTIUM_HOE);
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(fabricItemGroupEntries -> {
            fabricItemGroupEntries.add(CELESTIUM_SPEAR);
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(fabricItemGroupEntries -> {
            fabricItemGroupEntries.add(CELESTIUM_HORSE_ARMOR);
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(fabricItemGroupEntries -> {
            fabricItemGroupEntries.add(WARDEN_HEART);
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(fabricItemGroupEntries -> {
            fabricItemGroupEntries.add(CELESTIUM_UPGRADE_TEMPLATE);
        });
    }

}
