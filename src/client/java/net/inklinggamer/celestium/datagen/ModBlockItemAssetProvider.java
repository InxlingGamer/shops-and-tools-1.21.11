package net.inklinggamer.celestium.datagen;

import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.inklinggamer.celestium.Celestium;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ModBlockItemAssetProvider implements DataProvider {
    private static final List<BlockItemDefinition> BLOCK_ITEMS = List.of(
            new BlockItemDefinition("celestium_block", "block/celestium_block")
    );

    private final FabricPackOutput output;

    public ModBlockItemAssetProvider(FabricPackOutput output) {
        this.output = output;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput writer) {
        CompletableFuture<?>[] futures = BLOCK_ITEMS.stream()
                .map(definition -> DataProvider.saveStable(writer, createItemAsset(definition.modelPath()), getItemPath(definition.itemName())))
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
        model.addProperty("model", Celestium.MOD_ID + ":" + modelPath);
        root.add("model", model);

        return root;
    }

    private Path getItemPath(String fileName) {
        return output.getOutputFolder()
                .resolve("assets")
                .resolve(Celestium.MOD_ID)
                .resolve("items")
                .resolve(fileName + ".json");
    }

    private record BlockItemDefinition(String itemName, String modelPath) {
    }
}
