package net.inklinggamer.shopsandtools.player;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

public final class CelestiumBootsManagerTest {
    private static final double EPSILON = 1.0E-9D;
    private static final BlockPos START_POS = new BlockPos(10, 64, 20);
    private static final BlockPos UP_POS = START_POS.above();
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
        assertVerticalClimbMotion();
        assertSidewaysStrafeMotion();
        assertDetachResetMotion();
        assertWallFaceChangeRefreshesBasis();
        assertFrozenBasisIgnoresLaterYawChanges();
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

    private static void assertVerticalClimbMotion() {
        CelestiumBootsManager.AuthoritativeWallClimbMotion motion =
                CelestiumBootsManager.resolveAuthoritativeWallClimbMotion(false, null, null, Direction.EAST, 0.0F, 1, 0);

        assertMotion(
                "Forward climb on an east wall should only add stick and upward velocity",
                motion,
                true,
                Direction.EAST,
                new Vec3(0.0D, 0.0D, 1.0D),
                new Vec3(0.08D, 0.2D, 0.0D)
        );
    }

    private static void assertSidewaysStrafeMotion() {
        assertStrafe("Right strafe on east wall should move south", Direction.EAST, new Vec3(0.08D, 0.0D, 0.12D));
        assertStrafe("Left strafe on east wall should move north", Direction.EAST, new Vec3(0.08D, 0.0D, -0.12D), -1);
        assertStrafe("Right strafe on west wall should move north", Direction.WEST, new Vec3(-0.08D, 0.0D, -0.12D));
        assertStrafe("Left strafe on west wall should move south", Direction.WEST, new Vec3(-0.08D, 0.0D, 0.12D), -1);
        assertStrafe("Right strafe on north wall should move west", Direction.NORTH, new Vec3(-0.12D, 0.0D, -0.08D));
        assertStrafe("Left strafe on north wall should move east", Direction.NORTH, new Vec3(0.12D, 0.0D, -0.08D), -1);
        assertStrafe("Right strafe on south wall should move west", Direction.SOUTH, new Vec3(-0.12D, 0.0D, 0.08D));
        assertStrafe("Left strafe on south wall should move east", Direction.SOUTH, new Vec3(0.12D, 0.0D, 0.08D), -1);
    }

    private static void assertDetachResetMotion() {
        CelestiumBootsManager.AuthoritativeWallClimbMotion motion =
                CelestiumBootsManager.resolveAuthoritativeWallClimbMotion(
                        true,
                        Direction.EAST,
                        new Vec3(0.0D, 0.0D, 1.0D),
                        null,
                        0.0F,
                        0,
                        0
                );

        assertMotion(
                "Detaching from a wall should clear the authoritative state",
                motion,
                false,
                null,
                null,
                Vec3.ZERO
        );
    }

    private static void assertWallFaceChangeRefreshesBasis() {
        CelestiumBootsManager.AuthoritativeWallClimbMotion motion =
                CelestiumBootsManager.resolveAuthoritativeWallClimbMotion(
                        true,
                        Direction.EAST,
                        new Vec3(0.0D, 0.0D, 1.0D),
                        Direction.NORTH,
                        0.0F,
                        0,
                        1
                );

        assertMotion(
                "Switching wall faces should refresh the frozen strafe basis for the new wall",
                motion,
                true,
                Direction.NORTH,
                new Vec3(-1.0D, 0.0D, 0.0D),
                new Vec3(-0.12D, 0.0D, -0.08D)
        );
    }

    private static void assertFrozenBasisIgnoresLaterYawChanges() {
        CelestiumBootsManager.AuthoritativeWallClimbMotion initialMotion =
                CelestiumBootsManager.resolveAuthoritativeWallClimbMotion(false, null, null, Direction.EAST, 45.0F, 0, 1);
        CelestiumBootsManager.AuthoritativeWallClimbMotion continuedMotion =
                CelestiumBootsManager.resolveAuthoritativeWallClimbMotion(
                        true,
                        Direction.EAST,
                        initialMotion.wallStrafeBasis(),
                        Direction.EAST,
                        225.0F,
                        0,
                        1
                );

        assertMotion(
                "Continuing on the same wall should keep the original frozen strafe basis even if yaw changes",
                continuedMotion,
                true,
                Direction.EAST,
                new Vec3(0.0D, 0.0D, -1.0D),
                new Vec3(0.08D, 0.0D, -0.12D)
        );
    }

    private static void assertStrafe(String scenario, Direction wallDirection, Vec3 expectedVelocity) {
        assertStrafe(scenario, wallDirection, expectedVelocity, 1);
    }

    private static void assertStrafe(String scenario, Direction wallDirection, Vec3 expectedVelocity, int sidewaysInput) {
        CelestiumBootsManager.AuthoritativeWallClimbMotion motion =
                CelestiumBootsManager.resolveAuthoritativeWallClimbMotion(false, null, null, wallDirection, 0.0F, 0, sidewaysInput);

        assertMotion(
                scenario,
                motion,
                true,
                wallDirection,
                motion.wallStrafeBasis(),
                expectedVelocity
        );
    }

    private static void assertMotion(
            String scenario,
            CelestiumBootsManager.AuthoritativeWallClimbMotion motion,
            boolean expectedActive,
            Direction expectedWallDirection,
            Vec3 expectedStrafeBasis,
            Vec3 expectedVelocity
    ) {
        if (motion.active() != expectedActive) {
            throw new AssertionError(scenario + " expected active=" + expectedActive + " but got " + motion.active());
        }

        if (motion.wallDirection() != expectedWallDirection) {
            throw new AssertionError(scenario + " expected wall direction " + expectedWallDirection + " but got " + motion.wallDirection());
        }

        assertVecEquals(scenario + " should use the expected strafe basis", expectedStrafeBasis, motion.wallStrafeBasis());
        assertVecEquals(scenario + " should use the expected velocity", expectedVelocity, motion.velocity());
    }

    private static void assertVecEquals(String scenario, Vec3 expected, Vec3 actual) {
        if (expected == null) {
            if (actual != null) {
                throw new AssertionError(scenario + " expected null but got " + actual);
            }
            return;
        }

        if (actual == null
                || Math.abs(expected.x - actual.x) > EPSILON
                || Math.abs(expected.y - actual.y) > EPSILON
                || Math.abs(expected.z - actual.z) > EPSILON) {
            throw new AssertionError(scenario + " expected " + expected + " but got " + actual);
        }
    }
}
