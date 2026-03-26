package net.inklinggamer.shopsandtools.datagen;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class CelestiumBlockItemAssetTest {
    private static final Path BLOCK_ITEM_ASSET_PATH = Path.of(
            "src", "main", "generated", "assets", "shopsandtools", "items", "celestium_block.json"
    );

    private CelestiumBlockItemAssetTest() {
    }

    public static void main(String[] args) throws IOException {
        assertBlockItemAssetExists();
        assertBlockItemAssetUsesBlockModel();
    }

    private static void assertBlockItemAssetExists() {
        assertTrue(
                "Celestium block items should generate an explicit 1.21 item asset",
                Files.exists(BLOCK_ITEM_ASSET_PATH)
        );
    }

    private static void assertBlockItemAssetUsesBlockModel() throws IOException {
        String blockItemAsset = Files.readString(BLOCK_ITEM_ASSET_PATH);
        assertTrue(
                "Celestium block items should point at the generated Celestium block model",
                blockItemAsset.contains("\"model\": \"shopsandtools:block/celestium_block\"")
        );
        assertTrue(
                "Celestium block items should use the standard minecraft:model item definition",
                blockItemAsset.contains("\"type\": \"minecraft:model\"")
        );
    }

    private static void assertTrue(String scenario, boolean condition) {
        if (!condition) {
            throw new AssertionError(scenario);
        }
    }
}
