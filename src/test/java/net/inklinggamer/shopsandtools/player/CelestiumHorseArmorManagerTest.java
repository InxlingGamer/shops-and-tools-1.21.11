package net.inklinggamer.shopsandtools.player;

import net.inklinggamer.shopsandtools.item.CelestiumHorseArmorItem;

public final class CelestiumHorseArmorManagerTest {
    private CelestiumHorseArmorManagerTest() {
    }

    public static void main(String[] args) {
        assertArmorDetection();
        assertModifierAddRemoveIdempotence();
        assertFullHealthDetection();
        assertHealthClamp();
    }

    private static void assertArmorDetection() {
        assertTrue(
                "Celestium horse armor items should be recognized",
                CelestiumHorseArmorManager.isCelestiumHorseArmorClass(CelestiumHorseArmorItem.class)
        );
        assertFalse(
                "Non-celestium items should not be recognized as celestium horse armor",
                CelestiumHorseArmorManager.isCelestiumHorseArmorClass(String.class)
        );
    }

    private static void assertModifierAddRemoveIdempotence() {
        Double modifierValue = null;
        modifierValue = CelestiumHorseArmorManager.syncModifierValue(modifierValue, 24.0D);
        modifierValue = CelestiumHorseArmorManager.syncModifierValue(modifierValue, 24.0D);

        assertEquals("Syncing the same modifier twice should keep the same value", 24.0D, modifierValue);
        assertFalse("A matching modifier value should not need another sync", CelestiumHorseArmorManager.shouldSyncModifierValue(modifierValue, 24.0D));

        modifierValue = CelestiumHorseArmorManager.removeModifierValue(modifierValue);
        modifierValue = CelestiumHorseArmorManager.removeModifierValue(modifierValue);

        assertNull("Removing the modifier twice should still leave it absent", modifierValue);
    }

    private static void assertFullHealthDetection() {
        assertTrue("Exact max health should count as full health", CelestiumHorseArmorManager.wasAtFullHealth(30.0F, 30.0F));
        assertTrue("Values inside the health epsilon should still count as full health", CelestiumHorseArmorManager.wasAtFullHealth(29.9995F, 30.0F));
        assertFalse("Values below the health epsilon should not count as full health", CelestiumHorseArmorManager.wasAtFullHealth(29.0F, 30.0F));
    }

    private static void assertHealthClamp() {
        assertEquals("Health below the cap should remain unchanged", 12.0F, CelestiumHorseArmorManager.clampHealthToMax(12.0F, 20.0F));
        assertEquals("Health above the cap should clamp down to the new maximum", 20.0F, CelestiumHorseArmorManager.clampHealthToMax(28.0F, 20.0F));
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

    private static void assertEquals(String scenario, int expected, int actual) {
        if (expected != actual) {
            throw new AssertionError(scenario + " expected " + expected + " but got " + actual);
        }
    }

    private static void assertNull(String scenario, Object value) {
        if (value != null) {
            throw new AssertionError(scenario + " expected null but got " + value);
        }
    }

    private static void assertEquals(String scenario, double expected, double actual) {
        if (Math.abs(expected - actual) > 1.0E-6D) {
            throw new AssertionError(scenario + " expected " + expected + " but got " + actual);
        }
    }

    private static void assertEquals(String scenario, float expected, float actual) {
        if (Math.abs(expected - actual) > 1.0E-6F) {
            throw new AssertionError(scenario + " expected " + expected + " but got " + actual);
        }
    }
}
