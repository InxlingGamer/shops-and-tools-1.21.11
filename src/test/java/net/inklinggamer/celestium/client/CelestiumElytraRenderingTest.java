package net.inklinggamer.celestium.client;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class CelestiumElytraRenderingTest {
    private static final Path MIXIN_CONFIG = Path.of("src", "main", "resources", "celestium.mixins.json");
    private static final Path ARMOR_SUPPRESSION_MIXIN = Path.of(
            "src", "client", "java", "net", "inklinggamer", "celestium", "mixin", "client",
            "ArmorFeatureRendererMixin.java"
    );
    private static final Path ELYTRA_MIXIN = Path.of(
            "src", "client", "java", "net", "inklinggamer", "celestium", "mixin", "client",
            "ElytraFeatureRendererMixin.java"
    );
    private static final Path CHESTPLATE_ITEM = Path.of(
            "src", "main", "java", "net", "inklinggamer", "celestium", "item",
            "CelestiumChestItem.java"
    );
    private static final Path ARMOR_LAYER_ONE = Path.of(
            "src", "main", "resources", "assets", "celestium", "textures", "models", "armor",
            "celestium_layer_1.png"
    );
    private static final Path ARMOR_LAYER_TWO = Path.of(
            "src", "main", "resources", "assets", "celestium", "textures", "models", "armor",
            "celestium_layer_2.png"
    );

    private CelestiumElytraRenderingTest() {
    }

    public static void main(String[] args) throws IOException {
        String mixinConfig = Files.readString(MIXIN_CONFIG);
        String elytraMixin = Files.readString(ELYTRA_MIXIN);
        String chestplateItem = Files.readString(CHESTPLATE_ITEM);

        assertTrue(
                "The fused chestplate must not suppress Minecraft's armor renderer",
                !Files.exists(ARMOR_SUPPRESSION_MIXIN)
                        && !mixinConfig.contains("ArmorFeatureRendererMixin")
        );
        assertTrue(
                "The fused chestplate must remain registered with the vanilla elytra feature renderer",
                mixinConfig.contains("client.ElytraFeatureRendererMixin")
                        && elytraMixin.contains("ModItems.CELESTIUM_ELYTRA_CHESTPLATE")
                        && elytraMixin.contains("expectedItem == Items.ELYTRA")
        );
        assertTrue(
                "The fused item must remain an ArmorItem so Minecraft renders its torso and arm layer",
                chestplateItem.contains("extends ArmorItem")
                        && chestplateItem.contains("Type.CHESTPLATE")
        );
        assertTrue(
                "Minecraft 1.21.1 armor textures for the Celestium torso and leggings must exist",
                Files.isRegularFile(ARMOR_LAYER_ONE)
                        && Files.size(ARMOR_LAYER_ONE) > 0L
                        && Files.isRegularFile(ARMOR_LAYER_TWO)
                        && Files.size(ARMOR_LAYER_TWO) > 0L
        );
    }

    private static void assertTrue(String scenario, boolean condition) {
        if (!condition) {
            throw new AssertionError(scenario);
        }
    }
}
