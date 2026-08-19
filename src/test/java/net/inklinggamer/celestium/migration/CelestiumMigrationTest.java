package net.inklinggamer.celestium.migration;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.block.Blocks;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class CelestiumMigrationTest {
    private CelestiumMigrationTest() {
    }

    public static void main(String[] args) throws IOException {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        assertComponentPatchIsPreserved();
        assertBlockReplacementIsIdempotent();
        assertProgressTransferDoesNotAwardRewards();
        assertCompatibilityResourcesAreIsolated();
    }

    private static void assertComponentPatchIsPreserved() throws IOException {
        String source = bridgeSource();
        assertTrue(
                "The canonical stack should preserve count and the complete component patch",
                source.contains("canonical.builtInRegistryHolder(), stack.getCount(), stack.getComponentsPatch()")
        );
    }

    private static void assertBlockReplacementIsIdempotent() {
        assertTrue(
                "A legacy block should become the canonical block",
                LegacyContentBridge.convertBlockState(Blocks.DIRT.defaultBlockState(), Blocks.DIRT, Blocks.DIAMOND_BLOCK).is(Blocks.DIAMOND_BLOCK)
        );
        assertTrue(
                "A canonical block should remain unchanged on later passes",
                LegacyContentBridge.convertBlockState(Blocks.DIAMOND_BLOCK.defaultBlockState(), Blocks.DIRT, Blocks.DIAMOND_BLOCK).is(Blocks.DIAMOND_BLOCK)
        );
    }

    private static void assertProgressTransferDoesNotAwardRewards() throws IOException {
        String source = bridgeSource();
        assertTrue("Advancement progress should be copied directly", source.contains("newProgress.grantProgress(criterion)"));
        assertTrue("Migration must not call the reward-awarding advancement API", !source.contains("playerAdvancements.award("));
        assertTrue("Recipe progress should move to the canonical key", source.contains("player.getRecipeBook().add(canonical)"));
        assertTrue("The legacy recipe key should be removed after transfer", source.contains("player.getRecipeBook().remove(legacy)"));
    }

    private static String bridgeSource() throws IOException {
        return Files.readString(Path.of(
                "src", "main", "java", "net", "inklinggamer", "celestium", "migration", "LegacyContentBridge.java"));
    }

    private static void assertCompatibilityResourcesAreIsolated() throws IOException {
        Path legacyData = Path.of("src", "main", "resources", "data", LegacyContentBridge.LEGACY_MOD_ID);
        Path legacyAssets = Path.of("src", "main", "resources", "assets", LegacyContentBridge.LEGACY_MOD_ID);
        assertTrue("Legacy data should be isolated in its compatibility namespace", Files.isDirectory(legacyData));
        assertTrue("Legacy assets should be isolated in its compatibility namespace", Files.isDirectory(legacyAssets));
        assertTrue("The canonical namespace should exist", Files.isDirectory(Path.of("src", "main", "resources", "data", "celestium")));
    }

    private static void assertTrue(String scenario, boolean condition) {
        if (!condition) {
            throw new AssertionError(scenario);
        }
    }
}
