package net.inklinggamer.celestium.registry;

import net.inklinggamer.celestium.Celestium;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

public final class ItemIds {
    public static final ResourceKey<Item> CELESTIUM = key("celestium");
    public static final ResourceKey<Item> SKULK_VENOM = key("skulk_venom");
    public static final ResourceKey<Item> WARDEN_HEART = key("warden_heart");
    public static final ResourceKey<Item> CELESTIUM_UPGRADE_TEMPLATE = key("celestium_upgrade_template");
    public static final ResourceKey<Item> CELESTIUM_HELMET = key("celestium_helmet");
    public static final ResourceKey<Item> CELESTIUM_CHESTPLATE = key("celestium_chestplate");
    public static final ResourceKey<Item> CELESTIUM_ELYTRA_CHESTPLATE = key("celestium_elytra_chestplate");
    public static final ResourceKey<Item> CELESTIUM_LEGGINGS = key("celestium_leggings");
    public static final ResourceKey<Item> CELESTIUM_BOOTS = key("celestium_boots");
    public static final ResourceKey<Item> CELESTIUM_HORSE_ARMOR = key("celestium_horse_armor");
    public static final ResourceKey<Item> CELESTIUM_SWORD = key("celestium_sword");
    public static final ResourceKey<Item> CELESTIUM_PICKAXE = key("celestium_pickaxe");
    public static final ResourceKey<Item> CELESTIUM_SPEAR = key("celestium_spear");
    public static final ResourceKey<Item> CELESTIUM_SHOVEL = key("celestium_shovel");
    public static final ResourceKey<Item> CELESTIUM_AXE = key("celestium_axe");
    public static final ResourceKey<Item> CELESTIUM_HOE = key("celestium_hoe");

    private ItemIds() {
    }

    public static ResourceKey<Item> key(String path) {
        return ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Celestium.MOD_ID, path));
    }
}
