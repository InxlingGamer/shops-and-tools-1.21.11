package net.inklinggamer.shopsandtools.datagen;

import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.inklinggamer.shopsandtools.block.ModBlocks;
import net.inklinggamer.shopsandtools.item.ModItems;
import net.minecraft.client.data.BlockStateModelGenerator;
import net.minecraft.client.data.ItemModelGenerator;
import net.minecraft.client.data.Models;

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
        itemModelGenerator.register(ModItems.CELESTIUM_HELMET, Models.GENERATED);
        itemModelGenerator.register(ModItems.CELESTIUM_CHESTPLATE, Models.GENERATED);
        itemModelGenerator.register(ModItems.CELESTIUM_LEGGINGS, Models.GENERATED);
        itemModelGenerator.register(ModItems.CELESTIUM_BOOTS, Models.GENERATED);
        itemModelGenerator.register(ModItems.CELESTIUM_SWORD, Models.GENERATED);
        itemModelGenerator.register(ModItems.CELESTIUM_PICKAXE, Models.GENERATED);
        itemModelGenerator.register(ModItems.CELESTIUM_AXE, Models.GENERATED);
        itemModelGenerator.register(ModItems.CELESTIUM_SHOVEL, Models.GENERATED);
        itemModelGenerator.register(ModItems.CELESTIUM_HOE, Models.GENERATED);
        itemModelGenerator.register(ModItems.CELESTIUM_SPEAR, Models.GENERATED);
        itemModelGenerator.register(ModItems.CELESTIUM_HORSE_ARMOR, Models.GENERATED);
        itemModelGenerator.register(ModItems.CELESTIUM_UPGRADE_TEMPLATE, Models.GENERATED);
        itemModelGenerator.register(ModItems.WARDEN_HEART, Models.GENERATED);

    }
}
