package net.inklinggamer.shopsandtools.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.inklinggamer.shopsandtools.ShopsAndTools;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ModHeldItemAssetProvider implements DataProvider {
    private static final List<String> INVENTORY_DISPLAY_CONTEXTS = List.of("gui", "ground", "fixed", "on_shelf");
    private static final List<InHandItemDefinition> IN_HAND_ITEMS = List.of(
            new InHandItemDefinition(
                    "celestium_spear",
                    "minecraft:item/generated",
                    "shopsandtools:item/celestium_spear",
                    "minecraft:item/spear_in_hand",
                    "shopsandtools:item/celestium_spear_in_hand",
                    1.95D
            )
    );

    private final FabricDataOutput output;

    public ModHeldItemAssetProvider(FabricDataOutput output) {
        this.output = output;
    }

    @Override
    public CompletableFuture<?> run(DataWriter writer) {
        CompletableFuture<?>[] futures = IN_HAND_ITEMS.stream()
                .flatMap(definition -> List.of(
                        DataProvider.writeToPath(writer, createSimpleModel(definition.baseModelParent(), definition.baseTexture()), getModelPath(definition.itemName())),
                        DataProvider.writeToPath(writer, createSimpleModel(definition.inHandModelParent(), definition.inHandTexture()), getModelPath(definition.inHandModelName())),
                        DataProvider.writeToPath(writer, createDisplayContextItemAsset(definition), getItemPath(definition.itemName()))
                ).stream())
                .toArray(CompletableFuture[]::new);
        return CompletableFuture.allOf(futures);
    }

    @Override
    public String getName() {
        return "Held Item Assets";
    }

    private JsonObject createSimpleModel(String parent, String texture) {
        JsonObject root = new JsonObject();
        JsonObject textures = new JsonObject();
        root.addProperty("parent", parent);
        textures.addProperty("layer0", texture);
        root.add("textures", textures);
        return root;
    }

    private JsonObject createDisplayContextItemAsset(InHandItemDefinition definition) {
        JsonObject root = new JsonObject();
        JsonObject model = new JsonObject();
        JsonArray cases = new JsonArray();
        JsonObject inventoryCase = new JsonObject();
        JsonObject inventoryModel = new JsonObject();
        JsonArray when = new JsonArray();
        JsonObject fallback = new JsonObject();

        inventoryModel.addProperty("type", "minecraft:model");
        inventoryModel.addProperty("model", getItemModelId(definition.itemName()));
        inventoryCase.add("model", inventoryModel);

        for (String context : INVENTORY_DISPLAY_CONTEXTS) {
            when.add(context);
        }
        inventoryCase.add("when", when);
        cases.add(inventoryCase);

        fallback.addProperty("type", "minecraft:model");
        fallback.addProperty("model", getItemModelId(definition.inHandModelName()));

        model.addProperty("type", "minecraft:select");
        model.add("cases", cases);
        model.add("fallback", fallback);
        model.addProperty("property", "minecraft:display_context");

        root.add("model", model);
        root.addProperty("swap_animation_scale", definition.swapAnimationScale());
        return root;
    }

    private String getItemModelId(String modelName) {
        return ShopsAndTools.MOD_ID + ":item/" + modelName;
    }

    private Path getModelPath(String fileName) {
        return output.getPath()
                .resolve("assets")
                .resolve(ShopsAndTools.MOD_ID)
                .resolve("models")
                .resolve("item")
                .resolve(fileName + ".json");
    }

    private Path getItemPath(String fileName) {
        return output.getPath()
                .resolve("assets")
                .resolve(ShopsAndTools.MOD_ID)
                .resolve("items")
                .resolve(fileName + ".json");
    }

    private record InHandItemDefinition(
            String itemName,
            String baseModelParent,
            String baseTexture,
            String inHandModelParent,
            String inHandTexture,
            double swapAnimationScale
    ) {
        private String inHandModelName() {
            return itemName + "_in_hand";
        }
    }
}
