package net.inklinggamer.shopsandtools.item;

import com.google.common.collect.Maps;
import net.inklinggamer.shopsandtools.ShopsAndTools;
import net.minecraft.item.equipment.ArmorMaterial;
import net.minecraft.item.equipment.EquipmentAsset;
import net.minecraft.item.equipment.EquipmentAssetKeys; // <--- The new home for EQUIPMENT_ASSET
import net.minecraft.item.equipment.EquipmentType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

import java.util.Map;

public interface ModArmorMaterials {

    // In 1.21.5, we use EquipmentAssetKeys.ROOT_ID to define our custom asset key
    RegistryKey<EquipmentAsset> CELESTIUM_ASSET = RegistryKey.of(
            EquipmentAssetKeys.REGISTRY_KEY,
            Identifier.of(ShopsAndTools.MOD_ID, "celestium")
    );

    ArmorMaterial CELESTIUM = new ArmorMaterial(
            75,
            createDefenseMap(7, 10, 13, 7, 30),
            30,
            SoundEvents.ITEM_ARMOR_EQUIP_NETHERITE,
            6.0F,
            0.3F,
            ModToolMaterials.CELESTIUM_REPAIR,
            CELESTIUM_ASSET
    );

    static Map<EquipmentType, Integer> createDefenseMap(int bootsDefense, int leggingsDefense, int chestplateDefense, int helmetDefense, int bodyDefense) {
        return Maps.newEnumMap(
                Map.of(
                        EquipmentType.BOOTS, bootsDefense,
                        EquipmentType.LEGGINGS, leggingsDefense,
                        EquipmentType.CHESTPLATE, chestplateDefense,
                        EquipmentType.HELMET, helmetDefense,
                        EquipmentType.BODY, bodyDefense
                )
        );
    }
}