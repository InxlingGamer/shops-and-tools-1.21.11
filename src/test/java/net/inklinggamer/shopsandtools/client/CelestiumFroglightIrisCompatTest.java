package net.inklinggamer.shopsandtools.client;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public final class CelestiumFroglightIrisCompatTest {
    private CelestiumFroglightIrisCompatTest() {
    }

    public static void main(String[] args) {
        assertHeldItemAliasesUseReplacementMap();
        assertGenericAliasHelperUsesReplacementMap();
    }

    private static void assertHeldItemAliasesUseReplacementMap() {
        Object2IntOpenHashMap<String> mutableItemIds = new Object2IntOpenHashMap<>();
        mutableItemIds.defaultReturnValue(-1);
        mutableItemIds.put("minecraft:pearlescent_froglight", 44009);

        Object2IntMap<String> originalItemIds = Object2IntMaps.unmodifiable(mutableItemIds);
        Object2IntMap<String> patchedItemIds = CelestiumFroglightIrisCompat.createPatchedItemIds(
                originalItemIds,
                "shopsandtools:celestium_block",
                "minecraft:pearlescent_froglight",
                "shopsandtools:celestium",
                "minecraft:pearlescent_froglight"
        );

        assertTrue("Held-item aliasing should create a replacement map instead of mutating the original", patchedItemIds != originalItemIds);
        assertEquals("Celestium block items should reuse the Pearlescent Froglight held-light material ID", 44009, patchedItemIds.getInt("shopsandtools:celestium_block"));
        assertEquals("Loose Celestium should reuse the Pearlescent Froglight held-light material ID", 44009, patchedItemIds.getInt("shopsandtools:celestium"));
        assertEquals("The original unmodifiable map should stay unchanged for Celestium block items", -1, originalItemIds.getInt("shopsandtools:celestium_block"));
        assertEquals("The original unmodifiable map should stay unchanged for loose Celestium", -1, originalItemIds.getInt("shopsandtools:celestium"));
    }

    private static void assertGenericAliasHelperUsesReplacementMap() {
        Object2IntOpenHashMap<String> mutableIds = new Object2IntOpenHashMap<>();
        mutableIds.defaultReturnValue(-1);
        mutableIds.put("minecraft:pearlescent_froglight_block_state", 10688);

        Object2IntMap<String> originalIds = Object2IntMaps.unmodifiable(mutableIds);
        Object2IntMap<String> patchedIds = CelestiumFroglightIrisCompat.createPatchedAliasIds(
                originalIds,
                "shopsandtools:celestium_block_state",
                "minecraft:pearlescent_froglight_block_state"
        );

        assertTrue("Generic aliasing should create a replacement map instead of mutating the original", patchedIds != originalIds);
        assertEquals("Celestium block-state aliases should reuse the source material ID", 10688, patchedIds.getInt("shopsandtools:celestium_block_state"));
        assertEquals("The original alias source map should stay unchanged", -1, originalIds.getInt("shopsandtools:celestium_block_state"));
    }

    private static void assertTrue(String scenario, boolean condition) {
        if (!condition) {
            throw new AssertionError(scenario);
        }
    }

    private static void assertEquals(String scenario, int expected, int actual) {
        if (expected != actual) {
            throw new AssertionError(scenario + " Expected " + expected + " but got " + actual + '.');
        }
    }
}
