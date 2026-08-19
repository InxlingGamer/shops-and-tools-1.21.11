package net.inklinggamer.shopsandtools.block;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class CelestiumBlockPropertiesTest {
    private static final Path MOD_BLOCKS_SOURCE = Path.of(
            "src", "main", "java", "net", "inklinggamer", "shopsandtools", "block", "ModBlocks.java");

    private CelestiumBlockPropertiesTest() {
    }

    public static void main(String[] args) {
        assertPearlescentFroglightIsTheCopySource();
        assertCopyHelperUsesVanillaCopy();
        assertExpectedCelestiumOverridesExist();
        assertNoUnexpectedLightOverridesExist();
    }

    private static void assertPearlescentFroglightIsTheCopySource() {
        String modBlocksSource = readSourceFile();
        assertTrue(
                "Celestium should explicitly copy Pearlescent Froglight settings",
                modBlocksSource.contains("copyBlockSettings(Blocks.PEARLESCENT_FROGLIGHT)")
        );
    }

    private static void assertCopyHelperUsesVanillaCopy() {
        String modBlocksSource = readSourceFile();
        assertTrue(
                "Celestium should use BlockBehaviour.Properties.ofFullCopy for full vanilla parity",
                modBlocksSource.contains("return BlockBehaviour.Properties.ofFullCopy(block);")
        );
    }

    private static void assertExpectedCelestiumOverridesExist() {
        String modBlocksSource = readSourceFile();
        String createCelestiumSettingsBody = extractCreateCelestiumSettingsBody(modBlocksSource);

        assertTrue(
                "Celestium should use obsidian hardness and blast resistance",
                createCelestiumSettingsBody.contains(".strength(Blocks.OBSIDIAN.defaultDestroyTime(), Blocks.OBSIDIAN.getExplosionResistance())")
        );
        assertTrue(
                "Celestium should require the correct tool like obsidian",
                createCelestiumSettingsBody.contains(".requiresCorrectToolForDrops()")
        );
        assertTrue(
                "Celestium should use the amethyst block sound group",
                createCelestiumSettingsBody.contains(".sound(SoundType.AMETHYST)")
        );
    }

    private static void assertNoUnexpectedLightOverridesExist() {
        String modBlocksSource = readSourceFile();
        String createCelestiumSettingsBody = extractCreateCelestiumSettingsBody(modBlocksSource);

        assertFalse("Celestium should keep froglight luminance without overriding it", createCelestiumSettingsBody.contains(".luminance("));
        assertFalse("Celestium should keep froglight map color without overriding it", createCelestiumSettingsBody.contains(".mapColor("));
    }

    private static String readSourceFile() {
        try {
            return Files.readString(MOD_BLOCKS_SOURCE);
        } catch (IOException exception) {
            throw new AssertionError("Failed to read ModBlocks source file", exception);
        }
    }

    private static String extractCreateCelestiumSettingsBody(String source) {
        String methodSignature = "static BlockBehaviour.Properties createCelestiumSettings()";
        int methodStart = source.indexOf(methodSignature);
        if (methodStart < 0) {
            throw new AssertionError("Could not find createCelestiumSettings() in ModBlocks");
        }

        int bodyStart = source.indexOf('{', methodStart);
        int bodyEnd = source.indexOf('}', bodyStart);
        if (bodyStart < 0 || bodyEnd < 0) {
            throw new AssertionError("Could not parse createCelestiumSettings() body in ModBlocks");
        }

        return source.substring(bodyStart + 1, bodyEnd);
    }

    private static void assertTrue(String scenario, boolean condition) {
        if (!condition) {
            throw new AssertionError(scenario);
        }
    }

    private static void assertFalse(String scenario, boolean condition) {
        if (condition) {
            throw new AssertionError(scenario);
        }
    }
}
