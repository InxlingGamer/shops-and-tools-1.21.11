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
        CompletableFuture<?> celestiumFuture = DataProvider.writeToPath(writer, createEquipmentModel(false), getEquipmentPath("celestium"));
        CompletableFuture<?> celestiumElytraFuture = DataProvider.writeToPath(writer, createEquipmentModel(true), getEquipmentPath("celestium_elytra"));
        return CompletableFuture.allOf(celestiumFuture, celestiumElytraFuture);
    }

    @Override
    public String getName() {
        return "Equipment Assets";
    }

    private JsonObject createEquipmentModel(boolean includeWings) {
        JsonObject root = new JsonObject();
        JsonObject layers = new JsonObject();

        JsonArray humanoid = new JsonArray();
        JsonObject humanoidTexture = new JsonObject();
        humanoidTexture.addProperty("texture", "shopsandtools:celestium");
        humanoid.add(humanoidTexture);

        JsonArray humanoidLeggings = new JsonArray();
        JsonObject leggingsTexture = new JsonObject();
        leggingsTexture.addProperty("texture", "shopsandtools:celestium");
        humanoidLeggings.add(leggingsTexture);

        layers.add("humanoid", humanoid);
        layers.add("humanoid_leggings", humanoidLeggings);

        if (includeWings) {
            JsonArray wings = new JsonArray();
            JsonObject wingsTexture = new JsonObject();
            wingsTexture.addProperty("texture", "minecraft:elytra");
            wingsTexture.addProperty("use_player_texture", true);
            wings.add(wingsTexture);
            layers.add("wings", wings);
        }

        root.add("layers", layers);
        return root;
    }

    private Path getEquipmentPath(String fileName) {
        return output.getPath()
                .resolve("assets")
                .resolve("shopsandtools")
                .resolve("equipment")
                .resolve(fileName + ".json");
    }
}
