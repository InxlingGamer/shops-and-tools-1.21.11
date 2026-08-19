package net.inklinggamer.shopsandtools.item;

import java.util.function.Consumer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.inklinggamer.shopsandtools.ShopsAndTools;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Unit;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.enchantment.Enchantable;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.Equippable;
import java.util.Optional;

public class ModItems {

    // ==========================================
    // BASIC ITEMS & MATERIALS
    // ==========================================
    public static final Item CELESTIUM = registerItem("celestium", new CelestiumItem(new Item.Properties().fireResistant().component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true).setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(ShopsAndTools.MOD_ID,"celestium")))));

    public static final Item SKULK_VENOM = registerItem("skulk_venom", new SkulkVenomItem(new Item.Properties().stacksTo(1).component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true).setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(ShopsAndTools.MOD_ID,"skulk_venom")))) {
        @Override
        public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay displayComponent, Consumer<Component> textConsumer, TooltipFlag type) {
            // 1. The Subtitle
            textConsumer.accept(Component.literal("A needy little symbiote from the Deep Dark.").withStyle(ChatFormatting.GRAY));
            textConsumer.accept(Component.empty());
            textConsumer.accept(Component.literal("Grants godly power, but bills you by the second.").withStyle(ChatFormatting.DARK_AQUA));
        }
    });

    public static final Item WARDEN_HEART = registerItem("warden_heart", new Item(new Item.Properties().component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true).setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(ShopsAndTools.MOD_ID,"warden_heart")))));

    public static final Item CELESTIUM_UPGRADE_TEMPLATE = registerItem("celestium_upgrade_template", new Item(new Item.Properties().fireResistant().rarity(Rarity.UNCOMMON).setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(ShopsAndTools.MOD_ID,"celestium_upgrade_template")))) {

        @Override
        public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay displayComponent, Consumer<Component> textConsumer, TooltipFlag type) {
            // 1. The Subtitle
            textConsumer.accept(Component.literal("Smithing Template").withStyle(ChatFormatting.GRAY));

            // 2. A blank line to separate the title from the stats
            textConsumer.accept(Component.empty());

            // 3. The "Applies to:" section
            textConsumer.accept(Component.literal("Applies to:").withStyle(ChatFormatting.GRAY));
            textConsumer.accept(Component.literal(" Netherite Equipment").withStyle(ChatFormatting.BLUE));

            // 4. The "Ingredients:" section
            textConsumer.accept(Component.literal("Ingredients:").withStyle(ChatFormatting.GRAY));
            textConsumer.accept(Component.literal(" Celestium").withStyle(ChatFormatting.LIGHT_PURPLE));
        }
    });

    // ==========================================
    // ARMOR (Using the new .armor() component)
    // ==========================================
    public static final Item CELESTIUM_HELMET = registerItem("celestium_helmet", new CelestiumHelmetItem(new Item.Properties().fireResistant().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(ShopsAndTools.MOD_ID,"celestium_helmet"))).humanoidArmor(ModArmorMaterials.CELESTIUM, ArmorType.HELMET).component(DataComponents.ENCHANTABLE, new Enchantable(30))));

    public static final Item CELESTIUM_CHESTPLATE = registerItem("celestium_chestplate", new CelestiumChestItem(createCelestiumChestplateSettings("celestium_chestplate")));

    public static final Item CELESTIUM_ELYTRA_CHESTPLATE = registerItem("celestium_elytra_chestplate", new CelestiumChestItem(createCelestiumElytraChestplateSettings()));

    public static final Item CELESTIUM_LEGGINGS = registerItem("celestium_leggings", new CelestiumLeggingsItem(new Item.Properties().fireResistant().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(ShopsAndTools.MOD_ID,"celestium_leggings"))).humanoidArmor(ModArmorMaterials.CELESTIUM, ArmorType.LEGGINGS)));

    public static final Item CELESTIUM_BOOTS = registerItem("celestium_boots", new CelestiumBootsItem(new Item.Properties().fireResistant().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(ShopsAndTools.MOD_ID,"celestium_boots"))).humanoidArmor(ModArmorMaterials.CELESTIUM, ArmorType.BOOTS)));

    public static final Item CELESTIUM_HORSE_ARMOR = registerItem("celestium_horse_armor", new CelestiumHorseArmorItem(new Item.Properties().fireResistant().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(ShopsAndTools.MOD_ID,"celestium_horse_armor"))).horseArmor(ModArmorMaterials.CELESTIUM)));


    // ==========================================
    // WEAPONS & TOOLS (Using the new modifiers)
    // ==========================================
    public static final Item CELESTIUM_SWORD = registerItem("celestium_sword", new Item(new Item.Properties().fireResistant().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(ShopsAndTools.MOD_ID,"celestium_sword"))).sword(ModToolMaterials.CELESTIUM, 3.0F, -1.9F)));

    public static final Item CELESTIUM_PICKAXE = registerItem("celestium_pickaxe", new CelestiumPickaxeItem(new Item.Properties().fireResistant().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(ShopsAndTools.MOD_ID,"celestium_pickaxe"))).pickaxe(ModToolMaterials.CELESTIUM, 1.0F, -2.8F)));

    public static final Item CELESTIUM_SPEAR = registerItem("celestium_spear", new Item(new Item.Properties().fireResistant().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(ShopsAndTools.MOD_ID,"celestium_spear"))).spear(ModToolMaterials.CELESTIUM, 1.05F, 1.075F, 0.5F, 3.0F, 7.5F, 6.5F, 5.1F, 10.0F, 4.6F)));

    public static final Item CELESTIUM_SHOVEL = registerItem("celestium_shovel", new CelestiumShovelItem(new Item.Properties().fireResistant().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(ShopsAndTools.MOD_ID,"celestium_shovel")))));

    public static final Item CELESTIUM_AXE = registerItem("celestium_axe", new AxeItem(ModToolMaterials.CELESTIUM, 5.0F, -3.0F, new Item.Properties().fireResistant().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(ShopsAndTools.MOD_ID,"celestium_axe")))));

    public static final Item CELESTIUM_HOE = registerItem("celestium_hoe", new CelestiumHoeItem(new Item.Properties().fireResistant().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(ShopsAndTools.MOD_ID,"celestium_hoe")))));

    private static Item.Properties createCelestiumChestplateSettings(String itemName) {
        return new Item.Properties()
                .fireResistant()
                .setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(ShopsAndTools.MOD_ID, itemName)))
                .humanoidArmor(ModArmorMaterials.CELESTIUM, ArmorType.CHESTPLATE)
                .component(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT.withHidden(DataComponents.ATTRIBUTE_MODIFIERS, false));
    }

    private static Item.Properties createCelestiumElytraChestplateSettings() {
        Item.Properties settings = createCelestiumChestplateSettings("celestium_elytra_chestplate")
                .durability(1632)
                .component(DataComponents.GLIDER, Unit.INSTANCE);

        Equippable chestplateEquippable = CELESTIUM_CHESTPLATE.components().get(DataComponents.EQUIPPABLE);
        settings.component(DataComponents.EQUIPPABLE, new Equippable(
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
        return Registry.register(BuiltInRegistries.ITEM, Identifier.fromNamespaceAndPath(ShopsAndTools.MOD_ID, name), item);
    }

    public static void registerModItems() {
        ShopsAndTools.LOGGER.info("Registering items for " + ShopsAndTools.MOD_ID);

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.INGREDIENTS).register(fabricItemGroupEntries -> {
            fabricItemGroupEntries.accept(CELESTIUM);
            fabricItemGroupEntries.accept(WARDEN_HEART);
            fabricItemGroupEntries.accept(CELESTIUM_UPGRADE_TEMPLATE);
        });

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.COMBAT).register(fabricItemGroupEntries -> {
            fabricItemGroupEntries.accept(CELESTIUM_HELMET);
            fabricItemGroupEntries.accept(CELESTIUM_CHESTPLATE);
            fabricItemGroupEntries.accept(CELESTIUM_ELYTRA_CHESTPLATE);
            fabricItemGroupEntries.accept(CELESTIUM_LEGGINGS);
            fabricItemGroupEntries.accept(CELESTIUM_BOOTS);
            fabricItemGroupEntries.accept(CELESTIUM_SWORD);
            fabricItemGroupEntries.accept(CELESTIUM_SPEAR);
            fabricItemGroupEntries.accept(CELESTIUM_HORSE_ARMOR);
        });

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(fabricItemGroupEntries -> {
            fabricItemGroupEntries.accept(CELESTIUM_PICKAXE);
            fabricItemGroupEntries.accept(CELESTIUM_AXE);
            fabricItemGroupEntries.accept(CELESTIUM_SHOVEL);
            fabricItemGroupEntries.accept(CELESTIUM_HOE);
        });

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(fabricItemGroupEntries -> {
            fabricItemGroupEntries.accept(SKULK_VENOM);
        });
    }
}
