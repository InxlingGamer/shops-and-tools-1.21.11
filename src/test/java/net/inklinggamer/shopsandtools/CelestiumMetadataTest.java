package net.inklinggamer.shopsandtools;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class CelestiumMetadataTest {
    private static final Path FABRIC_MOD_JSON_PATH = Path.of("src", "main", "resources", "fabric.mod.json");
    private static final Path GRADLE_PROPERTIES_PATH = Path.of("gradle.properties");

    private CelestiumMetadataTest() {
    }

    public static void main(String[] args) throws IOException {
        assertFabricMetadataUsesCompatibilityIdAndCelestiumName();
        assertGradleBuildUsesCelestiumArchiveName();
    }

    private static void assertFabricMetadataUsesCompatibilityIdAndCelestiumName() throws IOException {
        String fabricModJson = Files.readString(FABRIC_MOD_JSON_PATH);

        assertTrue(
                "Fabric metadata should keep the shopsandtools runtime id for compatibility",
                fabricModJson.contains("\"id\": \"shopsandtools\"")
        );
        assertTrue(
                "Fabric metadata should present the mod name as Celestium",
                fabricModJson.contains("\"name\": \"Celestium\"")
        );
    }

    private static void assertGradleBuildUsesCelestiumArchiveName() throws IOException {
        String gradleProperties = Files.readString(GRADLE_PROPERTIES_PATH);

        assertTrue(
                "Gradle should build the published artifact as celestium",
                gradleProperties.contains("archives_base_name=celestium")
        );
    }

    private static void assertTrue(String scenario, boolean condition) {
        if (!condition) {
            throw new AssertionError(scenario);
        }
    }
}
