package net.inklinggamer.shopsandtools.datagen;

import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.inklinggamer.shopsandtools.ShopsAndTools;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ModBlockItemAssetProvider implements DataProvider {
    private static final List<BlockItemDefinition> BLOCK_ITEMS = List.of(
            new BlockItemDefinition("celestium_block", "block/celestium_block")
    );

    private final FabricDataOutput output;

    public ModBlockItemAssetProvider(FabricDataOutput output) {
        this.output = output;
    }

    @Override
    public CompletableFuture<?> run(DataWriter writer) {
        CompletableFuture<?>[] futures = BLOCK_ITEMS.stream()
                .map(definition -> DataProvider.writeToPath(writer, createItemAsset(definition.modelPath()), getItemPath(definition.itemName())))
                .toArray(CompletableFuture[]::new);
        return CompletableFuture.allOf(futures);
    }

    @Override
    public String getName() {
        return "Celestium Block Item Assets";
    }

    private JsonObject createItemAsset(String modelPath) {
        JsonObject root = new JsonObject();
        JsonObject model = new JsonObject();

        model.addProperty("type", "minecraft:model");
        model.addProperty("model", ShopsAndTools.MOD_ID + ":" + modelPath);
        root.add("model", model);

        return root;
    }

    private Path getItemPath(String fileName) {
        return output.getPath()
                .resolve("assets")
                .resolve(ShopsAndTools.MOD_ID)
                .resolve("items")
                .resolve(fileName + ".json");
    }

    private record BlockItemDefinition(String itemName, String modelPath) {
    }
}
