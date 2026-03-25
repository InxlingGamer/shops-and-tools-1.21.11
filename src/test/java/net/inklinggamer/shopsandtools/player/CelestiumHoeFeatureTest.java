package net.inklinggamer.shopsandtools.player;

import net.inklinggamer.shopsandtools.item.CelestiumHoeHelper;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public final class CelestiumHoeFeatureTest {
    private static final RegistryKey<World> OVERWORLD = RegistryKey.of(RegistryKeys.WORLD, Identifier.of("minecraft", "overworld"));
    private static final RegistryKey<World> NETHER = RegistryKey.of(RegistryKeys.WORLD, Identifier.of("minecraft", "the_nether"));

    private CelestiumHoeFeatureTest() {
    }

    public static void main(String[] args) {
        assertSupportedCropRecognition();
        assertMatureOnlyFiltering();
        assertHorizontalTargetCollection();
        assertReplantMapping();
        assertReplantCostSubtraction();
        assertGrowthAuraBoundaries();
        assertGrowthAuraWorldFiltering();
    }

    private static void assertSupportedCropRecognition() {
        assertEquals("Wheat should map to a supported crop type", CelestiumHoeHelper.SupportedCropType.WHEAT, CelestiumHoeHelper.getCropType("minecraft:wheat"));
        assertEquals("Carrots should map to a supported crop type", CelestiumHoeHelper.SupportedCropType.CARROT, CelestiumHoeHelper.getCropType("minecraft:carrots"));
        assertEquals("Potatoes should map to a supported crop type", CelestiumHoeHelper.SupportedCropType.POTATO, CelestiumHoeHelper.getCropType("minecraft:potatoes"));
        assertEquals("Beetroots should map to a supported crop type", CelestiumHoeHelper.SupportedCropType.BEETROOT, CelestiumHoeHelper.getCropType("minecraft:beetroots"));
        assertEquals("Nether wart should map to a supported crop type", CelestiumHoeHelper.SupportedCropType.NETHER_WART, CelestiumHoeHelper.getCropType("minecraft:nether_wart"));
        assertEquals("Unsupported crops should not map to a type", null, CelestiumHoeHelper.getCropType("minecraft:cocoa"));
    }

    private static void assertMatureOnlyFiltering() {
        assertTrue("Mature wheat age should be accepted", CelestiumHoeHelper.isMatureAge(CelestiumHoeHelper.SupportedCropType.WHEAT, 7));
        assertFalse("Immature wheat age should be rejected", CelestiumHoeHelper.isMatureAge(CelestiumHoeHelper.SupportedCropType.WHEAT, 6));
        assertTrue("Mature beetroot age should be accepted", CelestiumHoeHelper.isMatureAge(CelestiumHoeHelper.SupportedCropType.BEETROOT, 3));
        assertTrue("Mature carrot age should be accepted", CelestiumHoeHelper.isMatureAge(CelestiumHoeHelper.SupportedCropType.CARROT, 7));
        assertTrue("Mature potato age should be accepted", CelestiumHoeHelper.isMatureAge(CelestiumHoeHelper.SupportedCropType.POTATO, 7));
        assertTrue("Mature nether wart age should be accepted", CelestiumHoeHelper.isMatureAge(CelestiumHoeHelper.SupportedCropType.NETHER_WART, 3));
        assertFalse("Immature nether wart age should be rejected", CelestiumHoeHelper.isMatureAge(CelestiumHoeHelper.SupportedCropType.NETHER_WART, 2));
    }

    private static void assertHorizontalTargetCollection() {
        BlockPos center = new BlockPos(0, 64, 0);
        List<BlockPos> targets = CelestiumHoeHelper.getHorizontalArea(center);

        assertTrue("Center crop should be included", targets.contains(center));
        assertTrue("East block on the same plane should be included", targets.contains(center.east()));
        assertTrue("North block on the same plane should be included", targets.contains(center.north()));
        assertTrue("West block on the same plane should be included", targets.contains(center.west()));
        assertFalse("Blocks above the center plane should not be included", targets.contains(center.up()));
        assertEquals("The horizontal area should contain exactly 9 blocks", 9, targets.size());
    }

    private static void assertReplantMapping() {
        assertEquals("Wheat should consume seeds to replant", "minecraft:wheat_seeds", CelestiumHoeHelper.SupportedCropType.WHEAT.replantCostItemId());
        assertEquals("Beetroots should consume beetroot seeds to replant", "minecraft:beetroot_seeds", CelestiumHoeHelper.SupportedCropType.BEETROOT.replantCostItemId());
        assertEquals("Carrots should consume carrots to replant", "minecraft:carrot", CelestiumHoeHelper.SupportedCropType.CARROT.replantCostItemId());
        assertEquals("Potatoes should consume potatoes to replant", "minecraft:potato", CelestiumHoeHelper.SupportedCropType.POTATO.replantCostItemId());
        assertEquals("Nether wart should consume nether wart to replant", "minecraft:nether_wart", CelestiumHoeHelper.SupportedCropType.NETHER_WART.replantCostItemId());
    }

    private static void assertReplantCostSubtraction() {
        assertEquals("Replanting wheat should consume one seed", 2, CelestiumHoeHelper.getRemainingCountAfterReplant(3));
        assertEquals("Replanting carrots should consume one carrot", 3, CelestiumHoeHelper.getRemainingCountAfterReplant(4));
        assertEquals("Replanting nether wart should consume one wart", 4, CelestiumHoeHelper.getRemainingCountAfterReplant(5));
        assertEquals("Drop counts should not go negative", 0, CelestiumHoeHelper.getRemainingCountAfterReplant(0));
    }

    private static void assertGrowthAuraBoundaries() {
        assertTrue("A crop exactly 50 blocks away on X should be boosted", CelestiumHoeManager.isWithinGrowthAura(0.5D, 0.5D, new BlockPos(50, 64, 0)));
        assertTrue("A crop exactly 50 blocks away on both X and Z should be boosted", CelestiumHoeManager.isWithinGrowthAura(0.5D, 0.5D, new BlockPos(50, 64, 50)));
        assertFalse("A crop beyond 50 blocks on X should not be boosted", CelestiumHoeManager.isWithinGrowthAura(0.5D, 0.5D, new BlockPos(51, 64, 0)));
        assertFalse("A crop beyond 50 blocks on Z should not be boosted", CelestiumHoeManager.isWithinGrowthAura(0.5D, 0.5D, new BlockPos(0, 64, 51)));
    }

    private static void assertGrowthAuraWorldFiltering() {
        List<CelestiumHoeManager.ActiveHolder> holders = List.of(
                new CelestiumHoeManager.ActiveHolder(OVERWORLD, 0.5D, 0.5D),
                new CelestiumHoeManager.ActiveHolder(OVERWORLD, 10.5D, 10.5D)
        );

        assertTrue(
                "At least one overlapping holder in the same world should boost the crop",
                CelestiumHoeManager.anyHolderBoostsCrop(OVERWORLD, new BlockPos(10, 64, 10), holders)
        );
        assertFalse(
                "Holders in a different world should not boost the crop",
                CelestiumHoeManager.anyHolderBoostsCrop(NETHER, new BlockPos(10, 64, 10), holders)
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

    private static void assertEquals(String scenario, Object expected, Object actual) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError(scenario + " expected " + expected + " but got " + actual);
        }
    }
}
