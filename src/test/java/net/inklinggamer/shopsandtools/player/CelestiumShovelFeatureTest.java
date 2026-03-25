package net.inklinggamer.shopsandtools.player;

import net.inklinggamer.shopsandtools.item.CelestiumShovelHelper;

public final class CelestiumShovelFeatureTest {
    private CelestiumShovelFeatureTest() {
    }

    public static void main(String[] args) {
        assertPathConvertibleClassification();
        assertExcavationTargetClassification();
        assertAreaToggleDecisionRules();
    }

    private static void assertPathConvertibleClassification() {
        assertTrue("Dirt should preserve vanilla path conversion", CelestiumShovelHelper.isPathConvertibleShovelBlockId("minecraft:dirt"));
        assertTrue("Grass should preserve vanilla path conversion", CelestiumShovelHelper.isPathConvertibleShovelBlockId("minecraft:grass_block"));
        assertTrue("Coarse dirt should preserve vanilla path conversion", CelestiumShovelHelper.isPathConvertibleShovelBlockId("minecraft:coarse_dirt"));
        assertTrue("Podzol should preserve vanilla path conversion", CelestiumShovelHelper.isPathConvertibleShovelBlockId("minecraft:podzol"));
        assertTrue("Rooted dirt should preserve vanilla path conversion", CelestiumShovelHelper.isPathConvertibleShovelBlockId("minecraft:rooted_dirt"));
        assertTrue("Mycelium should preserve vanilla path conversion", CelestiumShovelHelper.isPathConvertibleShovelBlockId("minecraft:mycelium"));
    }

    private static void assertExcavationTargetClassification() {
        assertFalse("Gravel should not preserve vanilla shovel use", CelestiumShovelHelper.isPathConvertibleShovelBlockId("minecraft:gravel"));
        assertFalse("Sand should not preserve vanilla shovel use", CelestiumShovelHelper.isPathConvertibleShovelBlockId("minecraft:sand"));
        assertFalse("Red sand should not preserve vanilla shovel use", CelestiumShovelHelper.isPathConvertibleShovelBlockId("minecraft:red_sand"));
        assertFalse("Clay should not preserve vanilla shovel use", CelestiumShovelHelper.isPathConvertibleShovelBlockId("minecraft:clay"));
        assertFalse("Mud should not preserve vanilla shovel use", CelestiumShovelHelper.isPathConvertibleShovelBlockId("minecraft:mud"));
        assertFalse("Soul sand should not preserve vanilla shovel use", CelestiumShovelHelper.isPathConvertibleShovelBlockId("minecraft:soul_sand"));
        assertFalse("Soul soil should not preserve vanilla shovel use", CelestiumShovelHelper.isPathConvertibleShovelBlockId("minecraft:soul_soil"));
    }

    private static void assertAreaToggleDecisionRules() {
        assertTrue(
                "Area toggle should be allowed on non-path excavation targets",
                CelestiumShovelHelper.shouldAllowAreaToggle(false, false, false, true, false)
        );
        assertFalse(
                "Area toggle should be blocked when vanilla path conversion should win",
                CelestiumShovelHelper.shouldAllowAreaToggle(false, false, false, true, true)
        );
        assertFalse(
                "Area toggle should be blocked for interactive block targets",
                CelestiumShovelHelper.shouldAllowAreaToggle(false, true, false, false, false)
        );
        assertFalse(
                "Area toggle should be blocked when an offhand block can be placed",
                CelestiumShovelHelper.shouldAllowAreaToggle(false, false, true, false, false)
        );
        assertFalse(
                "Area toggle should be blocked while targeting an entity",
                CelestiumShovelHelper.shouldAllowAreaToggle(true, false, false, false, false)
        );
        assertFalse(
                "Area toggle should be blocked for path blocks too",
                CelestiumShovelHelper.shouldAllowAreaToggle(false, false, false, true, true)
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
}
