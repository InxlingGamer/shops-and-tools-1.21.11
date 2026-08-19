package net.inklinggamer.celestium;

import net.inklinggamer.celestium.network.ArmCelestiumShovelSlamPayload;
import net.inklinggamer.celestium.network.OpenCelestiumCraftingPayload;
import net.inklinggamer.celestium.network.ReturnToInventoryPayload;
import net.inklinggamer.celestium.network.SyncCelestiumRagePayload;
import net.inklinggamer.celestium.network.SyncCelestiumSpearStunCooldownPayload;
import net.inklinggamer.celestium.network.SyncCelestiumThrustCooldownPayload;
import net.inklinggamer.celestium.network.SyncCelestiumTrialChamberMarkerPayload;
import net.inklinggamer.celestium.network.SyncCelestiumWallClimbInputPayload;
import net.inklinggamer.celestium.network.SyncCelestiumWallClimbStatePayload;
import net.inklinggamer.celestium.network.ToggleCelestiumPickaxeAreaModePayload;
import net.inklinggamer.celestium.network.ToggleCelestiumPickaxeEnchantModePayload;
import net.inklinggamer.celestium.network.ToggleCelestiumShovelAreaModePayload;
import net.inklinggamer.celestium.registry.BlockIds;
import net.inklinggamer.celestium.registry.BlockItemIds;
import net.inklinggamer.celestium.registry.ItemIds;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class CelestiumMetadataTest {
    private static final Path FABRIC_MOD_JSON_PATH = Path.of("src", "main", "resources", "fabric.mod.json");
    private static final Path GRADLE_PROPERTIES_PATH = Path.of("gradle.properties");

    private CelestiumMetadataTest() {
    }

    public static void main(String[] args) throws Exception {
        assertFabricMetadataUsesCanonicalIdAndCelestiumName();
        assertGradleBuildUsesCelestiumArchiveNameAndHotfixVersion();
        assertCanonicalRegistryIds();
        assertCanonicalPacketIds();
        assertGeneratedNamespaceIntegrity();
    }

    private static void assertCanonicalRegistryIds() throws IllegalAccessException {
        int itemIds = assertResourceKeysUseCanonicalNamespace(ItemIds.class);
        int blockIds = assertResourceKeysUseCanonicalNamespace(BlockIds.class);
        int blockItemIds = assertResourceKeysUseCanonicalNamespace(BlockItemIds.class);
        assertTrue("Every canonical item ID should be declared", itemIds == 16);
        assertTrue("The canonical block ID should be declared", blockIds == 1);
        assertTrue("The separate canonical block-item ID should be declared", blockItemIds == 1);
    }

    private static int assertResourceKeysUseCanonicalNamespace(Class<?> idsClass) throws IllegalAccessException {
        int count = 0;
        for (Field field : idsClass.getFields()) {
            if (!Modifier.isStatic(field.getModifiers()) || !ResourceKey.class.isAssignableFrom(field.getType())) {
                continue;
            }
            ResourceKey<?> key = (ResourceKey<?>) field.get(null);
            assertTrue("Registry IDs must use the celestium namespace", "celestium".equals(key.identifier().getNamespace()));
            count++;
        }
        return count;
    }

    private static void assertCanonicalPacketIds() {
        List<CustomPacketPayload.Type<?>> packetTypes = List.of(
                ArmCelestiumShovelSlamPayload.ID,
                OpenCelestiumCraftingPayload.ID,
                ReturnToInventoryPayload.ID,
                SyncCelestiumRagePayload.ID,
                SyncCelestiumSpearStunCooldownPayload.ID,
                SyncCelestiumThrustCooldownPayload.ID,
                SyncCelestiumTrialChamberMarkerPayload.ID,
                SyncCelestiumWallClimbInputPayload.ID,
                SyncCelestiumWallClimbStatePayload.ID,
                ToggleCelestiumPickaxeAreaModePayload.ID,
                ToggleCelestiumPickaxeEnchantModePayload.ID,
                ToggleCelestiumShovelAreaModePayload.ID
        );
        assertTrue("All twelve custom channels should be covered", packetTypes.size() == 12);
        packetTypes.forEach(type -> assertTrue(
                "Every packet channel must use the celestium namespace",
                "celestium".equals(type.id().getNamespace())
        ));
    }

    private static void assertGeneratedNamespaceIntegrity() throws IOException {
        Path assets = Path.of("src", "main", "generated", "assets");
        Path data = Path.of("src", "main", "generated", "data");
        assertTrue("Generated assets must use the canonical namespace", Files.isDirectory(assets.resolve("celestium")));
        assertTrue("Generated data must use the canonical namespace", Files.isDirectory(data.resolve("celestium")));
        String formerId = String.join("", "shops", "andtools");
        assertTrue("Generated assets must not retain the former namespace", !Files.exists(assets.resolve(formerId)));
        assertTrue("Generated data must not retain the former namespace", !Files.exists(data.resolve(formerId)));
    }

    private static void assertFabricMetadataUsesCanonicalIdAndCelestiumName() throws IOException {
        String fabricModJson = Files.readString(FABRIC_MOD_JSON_PATH);

        assertTrue(
                "Fabric metadata should use the canonical celestium runtime id",
                fabricModJson.contains("\"id\": \"celestium\"")
        );
        assertTrue(
                "Fabric metadata should provide the former dependency id during 1.1.x",
                fabricModJson.contains("\"provides\"")
                        && fabricModJson.contains("\"" + String.join("", "shops", "andtools") + "\"")
        );
        assertTrue(
                "Fabric metadata should present the mod name as Celestium",
                fabricModJson.contains("\"name\": \"Celestium\"")
        );
    }

    private static void assertGradleBuildUsesCelestiumArchiveNameAndHotfixVersion() throws IOException {
        String gradleProperties = Files.readString(GRADLE_PROPERTIES_PATH);

        assertTrue(
                "Gradle should build the published artifact as celestium",
                gradleProperties.contains("archives_base_name=celestium")
        );
        assertTrue(
                "Gradle should build the 1.1.1 hotfix",
                gradleProperties.contains("mod_version=1.1.1")
        );
    }

    private static void assertTrue(String scenario, boolean condition) {
        if (!condition) {
            throw new AssertionError(scenario);
        }
    }
}
