package net.inklinggamer.shopsandtools.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.SimpleFabricLootTableProvider;
import net.inklinggamer.shopsandtools.item.ModItems;
import net.minecraft.item.Items;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class ModEntityLootProvider extends SimpleFabricLootTableProvider {

    public ModEntityLootProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(output, registryLookup, LootContextTypes.ENTITY);
    }

    @Override
    public void accept(BiConsumer<RegistryKey<LootTable>, LootTable.Builder> exporter) {
        // Target the exact vanilla Warden loot table ID so Datagen overwrites it
        RegistryKey<LootTable> wardenLootTable = RegistryKey.of(RegistryKeys.LOOT_TABLE, Identifier.of("minecraft", "entities/warden"));

        exporter.accept(wardenLootTable, LootTable.builder()
                // 1. We MUST recreate the vanilla Sculk Catalyst drop, otherwise it is lost forever!
                .pool(LootPool.builder()
                        .rolls(ConstantLootNumberProvider.create(1.0F))
                        .with(ItemEntry.builder(Items.SCULK_CATALYST))
                )
                // 2. Add your custom Warden Heart drop
                .pool(LootPool.builder()
                        .rolls(ConstantLootNumberProvider.create(1.0F))
                        .with(ItemEntry.builder(ModItems.WARDEN_HEART))
                )

                .pool(LootPool.builder()
                        .rolls(ConstantLootNumberProvider.create(1.0F))
                        .conditionally(RandomChanceLootCondition.builder(0.1F))
                        .with(ItemEntry.builder(ModItems.SKULK_VENOM))
                )
        );
    }
}