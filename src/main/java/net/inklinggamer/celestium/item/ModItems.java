package net.inklinggamer.celestium.item;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.inklinggamer.celestium.Celestium;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.*;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import java.util.List;

public class ModItems {

    // ==========================================
    // BASIC ITEMS & MATERIALS
    // ==========================================
    public static final Item CELESTIUM = registerItem("celestium", new CelestiumItem(new Item.Settings().fireproof().component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)));

    public static final Item SKULK_VENOM = registerItem("skulk_venom", new SkulkVenomItem(new Item.Settings().maxCount(1).component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)) {
        @Override
        public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
            // 1. The Subtitle
            tooltip.add(Text.literal("A needy little symbiote from the Deep Dark.").formatted(Formatting.GRAY));
            tooltip.add(Text.empty());
            tooltip.add(Text.literal("Grants godly power, but bills you by the second.").formatted(Formatting.DARK_AQUA));
        }
    });

    public static final Item WARDEN_HEART = registerItem("warden_heart", new Item(new Item.Settings().component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)));

    public static final Item CELESTIUM_UPGRADE_TEMPLATE = registerItem("celestium_upgrade_template", new Item(new Item.Settings().fireproof().rarity(Rarity.UNCOMMON)) {

        @Override
        public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
            // 1. The Subtitle
            tooltip.add(Text.literal("Smithing Template").formatted(Formatting.GRAY));

            // 2. A blank line to separate the title from the stats
            tooltip.add(Text.empty());

            // 3. The "Applies to:" section
            tooltip.add(Text.literal("Applies to:").formatted(Formatting.GRAY));
            tooltip.add(Text.literal(" Netherite Equipment").formatted(Formatting.BLUE));

            // 4. The "Ingredients:" section
            tooltip.add(Text.literal("Ingredients:").formatted(Formatting.GRAY));
            tooltip.add(Text.literal(" Celestium").formatted(Formatting.LIGHT_PURPLE));
        }
    });

    // ==========================================
    // ARMOR (Using the new .armor() component)
    // ==========================================
    public static final Item CELESTIUM_HELMET = registerItem("celestium_helmet", new CelestiumHelmetItem(armorSettings(ArmorItem.Type.HELMET)));

    public static final Item CELESTIUM_CHESTPLATE = registerItem("celestium_chestplate", new CelestiumChestItem(createCelestiumChestplateSettings("celestium_chestplate")));

    public static final Item CELESTIUM_ELYTRA_CHESTPLATE = registerItem("celestium_elytra_chestplate", new CelestiumChestItem(createCelestiumElytraChestplateSettings()));

    public static final Item CELESTIUM_LEGGINGS = registerItem("celestium_leggings", new CelestiumLeggingsItem(armorSettings(ArmorItem.Type.LEGGINGS)));

    public static final Item CELESTIUM_BOOTS = registerItem("celestium_boots", new CelestiumBootsItem(armorSettings(ArmorItem.Type.BOOTS)));

    public static final Item CELESTIUM_HORSE_ARMOR = registerItem("celestium_horse_armor", new CelestiumHorseArmorItem(new Item.Settings().fireproof().maxCount(1)));


    // ==========================================
    // WEAPONS & TOOLS (Using the new modifiers)
    // ==========================================
    public static final Item CELESTIUM_SWORD = registerItem("celestium_sword", new SwordItem(ModToolMaterials.CELESTIUM, toolSettings().attributeModifiers(SwordItem.createAttributeModifiers(ModToolMaterials.CELESTIUM, 3, -1.9F))));

    public static final Item CELESTIUM_PICKAXE = registerItem("celestium_pickaxe", new CelestiumPickaxeItem(toolSettings().attributeModifiers(MiningToolItem.createAttributeModifiers(ModToolMaterials.CELESTIUM, 1.0F, -2.8F))));

    public static final Item CELESTIUM_SPEAR = registerItem("celestium_spear", new CelestiumSpearItem(toolSettings().attributeModifiers(SwordItem.createAttributeModifiers(ModToolMaterials.CELESTIUM, 3, -2.4F))));

    public static final Item CELESTIUM_SHOVEL = registerItem("celestium_shovel", new CelestiumShovelItem(toolSettings().attributeModifiers(MiningToolItem.createAttributeModifiers(ModToolMaterials.CELESTIUM, 1.5F, -3.0F))));

    public static final Item CELESTIUM_AXE = registerItem("celestium_axe", new AxeItem(ModToolMaterials.CELESTIUM, toolSettings().attributeModifiers(MiningToolItem.createAttributeModifiers(ModToolMaterials.CELESTIUM, 5.0F, -3.0F))));

    public static final Item CELESTIUM_HOE = registerItem("celestium_hoe", new CelestiumHoeItem(toolSettings().attributeModifiers(MiningToolItem.createAttributeModifiers(ModToolMaterials.CELESTIUM, -4.0F, 0.0F))));

    private static Item.Settings createCelestiumChestplateSettings(String itemName) {
        return armorSettings(ArmorItem.Type.CHESTPLATE);
    }

    private static Item.Settings createCelestiumElytraChestplateSettings() {
        return createCelestiumChestplateSettings("celestium_elytra_chestplate").maxDamage(1632);
    }

    private static Item.Settings armorSettings(ArmorItem.Type type) {
        return new Item.Settings().fireproof().maxDamage(ModArmorMaterials.durability(type));
    }

    private static Item.Settings toolSettings() {
        return new Item.Settings().fireproof().maxDamage(ModToolMaterials.CELESTIUM.getDurability());
    }
    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, Identifier.of(Celestium.MOD_ID, name), item);
    }

    public static void registerModItems() {
        Celestium.LOGGER.info("Registering items for " + Celestium.MOD_ID);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(fabricItemGroupEntries -> {
            fabricItemGroupEntries.add(CELESTIUM);
            fabricItemGroupEntries.add(WARDEN_HEART);
            fabricItemGroupEntries.add(CELESTIUM_UPGRADE_TEMPLATE);
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(fabricItemGroupEntries -> {
            fabricItemGroupEntries.add(CELESTIUM_HELMET);
            fabricItemGroupEntries.add(CELESTIUM_CHESTPLATE);
            fabricItemGroupEntries.add(CELESTIUM_ELYTRA_CHESTPLATE);
            fabricItemGroupEntries.add(CELESTIUM_LEGGINGS);
            fabricItemGroupEntries.add(CELESTIUM_BOOTS);
            fabricItemGroupEntries.add(CELESTIUM_SWORD);
            fabricItemGroupEntries.add(CELESTIUM_SPEAR);
            fabricItemGroupEntries.add(CELESTIUM_HORSE_ARMOR);
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(fabricItemGroupEntries -> {
            fabricItemGroupEntries.add(CELESTIUM_PICKAXE);
            fabricItemGroupEntries.add(CELESTIUM_AXE);
            fabricItemGroupEntries.add(CELESTIUM_SHOVEL);
            fabricItemGroupEntries.add(CELESTIUM_HOE);
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(fabricItemGroupEntries -> {
            fabricItemGroupEntries.add(SKULK_VENOM);
        });
    }
}
