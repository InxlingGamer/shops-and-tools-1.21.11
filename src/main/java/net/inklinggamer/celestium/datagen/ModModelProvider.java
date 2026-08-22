package net.inklinggamer.celestium.datagen;

import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.inklinggamer.celestium.block.ModBlocks;
import net.inklinggamer.celestium.item.ModItems;
import net.minecraft.data.client.BlockStateModelGenerator;
import net.minecraft.data.client.ItemModelGenerator;
import net.minecraft.data.client.Models;

public class ModModelProvider extends FabricModelProvider {
    public ModModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator blockStateModelGenerator) {
        blockStateModelGenerator.registerSimpleCubeAll(ModBlocks.CELESTIUM_BLOCK);

    }

    @Override
    public void generateItemModels(ItemModelGenerator itemModelGenerator) {
        itemModelGenerator.register(ModItems.CELESTIUM, Models.GENERATED);
        itemModelGenerator.register(ModItems.SKULK_VENOM, Models.GENERATED);
        itemModelGenerator.register(ModItems.CELESTIUM_HELMET, Models.GENERATED);
        itemModelGenerator.register(ModItems.CELESTIUM_CHESTPLATE, Models.GENERATED);
        itemModelGenerator.register(ModItems.CELESTIUM_ELYTRA_CHESTPLATE, Models.GENERATED);
        itemModelGenerator.register(ModItems.CELESTIUM_LEGGINGS, Models.GENERATED);
        itemModelGenerator.register(ModItems.CELESTIUM_BOOTS, Models.GENERATED);
        itemModelGenerator.register(ModItems.CELESTIUM_SWORD, Models.HANDHELD);
        itemModelGenerator.register(ModItems.CELESTIUM_PICKAXE, Models.HANDHELD);
        itemModelGenerator.register(ModItems.CELESTIUM_AXE, Models.HANDHELD);
        itemModelGenerator.register(ModItems.CELESTIUM_SHOVEL, Models.HANDHELD);
        itemModelGenerator.register(ModItems.CELESTIUM_HOE, Models.HANDHELD);
        itemModelGenerator.register(ModItems.CELESTIUM_SPEAR, Models.HANDHELD);
        itemModelGenerator.register(ModItems.CELESTIUM_HORSE_ARMOR, Models.GENERATED);
        itemModelGenerator.register(ModItems.CELESTIUM_UPGRADE_TEMPLATE, Models.GENERATED);
        itemModelGenerator.register(ModItems.WARDEN_HEART, Models.GENERATED);

    }
}
