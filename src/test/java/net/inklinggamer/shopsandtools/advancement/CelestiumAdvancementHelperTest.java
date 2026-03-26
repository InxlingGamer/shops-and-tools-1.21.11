package net.inklinggamer.shopsandtools.advancement;

public final class CelestiumAdvancementHelperTest {
    private CelestiumAdvancementHelperTest() {
    }

    public static void main(String[] args) {
        assertChestVariantRecognition();
        assertFullSetRecognition();
    }

    private static void assertChestVariantRecognition() {
        assertTrue(
                "Standard Celestium chestplates should count toward Fully Ascended",
                CelestiumAdvancementHelper.isCelestiumChestItem(CelestiumAdvancementHelper.CELESTIUM_CHESTPLATE_ID)
        );
        assertTrue(
                "Celestium Elytra chestplates should count toward Fully Ascended",
                CelestiumAdvancementHelper.isCelestiumChestItem(CelestiumAdvancementHelper.CELESTIUM_ELYTRA_CHESTPLATE_ID)
        );
        assertFalse(
                "Non-Celestium chest items should not count toward Fully Ascended",
                CelestiumAdvancementHelper.isCelestiumChestItem("minecraft:elytra")
        );
    }

    private static void assertFullSetRecognition() {
        assertTrue(
                "A full standard Celestium set should unlock Fully Ascended",
                CelestiumAdvancementHelper.isFullyAscended(
                        CelestiumAdvancementHelper.CELESTIUM_HELMET_ID,
                        CelestiumAdvancementHelper.CELESTIUM_CHESTPLATE_ID,
                        CelestiumAdvancementHelper.CELESTIUM_LEGGINGS_ID,
                        CelestiumAdvancementHelper.CELESTIUM_BOOTS_ID
                )
        );
        assertTrue(
                "The Elytra chest variant should also satisfy Fully Ascended",
                CelestiumAdvancementHelper.isFullyAscended(
                        CelestiumAdvancementHelper.CELESTIUM_HELMET_ID,
                        CelestiumAdvancementHelper.CELESTIUM_ELYTRA_CHESTPLATE_ID,
                        CelestiumAdvancementHelper.CELESTIUM_LEGGINGS_ID,
                        CelestiumAdvancementHelper.CELESTIUM_BOOTS_ID
                )
        );
        assertFalse(
                "Missing any required piece should prevent Fully Ascended",
                CelestiumAdvancementHelper.isFullyAscended(
                        CelestiumAdvancementHelper.CELESTIUM_HELMET_ID,
                        "minecraft:elytra",
                        CelestiumAdvancementHelper.CELESTIUM_LEGGINGS_ID,
                        CelestiumAdvancementHelper.CELESTIUM_BOOTS_ID
                )
        );
        assertFalse(
                "Incorrect boots should prevent Fully Ascended",
                CelestiumAdvancementHelper.isFullyAscended(
                        CelestiumAdvancementHelper.CELESTIUM_HELMET_ID,
                        CelestiumAdvancementHelper.CELESTIUM_CHESTPLATE_ID,
                        CelestiumAdvancementHelper.CELESTIUM_LEGGINGS_ID,
                        "minecraft:netherite_boots"
                )
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
