package net.inklinggamer.shopsandtools.player;

import net.minecraft.util.math.BlockPos;

public final class CelestiumBootsManagerTest {
    private static final BlockPos START_POS = new BlockPos(10, 64, 20);
    private static final BlockPos UP_POS = START_POS.up();
    private static final BlockPos STRAFE_POS = START_POS.east();

    private CelestiumBootsManagerTest() {
    }

    public static void main(String[] args) {
        assertTransition("Initial wall attachment should seed tracking without playing a sound", null, START_POS, false, START_POS);
        assertTransition("Remaining on the same wall block should not replay a sound", START_POS, START_POS, false, START_POS);
        assertTransition("Climbing up into the next wall block should play once", START_POS, UP_POS, true, UP_POS);
        assertTransition("Climbing back down into the previous wall block should play once", UP_POS, START_POS, true, START_POS);
        assertTransition("Strafing onto a neighboring wall block should play once", START_POS, STRAFE_POS, true, STRAFE_POS);
        assertTransition("A missing resolved wall block should keep the prior tracked block without playing", START_POS, null, false, START_POS);
        assertTransition("Reattaching after a reset should seed without replaying stale wall data", null, STRAFE_POS, false, STRAFE_POS);
    }

    private static void assertTransition(
            String scenario,
            BlockPos previousSoundPos,
            BlockPos currentSoundPos,
            boolean expectedShouldPlay,
            BlockPos expectedTrackedSoundPos
    ) {
        CelestiumBootsManager.WallClimbSoundTransition transition =
                CelestiumBootsManager.evaluateWallClimbSoundTransition(previousSoundPos, currentSoundPos);

        if (transition.shouldPlaySound() != expectedShouldPlay) {
            throw new AssertionError(
                    scenario + " expected shouldPlaySound=" + expectedShouldPlay + " but got " + transition.shouldPlaySound()
            );
        }

        if (expectedTrackedSoundPos == null ? transition.trackedSoundPos() != null : !expectedTrackedSoundPos.equals(transition.trackedSoundPos())) {
            throw new AssertionError(
                    scenario + " expected tracked sound pos " + expectedTrackedSoundPos + " but got " + transition.trackedSoundPos()
            );
        }
    }
}
