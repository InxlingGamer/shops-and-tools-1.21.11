package net.inklinggamer.shopsandtools.item;

import java.util.function.Consumer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.inklinggamer.shopsandtools.ShopsAndTools;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.EnchantableComponent;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.item.*;
import net.minecraft.item.equipment.EquipmentType;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.Unit;

import java.util.Optional;

public class ModItems {

    // ==========================================
    // BASIC ITEMS & MATERIALS
    // ==========================================
    public static final Item CELESTIUM = registerItem("celestium", new Item(new Item.Settings().fireproof().component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true).registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(ShopsAndTools.MOD_ID,"celestium")))));

    public static final Item SKULK_VENOM = registerItem("skulk_venom", new SkulkVenomItem(new Item.Settings().maxCount(1).component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true).registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(ShopsAndTools.MOD_ID,"skulk_venom")))) {
        @Override
        public void appendTooltip(ItemStack stack, Item.TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
            // 1. The Subtitle
            textConsumer.accept(Text.literal("A needy little symbiote from the Deep Dark.").formatted(Formatting.GRAY));
            textConsumer.accept(Text.empty());
            textConsumer.accept(Text.literal("Grants godly power, but bills you by the second.").formatted(Formatting.DARK_AQUA));
        }
    });

    public static final Item WARDEN_HEART = registerItem("warden_heart", new Item(new Item.Settings().component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true).registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(ShopsAndTools.MOD_ID,"warden_heart")))));

    public static final Item CELESTIUM_UPGRADE_TEMPLATE = registerItem("celestium_upgrade_template", new Item(new Item.Settings().fireproof().rarity(Rarity.UNCOMMON).registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(ShopsAndTools.MOD_ID,"celestium_upgrade_template")))) {

        @Override
        public void appendTooltip(ItemStack stack, Item.TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
            // 1. The Subtitle
            textConsumer.accept(Text.literal("Smithing Template").formatted(Formatting.GRAY));

            // 2. A blank line to separate the title from the stats
            textConsumer.accept(Text.empty());

            // 3. The "Applies to:" section
            textConsumer.accept(Text.literal("Applies to:").formatted(Formatting.GRAY));
            textConsumer.accept(Text.literal(" Netherite Equipment").formatted(Formatting.BLUE));

            // 4. The "Ingredients:" section
            textConsumer.accept(Text.literal("Ingredients:").formatted(Formatting.GRAY));
            textConsumer.accept(Text.literal(" Celestium").formatted(Formatting.LIGHT_PURPLE));
        }
    });

    // ==========================================
    // ARMOR (Using the new .armor() component)
    // ==========================================
    public static final Item CELESTIUM_HELMET = registerItem("celestium_helmet", new CelestiumHelmetItem(new Item.Settings().fireproof().registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(ShopsAndTools.MOD_ID,"celestium_helmet"))).armor(ModArmorMaterials.CELESTIUM, EquipmentType.HELMET).component(DataComponentTypes.ENCHANTABLE, new EnchantableComponent(30))));

    public static final Item CELESTIUM_CHESTPLATE = registerItem("celestium_chestplate", new CelestiumChestItem(createCelestiumChestplateSettings("celestium_chestplate")));

    public static final Item CELESTIUM_ELYTRA_CHESTPLATE = registerItem("celestium_elytra_chestplate", new CelestiumChestItem(createCelestiumElytraChestplateSettings()));

    public static final Item CELESTIUM_LEGGINGS = registerItem("celestium_leggings", new CelestiumLeggingsItem(new Item.Settings().fireproof().registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(ShopsAndTools.MOD_ID,"celestium_leggings"))).armor(ModArmorMaterials.CELESTIUM, EquipmentType.LEGGINGS)));

    public static final Item CELESTIUM_BOOTS = registerItem("celestium_boots", new CelestiumBootsItem(new Item.Settings().fireproof().registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(ShopsAndTools.MOD_ID,"celestium_boots"))).armor(ModArmorMaterials.CELESTIUM, EquipmentType.BOOTS)));

    public static final Item CELESTIUM_HORSE_ARMOR = registerItem("celestium_horse_armor", new Item(new Item.Settings().fireproof().registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(ShopsAndTools.MOD_ID,"celestium_horse_armor"))).horseArmor(ModArmorMaterials.CELESTIUM)));


    // ==========================================
    // WEAPONS & TOOLS (Using the new modifiers)
    // ==========================================
    public static final Item CELESTIUM_SWORD = registerItem("celestium_sword", new Item(new Item.Settings().fireproof().registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(ShopsAndTools.MOD_ID,"celestium_sword"))).sword(ModToolMaterials.CELESTIUM, 3.0F, -1.9F)));

    public static final Item CELESTIUM_PICKAXE = registerItem("celestium_pickaxe", new CelestiumPickaxeItem(new Item.Settings().fireproof().registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(ShopsAndTools.MOD_ID,"celestium_pickaxe"))).pickaxe(ModToolMaterials.CELESTIUM, 1.0F, -2.8F)));

    public static final Item CELESTIUM_SPEAR = registerItem("celestium_spear", new Item(new Item.Settings().fireproof().registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(ShopsAndTools.MOD_ID,"celestium_spear"))).spear(ModToolMaterials.CELESTIUM, 1.05F, 1.075F, 0.5F, 3.0F, 7.5F, 6.5F, 5.1F, 10.0F, 4.6F)));

    public static final Item CELESTIUM_SHOVEL = registerItem("celestium_shovel", new ShovelItem(ModToolMaterials.CELESTIUM, 1.5F, -3.0F, new Item.Settings().fireproof().registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(ShopsAndTools.MOD_ID,"celestium_shovel")))));

    public static final Item CELESTIUM_AXE = registerItem("celestium_axe", new AxeItem(ModToolMaterials.CELESTIUM, 5.0F, -3.0F, new Item.Settings().fireproof().registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(ShopsAndTools.MOD_ID,"celestium_axe")))));

    public static final Item CELESTIUM_HOE = registerItem("celestium_hoe", new HoeItem(ModToolMaterials.CELESTIUM, -4.0F, 0.0F, new Item.Settings().fireproof().registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(ShopsAndTools.MOD_ID,"celestium_hoe")))));


    private static Item.Settings createCelestiumChestplateSettings(String itemName) {
        return new Item.Settings()
                .fireproof()
                .registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(ShopsAndTools.MOD_ID, itemName)))
                .armor(ModArmorMaterials.CELESTIUM, EquipmentType.CHESTPLATE)
                .component(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplayComponent.DEFAULT.with(DataComponentTypes.ATTRIBUTE_MODIFIERS, false));
    }

    private static Item.Settings createCelestiumElytraChestplateSettings() {
        Item.Settings settings = createCelestiumChestplateSettings("celestium_elytra_chestplate")
                .maxDamage(1632)
                .component(DataComponentTypes.GLIDER, Unit.INSTANCE);

        EquippableComponent chestplateEquippable = CELESTIUM_CHESTPLATE.getComponents().get(DataComponentTypes.EQUIPPABLE);
        settings.component(DataComponentTypes.EQUIPPABLE, new EquippableComponent(
                chestplateEquippable.slot(),
                chestplateEquippable.equipSound(),
                Optional.of(ModArmorMaterials.CELESTIUM_ELYTRA_ASSET),
                chestplateEquippable.cameraOverlay(),
                chestplateEquippable.allowedEntities(),
                chestplateEquippable.dispensable(),
                chestplateEquippable.swappable(),
                chestplateEquippable.damageOnHurt(),
                chestplateEquippable.equipOnInteract(),
                chestplateEquippable.canBeSheared(),
                chestplateEquippable.shearingSound()
        ));

        return settings;
    }
    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, Identifier.of(ShopsAndTools.MOD_ID, name), item);
    }

    public static void registerModItems() {
        ShopsAndTools.LOGGER.info("Registering items for " + ShopsAndTools.MOD_ID);

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
