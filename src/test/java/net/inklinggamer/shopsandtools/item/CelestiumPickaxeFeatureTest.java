package net.inklinggamer.shopsandtools.item;

public final class CelestiumPickaxeFeatureTest {
    private CelestiumPickaxeFeatureTest() {
    }

    public static void main(String[] args) {
        assertAreaMiningAppliesToEligibleCenters();
        assertBreakPredictionFallsBackForInvalidCenters();
        assertAreaDeltaFallsBackToVanillaWhenNeeded();
        assertSwitchingFromStoneToDirtKeepsModeButStopsAreaMining();
        assertStoredAreaBreakStillProcessesAfterCenterBreak();
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
}
