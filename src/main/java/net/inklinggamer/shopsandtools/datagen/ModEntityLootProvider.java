package net.inklinggamer.shopsandtools.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.SimpleFabricLootTableProvider;
import net.inklinggamer.shopsandtools.item.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceWithEnchantedBonusCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class ModEntityLootProvider extends SimpleFabricLootTableProvider {
    private static final float SKULK_VENOM_UNENCHANTED_CHANCE = 0.025F;
    private static final float SKULK_VENOM_LOOTING_ONE_CHANCE = 0.05F;
    private static final float SKULK_VENOM_PER_LEVEL_ABOVE_FIRST = 0.025F;

    private final CompletableFuture<HolderLookup.Provider> registryLookupFuture;

    public ModEntityLootProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(output, registryLookup, LootContextParamSets.ENTITY);
        this.registryLookupFuture = registryLookup;
    }

    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> exporter) {
        HolderLookup.Provider wrapperLookup = this.registryLookupFuture.join();
        HolderLookup.RegistryLookup<Enchantment> enchantmentRegistry = wrapperLookup.lookupOrThrow(Registries.ENCHANTMENT);
        var looting = enchantmentRegistry.getOrThrow(Enchantments.LOOTING);

        // Target the exact vanilla Warden loot table ID so Datagen overwrites it
        ResourceKey<LootTable> wardenLootTable = ResourceKey.create(Registries.LOOT_TABLE, Identifier.fromNamespaceAndPath("minecraft", "entities/warden"));

        exporter.accept(wardenLootTable, LootTable.lootTable()
                // 1. We MUST recreate the vanilla Sculk Catalyst drop, otherwise it is lost forever!
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(Items.SCULK_CATALYST))
                )
                // 2. Add your custom Warden Heart drop
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(ModItems.WARDEN_HEART))
                )

                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .when(() -> new LootItemRandomChanceWithEnchantedBonusCondition(
                                SKULK_VENOM_UNENCHANTED_CHANCE,
                                LevelBasedValue.perLevel(SKULK_VENOM_LOOTING_ONE_CHANCE, SKULK_VENOM_PER_LEVEL_ABOVE_FIRST),
                                looting
                        ))
                        .add(LootItem.lootTableItem(ModItems.SKULK_VENOM))
                )
        );
    }
}
