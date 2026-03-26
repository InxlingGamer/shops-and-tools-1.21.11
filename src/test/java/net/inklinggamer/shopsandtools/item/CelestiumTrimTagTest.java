package net.inklinggamer.shopsandtools.item;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class CelestiumTrimTagTest {
    private static final Path TRIMMABLE_ARMOR_TAG_PATH = Path.of(
            "src", "main", "generated", "data", "minecraft", "tags", "item", "trimmable_armor.json"
    );

    private static final String[] CELESTIUM_ARMOR_IDS = {
            "shopsandtools:celestium_helmet",
            "shopsandtools:celestium_chestplate",
            "shopsandtools:celestium_elytra_chestplate",
            "shopsandtools:celestium_leggings",
            "shopsandtools:celestium_boots"
    };

    private CelestiumTrimTagTest() {
    }

    public static void main(String[] args) throws IOException {
        assertCelestiumArmorIsNotTrimmable();
    }

    private static void assertCelestiumArmorIsNotTrimmable() throws IOException {
        if (!Files.exists(TRIMMABLE_ARMOR_TAG_PATH)) {
            return;
        }

        String tagContents = Files.readString(TRIMMABLE_ARMOR_TAG_PATH);
        for (String celestiumArmorId : CELESTIUM_ARMOR_IDS) {
            assertFalse(
                    "Celestium armor should not appear in the generated trimmable armor tag: " + celestiumArmorId,
                    tagContents.contains(celestiumArmorId)
            );
        }
    }

    private static void assertFalse(String scenario, boolean condition) {
        if (condition) {
            throw new AssertionError(scenario);
        }
    }
}
