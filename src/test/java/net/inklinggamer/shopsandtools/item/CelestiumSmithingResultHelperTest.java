package net.inklinggamer.shopsandtools.item;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class CelestiumSmithingResultHelperTest {
    private static final Path HELPER_SOURCE_PATH = Path.of(
            "src", "main", "java", "net", "inklinggamer", "shopsandtools", "item", "CelestiumSmithingResultHelper.java"
    );

    private static final Path SMITHING_MIXIN_SOURCE_PATH = Path.of(
            "src", "main", "java", "net", "inklinggamer", "shopsandtools", "mixin", "SmithingScreenHandlerMixin.java"
    );

    private static final String[] WEARABLE_CELESTIUM_ARMOR_REFERENCES = {
            "ModItems.CELESTIUM_HELMET",
            "ModItems.CELESTIUM_CHESTPLATE",
            "ModItems.CELESTIUM_LEGGINGS",
            "ModItems.CELESTIUM_BOOTS"
    };

    private CelestiumSmithingResultHelperTest() {
    }

    public static void main(String[] args) throws IOException {
        String helperSource = Files.readString(HELPER_SOURCE_PATH);
        String smithingMixinSource = Files.readString(SMITHING_MIXIN_SOURCE_PATH);

        assertTrimCleanupOperatesOnACopy(helperSource);
        assertWearableCelestiumArmorIsTrimmed(helperSource);
        assertSpecialSmithingEnchantmentsRemain(helperSource);
        assertNonCelestiumResultsRemainUnchanged(helperSource);
        assertSmithingMixinStillUsesPostProcessing(helperSource, smithingMixinSource);
    }

    private static void assertTrimCleanupOperatesOnACopy(String helperSource) {
        assertContains(
                "Celestium smithing results should still start from a copied vanilla output",
                helperSource,
                "ItemStack upgradedResult = result.copy();"
        );
        assertContains(
                "Celestium smithing results should strip trim from the copied result stack",
                helperSource,
                "removeArmorTrimIfPresent(upgradedResult);"
        );
    }

    private static void assertWearableCelestiumArmorIsTrimmed(String helperSource) {
        assertContains(
                "Celestium smithing should remove the trim component from wearable armor results",
                helperSource,
                "stack.remove(DataComponentTypes.TRIM);"
        );

        for (String armorReference : WEARABLE_CELESTIUM_ARMOR_REFERENCES) {
            assertContains(
                    "Wearable celestium armor should be included in the trim cleanup set: " + armorReference,
                    helperSource,
                    armorReference
            );
        }
    }

    private static void assertSpecialSmithingEnchantmentsRemain(String helperSource) {
        int trimRemovalIndex = helperSource.indexOf("removeArmorTrimIfPresent(upgradedResult);");
        int bootsIndex = helperSource.indexOf("if (result.isOf(ModItems.CELESTIUM_BOOTS))");
        int swordIndex = helperSource.indexOf("if (result.isOf(ModItems.CELESTIUM_SWORD))");

        assertTrue(
                "Trim cleanup should happen before celestium boots receive Feather Falling V",
                trimRemovalIndex >= 0 && bootsIndex > trimRemovalIndex
        );
        assertTrue(
                "Trim cleanup should happen before celestium swords receive Sharpness X",
                trimRemovalIndex >= 0 && swordIndex > trimRemovalIndex
        );
        assertContains(
                "Celestium boots should still gain Feather Falling V",
                helperSource,
                "builder.set(featherFalling, 5)"
        );
        assertContains(
                "Celestium swords should still gain Sharpness X",
                helperSource,
                "builder.set(sharpness, 10)"
        );
    }

    private static void assertNonCelestiumResultsRemainUnchanged(String helperSource) {
        assertContains(
                "Non-celestium smithing results should still return early without post-processing",
                helperSource,
                "if (!isCelestiumSmithingResult(result)) {"
        );
        assertContains(
                "Non-celestium smithing results should still be returned unchanged",
                helperSource,
                "return result;"
        );
    }

    private static void assertSmithingMixinStillUsesPostProcessing(String helperSource, String smithingMixinSource) {
        assertContains(
                "The smithing screen mixin should still delegate to the celestium smithing result helper",
                smithingMixinSource,
                "CelestiumSmithingResultHelper.postProcess(result, this.world.getRegistryManager())"
        );
        assertContains(
                "The smithing result helper should still know how to post-process celestium smithing outputs",
                helperSource,
                "private static boolean isCelestiumSmithingResult(ItemStack stack)"
        );
    }

    private static void assertContains(String scenario, String contents, String expectedSnippet) {
        if (!contents.contains(expectedSnippet)) {
            throw new AssertionError(scenario + " Missing snippet: " + expectedSnippet);
        }
    }

    private static void assertTrue(String scenario, boolean condition) {
        if (!condition) {
            throw new AssertionError(scenario);
        }
    }
}
