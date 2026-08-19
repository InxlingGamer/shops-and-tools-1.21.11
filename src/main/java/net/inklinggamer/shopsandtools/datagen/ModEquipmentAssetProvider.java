package net.inklinggamer.shopsandtools.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.inklinggamer.shopsandtools.ShopsAndTools;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class ModEquipmentAssetProvider implements DataProvider {
    private final FabricDataOutput output;

    public ModEquipmentAssetProvider(FabricDataOutput output) {
        this.output = output;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput writer) {
        CompletableFuture<?> celestiumFuture = DataProvider.saveStable(writer, createEquipmentModel(true, false), getEquipmentPath("celestium"));
        CompletableFuture<?> celestiumElytraFuture = DataProvider.saveStable(writer, createEquipmentModel(false, true), getEquipmentPath("celestium_elytra"));
        return CompletableFuture.allOf(celestiumFuture, celestiumElytraFuture);
    }

    @Override
    public String getName() {
        return "Celestium Equipment Assets";
    }

    private JsonObject createEquipmentModel(boolean includeHorseBody, boolean includeWings) {
        JsonObject root = new JsonObject();
        JsonObject layers = new JsonObject();
        JsonArray humanoid = createTextureLayer(getTextureId("celestium"));
        JsonArray humanoidLeggings = createTextureLayer(getTextureId("celestium"));

        if (includeHorseBody) {
            layers.add("horse_body", createTextureLayer(getTextureId("celestium")));
        }
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

    private JsonArray createTextureLayer(String textureId) {
        JsonArray textureLayer = new JsonArray();
        JsonObject texture = new JsonObject();
        texture.addProperty("texture", textureId);
        textureLayer.add(texture);
        return textureLayer;
    }

    private String getTextureId(String textureName) {
        return ShopsAndTools.MOD_ID + ":" + textureName;
    }

    private Path getEquipmentPath(String fileName) {
        return output.getOutputFolder()
                .resolve("assets")
                .resolve(ShopsAndTools.MOD_ID)
                .resolve("equipment")
                .resolve(fileName + ".json");
    }
}
