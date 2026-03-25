package net.inklinggamer.shopsandtools.player;

public final class CelestiumExperienceManagerTest {
    private static final double XP_REMAINDER_EPSILON = 1.0E-6D;

    private CelestiumExperienceManagerTest() {
    }

    public static void main(String[] args) {
        assertAdjustedExperience("No held Celestium item leaves XP unchanged", 5, 0, 0, 0, 0.0D, 5, 0.0D);
        assertAdjustedExperience("A single 5 XP orb gives the sword its full 20% bonus", 5, 1, 0, 0, 0.0D, 6, 0.0D);
        assertAdjustedExperience("A single 5 XP orb gives the pickaxe its 50% bonus with carried remainder", 5, 0, 1, 0, 0.0D, 7, 0.5D);
        assertAdjustedExperience("A single 5 XP orb gives the axe its 50% bonus with carried remainder", 5, 0, 0, 1, 0.0D, 7, 0.5D);
        assertAdjustedExperience("Sword and pickaxe stack additively on a single 5 XP orb", 5, 1, 1, 0, 0.0D, 8, 0.5D);
        assertAdjustedExperience("Sword and axe stack additively on a single 5 XP orb", 5, 1, 0, 1, 0.0D, 8, 0.5D);
        assertAdjustedExperience("Pickaxe and axe stack additively on a single 5 XP orb", 5, 0, 1, 1, 0.0D, 10, 0.0D);
        assertAdjustedExperience("Sword, pickaxe, and axe stack additively on a single 5 XP orb", 5, 1, 1, 1, 0.0D, 11, 0.0D);
        assertAdjustedExperience("Fractional XP is preserved in remainder instead of being lost", 1, 0, 1, 0, 0.0D, 1, 0.5D);
        assertAdjustedExperience("Duplicate swords do not stack twice", 5, 2, 0, 0, 0.0D, 6, 0.0D);
        assertAdjustedExperience("Duplicate pickaxes do not stack twice", 5, 0, 2, 0, 0.0D, 7, 0.5D);
        assertAdjustedExperience("Duplicate axes do not stack twice", 5, 0, 0, 2, 0.0D, 7, 0.5D);
        assertSplitOrbSequence("A 5 XP zombie split into 3,1,1 still gives the sword its full bonus", new int[]{3, 1, 1}, 1, 0, 0, 6, 0.0D);
        assertSplitOrbSequence("A 5 XP zombie split into 3,1,1 still gives the pickaxe its expected total", new int[]{3, 1, 1}, 0, 1, 0, 7, 0.5D);
        assertSplitOrbSequence("A 5 XP zombie split into 3,1,1 still gives the axe its expected total", new int[]{3, 1, 1}, 0, 0, 1, 7, 0.5D);
        assertSplitOrbSequence("A 5 XP zombie split into 3,1,1 still gives the stacked sword, pickaxe, and axe total", new int[]{3, 1, 1}, 1, 1, 1, 11, 0.0D);
    }

    private static void assertAdjustedExperience(String scenario, int baseExperience, int swordCopies, int pickaxeCopies, int axeCopies, double carriedRemainder, int expectedExperience, double expectedRemainder) {
        CelestiumExperienceManager.XpAdjustment adjustment =
                CelestiumExperienceManager.adjustExperience(baseExperience, swordCopies, pickaxeCopies, axeCopies, carriedRemainder);

        if (adjustment.adjustedExperience() != expectedExperience) {
            throw new AssertionError(
                    scenario + " expected " + expectedExperience + " XP but got " + adjustment.adjustedExperience() + " XP"
            );
        }

        assertRemainder(scenario, adjustment.remainder(), expectedRemainder);
    }

    private static void assertSplitOrbSequence(String scenario, int[] orbValues, int swordCopies, int pickaxeCopies, int axeCopies, int expectedTotalXp, double expectedFinalRemainder) {
        double remainder = 0.0D;
        int totalXp = 0;
        for (int orbValue : orbValues) {
            CelestiumExperienceManager.XpAdjustment adjustment =
                    CelestiumExperienceManager.adjustExperience(orbValue, swordCopies, pickaxeCopies, axeCopies, remainder);
            totalXp += adjustment.adjustedExperience();
            remainder = adjustment.remainder();
        }

        if (totalXp != expectedTotalXp) {
            throw new AssertionError(
                    scenario + " expected " + expectedTotalXp + " total XP but got " + totalXp + " XP"
            );
        }

        assertRemainder(scenario, remainder, expectedFinalRemainder);
    }

    private static void assertRemainder(String scenario, double actualRemainder, double expectedRemainder) {
        if (Math.abs(actualRemainder - expectedRemainder) > XP_REMAINDER_EPSILON) {
            throw new AssertionError(
                    scenario + " expected remainder " + expectedRemainder + " but got " + actualRemainder
            );
        }
    }
}
