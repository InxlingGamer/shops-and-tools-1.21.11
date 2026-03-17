package net.inklinggamer.shopsandtools.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class ModEquipmentAssetProvider implements DataProvider {
    private final FabricDataOutput output;

    public ModEquipmentAssetProvider(FabricDataOutput output) {
        this.output = output;
    }

    @Override
    public CompletableFuture<?> run(DataWriter writer) {
        // 1. Create the base JSON object
        JsonObject root = new JsonObject();
        JsonObject layers = new JsonObject();

        // 2. Set up the "humanoid" layer (Helmet, Chestplate, Boots)
        JsonArray humanoid = new JsonArray();
        JsonObject humanoidTexture = new JsonObject();
        humanoidTexture.addProperty("texture", "shopsandtools:celestium");
        humanoid.add(humanoidTexture);

        // 3. Set up the "humanoid_leggings" layer (Leggings)
        JsonArray humanoidLeggings = new JsonArray();
        JsonObject leggingsTexture = new JsonObject();
        leggingsTexture.addProperty("texture", "shopsandtools:celestium");
        humanoidLeggings.add(leggingsTexture);

        // 4. Combine them into the layers object matching the docs
        layers.add("humanoid", humanoid);
        layers.add("humanoid_leggings", humanoidLeggings);
        root.add("layers", layers);

        // 5. Define the exact path: assets/shopsandtools/equipment/celestium.json
        Path path = output.getPath()
                .resolve("assets")
                .resolve("shopsandtools")
                .resolve("equipment")
                .resolve("celestium.json");

        // Write the JSON to the generated folder
        return DataProvider.writeToPath(writer, root, path);
    }

    @Override
    public String getName() {
        return "Equipment Assets";
    }
}