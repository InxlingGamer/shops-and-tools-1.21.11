package net.inklinggamer.celestium.item;

import com.google.common.collect.Maps;
import net.inklinggamer.celestium.Celestium;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import java.util.Map;

public interface ModArmorMaterials {

    // In 1.21.5, we use EquipmentAssetKeys.ROOT_ID to define our custom asset key
    ResourceKey<EquipmentAsset> CELESTIUM_ASSET = ResourceKey.create(
            EquipmentAssets.ROOT_ID,
            Identifier.fromNamespaceAndPath(Celestium.MOD_ID, "celestium")
    );

    ResourceKey<EquipmentAsset> CELESTIUM_ELYTRA_ASSET = ResourceKey.create(
            EquipmentAssets.ROOT_ID,
            Identifier.fromNamespaceAndPath(Celestium.MOD_ID, "celestium_elytra")
    );

    ArmorMaterial CELESTIUM = new ArmorMaterial(
            75,
            createDefenseMap(7, 10, 13, 7, 30),
            30,
            SoundEvents.ARMOR_EQUIP_NETHERITE,
            6.0F,
            0.3F,
            ModToolMaterials.CELESTIUM_REPAIR,
            CELESTIUM_ASSET
    );

    static Map<ArmorType, Integer> createDefenseMap(int bootsDefense, int leggingsDefense, int chestplateDefense, int helmetDefense, int bodyDefense) {
        return Maps.newEnumMap(
                Map.of(
                        ArmorType.BOOTS, bootsDefense,
                        ArmorType.LEGGINGS, leggingsDefense,
                        ArmorType.CHESTPLATE, chestplateDefense,
                        ArmorType.HELMET, helmetDefense,
                        ArmorType.BODY, bodyDefense
                )
        );
    }
}
