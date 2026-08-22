package net.inklinggamer.celestium.datagen;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class CelestiumBlockItemAssetTest {
    private static final Path BLOCK_ITEM_MODEL_PATH = Path.of(
            "src", "main", "generated", "assets", "celestium", "models", "item", "celestium_block.json"
    );

    private CelestiumBlockItemAssetTest() {
    }

    public static void main(String[] args) throws IOException {
        assertBlockItemAssetExists();
        assertBlockItemAssetUsesBlockModel();
    }

    private static void assertBlockItemAssetExists() {
        assertTrue(
                "Celestium block items should generate a Minecraft 1.21.1 item model",
                Files.exists(BLOCK_ITEM_MODEL_PATH)
        );
    }

    private static void assertBlockItemAssetUsesBlockModel() throws IOException {
        String blockItemAsset = Files.readString(BLOCK_ITEM_MODEL_PATH);
        assertTrue(
                "Celestium block items should point at the generated Celestium block model",
                blockItemAsset.contains("\"parent\": \"celestium:block/celestium_block\"")
        );
    }

    private static void assertTrue(String scenario, boolean condition) {
        if (!condition) {
            throw new AssertionError(scenario);
        }
    }
}
