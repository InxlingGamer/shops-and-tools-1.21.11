package net.inklinggamer.celestium.item;

import net.inklinggamer.celestium.Celestium;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Map;

public final class ModArmorMaterials {
    private static final int BASE_DURABILITY = 75;

    public static final RegistryEntry<ArmorMaterial> CELESTIUM = Registry.registerReference(
            Registries.ARMOR_MATERIAL,
            Identifier.of(Celestium.MOD_ID, "celestium"),
            new ArmorMaterial(
            Map.of(
                    ArmorItem.Type.BOOTS, 7,
                    ArmorItem.Type.LEGGINGS, 10,
                    ArmorItem.Type.CHESTPLATE, 13,
                    ArmorItem.Type.HELMET, 7,
                    ArmorItem.Type.BODY, 30
            ),
            30,
            SoundEvents.ITEM_ARMOR_EQUIP_NETHERITE,
            () -> Ingredient.fromTag(ModToolMaterials.CELESTIUM_REPAIR),
            List.of(new ArmorMaterial.Layer(Identifier.of(Celestium.MOD_ID, "celestium"))),
            6.0F,
            0.3F
    ));

    private ModArmorMaterials() {
    }

    public static int durability(ArmorItem.Type type) {
        return type.getMaxDamage(BASE_DURABILITY);
    }
}
