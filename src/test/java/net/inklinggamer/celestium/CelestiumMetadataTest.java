package net.inklinggamer.celestium;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class CelestiumMetadataTest {
    private static final Path FABRIC_MOD_JSON_PATH = Path.of("src", "main", "resources", "fabric.mod.json");
    private static final Path GRADLE_PROPERTIES_PATH = Path.of("gradle.properties");

    private CelestiumMetadataTest() {
    }

    public static void main(String[] args) throws IOException {
        assertFabricMetadataUsesExactSideReleaseContract();
        assertGradleBuildUsesExactSideReleaseContract();
    }

    private static void assertFabricMetadataUsesExactSideReleaseContract() throws IOException {
        String fabricModJson = Files.readString(FABRIC_MOD_JSON_PATH);

        assertTrue(
                "Fabric metadata should keep the celestium runtime id for compatibility",
                fabricModJson.contains("\"id\": \"celestium\"")
        );
        assertTrue(
                "Fabric metadata should present the mod name as Celestium",
                fabricModJson.contains("\"name\": \"Celestium\"")
        );
        assertTrue(
                "The side release must accept only Minecraft 1.21.1 and therefore reject 26.2",
                fabricModJson.contains("\"minecraft\": \"=1.21.1\"")
                        && !fabricModJson.contains("26.2")
        );
        assertTrue(
                "The side release must require Java 21, Fabric Loader 0.19.3, and Fabric API",
                fabricModJson.contains("\"java\": \">=21\"")
                        && fabricModJson.contains("\"fabricloader\": \">=0.19.3\"")
                        && fabricModJson.contains("\"fabric-api\": \"*\"")
        );
        assertTrue(
                "Iris must remain optional and no legacy mod-id alias may be provided",
                fabricModJson.contains("\"iris\": \"*\"")
                        && !fabricModJson.contains("\"provides\"")
                        && !fabricModJson.contains("shopsandtools")
        );
    }

    private static void assertGradleBuildUsesExactSideReleaseContract() throws IOException {
        Properties gradleProperties = new Properties();
        try (var reader = Files.newBufferedReader(GRADLE_PROPERTIES_PATH)) {
            gradleProperties.load(reader);
        }

        assertTrue(
                "Gradle must build exactly the independent 1.21.1 1.0.1 release line",
                "celestium-1.21.1".equals(gradleProperties.getProperty("archives_base_name"))
                        && "1.0.1".equals(gradleProperties.getProperty("mod_version"))
                        && "1.21.1".equals(gradleProperties.getProperty("minecraft_version"))
                        && "1.21.1+build.3".equals(gradleProperties.getProperty("yarn_mappings"))
                        && "0.19.3".equals(gradleProperties.getProperty("loader_version"))
                        && "0.116.15+1.21.1".equals(gradleProperties.getProperty("fabric_api_version"))
        );
    }

    private static void assertTrue(String scenario, boolean condition) {
        if (!condition) {
            throw new AssertionError(scenario);
        }
    }
}
