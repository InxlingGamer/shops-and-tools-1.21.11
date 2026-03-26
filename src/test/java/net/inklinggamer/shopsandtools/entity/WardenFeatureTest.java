package net.inklinggamer.shopsandtools.entity;

public final class WardenFeatureTest {
    private WardenFeatureTest() {
    }

    public static void main(String[] args) {
        assertExtraMaceDurabilityCalculation();
        assertDirectDurabilityPenaltyResolution();
        assertProjectileImmunityThresholds();
        assertBossBarRangeChecks();
        assertBossBarPercentCalculation();
        assertDeathGraceTicking();
    }

    private static void assertExtraMaceDurabilityCalculation() {
        assertEquals("Ten damage should add thirty extra mace durability loss", 30, WardenCombatManager.calculateExtraMaceDurability(10.0F));
        assertEquals("Fractional damage should round to the nearest durability loss", 23, WardenCombatManager.calculateExtraMaceDurability(7.5F));
        assertEquals("Negative damage should not create extra durability loss", 0, WardenCombatManager.calculateExtraMaceDurability(-2.0F));
    }

    private static void assertDirectDurabilityPenaltyResolution() {
        WardenCombatManager.DirectDurabilityResult regularPenalty =
                WardenCombatManager.resolveDirectDurabilityPenalty(12, 250, 30);
        assertFalse("Non-breaking bonus durability should keep the mace intact", regularPenalty.breaksItem());
        assertEquals("The direct durability helper should add the full penalty without reduction", 42, regularPenalty.resultingDamage());

        WardenCombatManager.DirectDurabilityResult breakingPenalty =
                WardenCombatManager.resolveDirectDurabilityPenalty(80, 100, 20);
        assertTrue("Crossing the max damage threshold should break the mace", breakingPenalty.breaksItem());
    }

    private static void assertProjectileImmunityThresholds() {
        assertFalse(
                "Wardens above seventy percent health should still take projectile damage",
                WardenCombatManager.shouldBlockProjectileDamage(100.0F, 100.0F, 5.0F, true)
        );
        assertFalse(
                "Wardens at exactly seventy percent health should still take projectile damage",
                WardenCombatManager.shouldBlockProjectileDamage(70.0F, 100.0F, 0.0F, true)
        );
        assertTrue(
                "Wardens at exactly seventy percent health should block projectile hits that would push them lower",
                WardenCombatManager.shouldBlockProjectileDamage(70.0F, 100.0F, 1.0F, true)
        );
        assertTrue(
                "Wardens below seventy percent health should ignore projectile damage",
                WardenCombatManager.shouldBlockProjectileDamage(69.9F, 100.0F, 1.0F, true)
        );
        assertTrue(
                "Projectile hits that would push the Warden below seventy percent health should be blocked",
                WardenCombatManager.shouldBlockProjectileDamage(75.0F, 100.0F, 6.0F, true)
        );
        assertFalse(
                "Non-projectile damage should never be blocked by the projectile immunity rule",
                WardenCombatManager.shouldBlockProjectileDamage(20.0F, 100.0F, 6.0F, false)
        );
    }

    private static void assertBossBarRangeChecks() {
        assertTrue("Players exactly one hundred blocks away should still see the boss bar", WardenBossBarManager.isWithinBossBarRange(10000.0D));
        assertFalse("Players beyond one hundred blocks should not see the boss bar", WardenBossBarManager.isWithinBossBarRange(10000.01D));
    }

    private static void assertBossBarPercentCalculation() {
        assertFloatEquals("A full-health Warden should show a full boss bar", 1.0F, WardenBossBarManager.getBossBarPercent(500.0F, 500.0F));
        assertFloatEquals("A dead Warden should show an empty boss bar", 0.0F, WardenBossBarManager.getBossBarPercent(0.0F, 500.0F));
        assertFloatEquals("Half health should map to half boss bar progress", 0.5F, WardenBossBarManager.getBossBarPercent(250.0F, 500.0F));
        assertFloatEquals("Invalid max health should clamp to zero progress", 0.0F, WardenBossBarManager.getBossBarPercent(50.0F, 0.0F));
    }

    private static void assertDeathGraceTicking() {
        int remainingTicks = 60;
        for (int tick = 0; tick < 59; tick++) {
            remainingTicks = WardenBossBarManager.tickDeathGrace(remainingTicks);
        }

        assertEquals("The boss bar should still have one tick of grace after fifty-nine ticks", 1, remainingTicks);
        assertEquals("The sixtieth grace tick should remove the boss bar", 0, WardenBossBarManager.tickDeathGrace(remainingTicks));
        assertEquals("Extra grace ticks should stay clamped at zero", 0, WardenBossBarManager.tickDeathGrace(0));
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

    private static void assertFloatEquals(String scenario, float expected, float actual) {
        if (Math.abs(expected - actual) > 0.0001F) {
            throw new AssertionError(scenario + " expected " + expected + " but got " + actual);
        }
    }
}
