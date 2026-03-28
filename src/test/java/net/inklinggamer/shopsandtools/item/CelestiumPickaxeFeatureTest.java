package net.inklinggamer.shopsandtools.item;

import net.minecraft.util.math.BlockPos;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class CelestiumPickaxeFeatureTest {
    private static final Path HELPER_SOURCE_PATH = Path.of(
            "src", "main", "java", "net", "inklinggamer", "shopsandtools", "item", "CelestiumPickaxeHelper.java"
    );
    private static final Path MANAGER_SOURCE_PATH = Path.of(
            "src", "main", "java", "net", "inklinggamer", "shopsandtools", "player", "CelestiumPickaxeManager.java"
    );
    private static final Path SERVER_INTERACTION_MIXIN_SOURCE_PATH = Path.of(
            "src", "main", "java", "net", "inklinggamer", "shopsandtools", "mixin", "ServerPlayerInteractionManagerMixin.java"
    );

    private CelestiumPickaxeFeatureTest() {
    }

    public static void main(String[] args) throws IOException {
        String helperSource = Files.readString(HELPER_SOURCE_PATH);
        String managerSource = Files.readString(MANAGER_SOURCE_PATH);
        String interactionMixinSource = Files.readString(SERVER_INTERACTION_MIXIN_SOURCE_PATH);

        assertAreaMiningAppliesToEligibleCenters();
        assertBreakPredictionFallsBackForInvalidCenters();
        assertAreaDeltaFallsBackToVanillaWhenNeeded();
        assertSwitchingFromStoneToDirtKeepsModeButStopsAreaMining();
        assertStoredAreaBreakStillProcessesAfterCenterBreak();
        assertSneakingDisablesVeinMining();
        assertResolvedOreFamilyMatchingRules();
        assertVeinNeighborConnectivityIncludesDiagonals();
        assertCombinedBreakTargetsDeduplicateAreaAndVeinResults();
        assertVeinMiningUsesConventionalOreTags(helperSource);
        assertCenterStateSnapshotStillSeedsVeinMining(helperSource, managerSource, interactionMixinSource);
    }

    private static void assertAreaMiningAppliesToEligibleCenters() {
        assertTrue(
                "An eligible center block should keep 3x3 mining active",
                CelestiumPickaxeHelper.shouldApplyAreaMining(true, true)
        );
        assertFalse(
                "Area mining should stay off when the mode itself is disabled",
                CelestiumPickaxeHelper.shouldApplyAreaMining(false, true)
        );
        assertFalse(
                "An invalid center block should fall back to vanilla mining even while 3x3 mode is enabled",
                CelestiumPickaxeHelper.shouldApplyAreaMining(true, false)
        );
    }

    private static void assertBreakPredictionFallsBackForInvalidCenters() {
        assertTrue(
                "Eligible center blocks should defer break prediction for the 3x3 break",
                CelestiumPickaxeHelper.shouldDeferBreakPrediction(true, true, true)
        );
        assertFalse(
                "Dirt and gravel style targets should not defer break prediction when they are not area-mineable",
                CelestiumPickaxeHelper.shouldDeferBreakPrediction(true, true, false)
        );
        assertFalse(
                "Prediction should stay vanilla when the current breaking selection does not match the stored 3x3 center",
                CelestiumPickaxeHelper.shouldDeferBreakPrediction(true, false, true)
        );
    }

    private static void assertAreaDeltaFallsBackToVanillaWhenNeeded() {
        assertEquals(
                "Eligible centers should use the slowest 3x3 mining delta",
                0.125F,
                CelestiumPickaxeHelper.resolveAreaMiningDelta(true, true, 0.125F, 0.75F)
        );
        assertEquals(
                "Invalid centers should preserve the vanilla single-block mining delta",
                0.75F,
                CelestiumPickaxeHelper.resolveAreaMiningDelta(true, false, 0.125F, 0.75F)
        );
        assertEquals(
                "Missing 3x3 targets should preserve the vanilla mining delta",
                0.75F,
                CelestiumPickaxeHelper.resolveAreaMiningDelta(true, true, 0.0F, 0.75F)
        );
    }

    private static void assertSwitchingFromStoneToDirtKeepsModeButStopsAreaMining() {
        boolean areaModeEnabled = true;

        assertTrue(
                "A stone center should start with 3x3 mining active",
                CelestiumPickaxeHelper.shouldApplyAreaMining(areaModeEnabled, true)
        );
        assertFalse(
                "Switching directly to dirt while 3x3 stays enabled should immediately fall back to vanilla mining",
                CelestiumPickaxeHelper.shouldApplyAreaMining(areaModeEnabled, false)
        );
        assertFalse(
                "The dirt attempt should also stop deferred break prediction so the block breaks normally",
                CelestiumPickaxeHelper.shouldDeferBreakPrediction(areaModeEnabled, true, false)
        );
    }

    private static void assertStoredAreaBreakStillProcessesAfterCenterBreak() {
        assertTrue(
                "Once the stored 3x3 selection matches the broken center, the server should still process neighboring blocks after the center turns to air",
                CelestiumPickaxeHelper.shouldProcessStoredAreaBreak(true, true)
        );
        assertFalse(
                "Stored area breaking should stop when 3x3 mode is disabled",
                CelestiumPickaxeHelper.shouldProcessStoredAreaBreak(false, true)
        );
        assertFalse(
                "Stored area breaking should stop when the broken block no longer matches the active 3x3 selection",
                CelestiumPickaxeHelper.shouldProcessStoredAreaBreak(true, false)
        );
    }

    private static void assertSneakingDisablesVeinMining() {
        assertTrue(
                "Vein mining should stay enabled when the player is not sneaking",
                CelestiumPickaxeHelper.shouldApplyVeinMining(false)
        );
        assertFalse(
                "Holding sneak should disable vein mining without affecting the normal pickaxe break flow",
                CelestiumPickaxeHelper.shouldApplyVeinMining(true)
        );
    }

    private static void assertResolvedOreFamilyMatchingRules() {
        assertTrue(
                "Iron ore should share a vein with deepslate iron ore when they resolve to the same ore family",
                CelestiumPickaxeHelper.matchesResolvedOreFamily("ores/iron", "minecraft:iron_ore", "ores/iron", "minecraft:deepslate_iron_ore")
        );
        assertFalse(
                "Iron ore should not share a vein with gold ore when their resolved ore families differ",
                CelestiumPickaxeHelper.matchesResolvedOreFamily("ores/iron", "minecraft:iron_ore", "ores/gold", "minecraft:gold_ore")
        );
        assertTrue(
                "Fallback exact matching should keep the same block grouped together when no specific ore family resolves",
                CelestiumPickaxeHelper.matchesResolvedOreFamily(null, "minecraft:stone", null, "minecraft:stone")
        );
        assertFalse(
                "Fallback exact matching should reject different blocks when no specific ore family resolves",
                CelestiumPickaxeHelper.matchesResolvedOreFamily(null, "minecraft:stone", null, "minecraft:deepslate")
        );
    }

    private static void assertVeinNeighborConnectivityIncludesDiagonals() {
        List<BlockPos> neighbors = CelestiumPickaxeHelper.getVeinMiningNeighbors(BlockPos.ORIGIN);

        assertEquals(
                "Vein mining should inspect all 26 neighboring blocks around the current ore",
                26,
                neighbors.size()
        );
        assertTrue(
                "Face-adjacent blocks should count as connected vein neighbors",
                neighbors.contains(new BlockPos(1, 0, 0))
        );
        assertTrue(
                "Diagonal corner blocks should count as connected vein neighbors",
                neighbors.contains(new BlockPos(1, 1, 1))
        );
        assertFalse(
                "The current block itself should not be treated as its own neighbor",
                neighbors.contains(BlockPos.ORIGIN)
        );
    }

    private static void assertCombinedBreakTargetsDeduplicateAreaAndVeinResults() {
        BlockPos center = BlockPos.ORIGIN;
        List<BlockPos> areaTargets = List.of(
                new BlockPos(1, 0, 0),
                new BlockPos(2, 0, 0)
        );
        List<BlockPos> veinTargets = List.of(
                center,
                new BlockPos(2, 0, 0),
                new BlockPos(3, 0, 0)
        );

        assertListEquals(
                "Combined 3x3 and vein mining targets should stay deduplicated and should not re-break the center block",
                List.of(
                        new BlockPos(1, 0, 0),
                        new BlockPos(2, 0, 0),
                        new BlockPos(3, 0, 0)
                ),
                CelestiumPickaxeHelper.combineSecondaryBreakTargets(center, areaTargets, veinTargets)
        );
    }

    private static void assertVeinMiningUsesConventionalOreTags(String helperSource) {
        assertContains(
                "Vein mining should stay gated behind the broad conventional ore tag so non-ores never enter the vein batch",
                helperSource,
                "ConventionalBlockTags.ORES"
        );
        assertContains("Iron ore family resolution should use the conventional iron ore tag", helperSource, "ConventionalBlockTags.IRON_ORES");
        assertContains("Gold ore family resolution should use the conventional gold ore tag", helperSource, "ConventionalBlockTags.GOLD_ORES");
        assertContains("Copper ore family resolution should use the conventional copper ore tag", helperSource, "ConventionalBlockTags.COPPER_ORES");
        assertContains("Diamond ore family resolution should use the conventional diamond ore tag", helperSource, "ConventionalBlockTags.DIAMOND_ORES");
        assertContains("Emerald ore family resolution should use the conventional emerald ore tag", helperSource, "ConventionalBlockTags.EMERALD_ORES");
        assertContains("Lapis ore family resolution should use the conventional lapis ore tag", helperSource, "ConventionalBlockTags.LAPIS_ORES");
        assertContains("Redstone ore family resolution should use the conventional redstone ore tag", helperSource, "ConventionalBlockTags.REDSTONE_ORES");
        assertContains("Coal ore family resolution should use the conventional coal ore tag", helperSource, "ConventionalBlockTags.COAL_ORES");
        assertContains("Quartz ore family resolution should use the conventional quartz ore tag", helperSource, "ConventionalBlockTags.QUARTZ_ORES");
        assertContains("Ancient debris should resolve through the conventional netherite scrap ore tag", helperSource, "ConventionalBlockTags.NETHERITE_SCRAP_ORES");
    }

    private static void assertCenterStateSnapshotStillSeedsVeinMining(String helperSource, String managerSource, String interactionMixinSource) {
        assertContains(
                "The pickaxe helper should be able to copy the captured enchantment and mode components back onto the live held pickaxe before secondary breaks",
                helperSource,
                "targetStack.copy(DataComponentTypes.ENCHANTMENTS, sourceStack);"
        );
        assertContains(
                "The pickaxe helper should also restore the custom mode state from the captured tool snapshot before secondary breaks",
                helperSource,
                "targetStack.copy(DataComponentTypes.CUSTOM_DATA, sourceStack);"
        );
        assertContains(
                "The pickaxe manager should accept the broken center state so the original ore can still seed vein mining after the world turns it to air",
                managerSource,
                "BlockState centerState,"
        );
        assertContains(
                "The pickaxe manager should also receive the captured pre-break tool snapshot for secondary breaks",
                managerSource,
                "ItemStack breakingTool"
        );
        assertContains(
                "The pickaxe manager should combine area and vein results into one deduplicated secondary break batch",
                managerSource,
                "combineSecondaryBreakTargets(centerPos, areaTargets, veinTargets)"
        );
        assertContains(
                "The pickaxe manager should track whether vein mining actually activated before playing extra break audio",
                managerSource,
                "boolean veinMiningActivated = !veinTargets.isEmpty();"
        );
        assertContains(
                "The pickaxe manager should skip vein target collection while the player is sneaking",
                managerSource,
                "shouldApplyVeinMining(player.isSneaking())"
        );
        assertContains(
                "The pickaxe manager should play each extra block's break sound when a vein-mined batch fires",
                managerSource,
                "if (interactionManager.tryBreakBlock(targetPos) && veinMiningActivated) {"
        );
        assertContains(
                "Vein-mined secondary breaks should use the broken block's own sound group",
                managerSource,
                "soundGroup.getBreakSound()"
        );
        assertContains(
                "The server interaction mixin should pass the captured pre-break block state into the pickaxe manager",
                interactionMixinSource,
                "snapshot != null && snapshot.pos().equals(pos) ? snapshot.state() : this.player.getEntityWorld().getBlockState(pos),"
        );
        assertContains(
                "The server interaction mixin should also pass the captured pre-break tool snapshot into the pickaxe manager",
                interactionMixinSource,
                "snapshot != null && snapshot.pos().equals(pos) ? snapshot.tool() : this.player.getMainHandStack().copy()"
        );
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

    private static void assertEquals(String scenario, float expected, float actual) {
        if (Math.abs(expected - actual) > 1.0E-6F) {
            throw new AssertionError(scenario + " expected " + expected + " but got " + actual);
        }
    }

    private static void assertEquals(String scenario, int expected, int actual) {
        if (expected != actual) {
            throw new AssertionError(scenario + " expected " + expected + " but got " + actual);
        }
    }

    private static void assertListEquals(String scenario, List<BlockPos> expected, List<BlockPos> actual) {
        if (!expected.equals(actual)) {
            throw new AssertionError(scenario + " expected " + expected + " but got " + actual);
        }
    }

    private static void assertContains(String scenario, String contents, String expectedSnippet) {
        if (!contents.contains(expectedSnippet)) {
            throw new AssertionError(scenario + " Missing snippet: " + expectedSnippet);
        }
    }
}
