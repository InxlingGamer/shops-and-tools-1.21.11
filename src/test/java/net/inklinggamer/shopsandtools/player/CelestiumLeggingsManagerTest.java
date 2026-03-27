package net.inklinggamer.shopsandtools.player;

public final class CelestiumLeggingsManagerTest {
    private CelestiumLeggingsManagerTest() {
    }

    public static void main(String[] args) {
        assertSurvivalFlightPermissionGrantAndRevoke();
        assertCreativePlayersKeepVanillaFlight();
        assertCreativeUnequipPreservesFlight();
        assertCreativeFlightToggleBypassesCelestiumInterception();
    }

    private static void assertSurvivalFlightPermissionGrantAndRevoke() {
        CelestiumLeggingsManager.FlightPermissionSync grant =
                CelestiumLeggingsManager.resolveFlightPermission(false, false, true);

        assertTrue("Eligible survival movement should grant Celestium flight permission", grant.shouldGrantPermission());
        assertFalse("Granting permission should not also revoke it", grant.shouldRevokePermission());
        assertFalse("Granting permission should not disable flight", grant.shouldDisableFlightOnRevoke());

        CelestiumLeggingsManager.FlightPermissionSync revoke =
                CelestiumLeggingsManager.resolveFlightPermission(true, false, false);

        assertFalse("Revoking permission should not also grant it", revoke.shouldGrantPermission());
        assertTrue("Leaving the eligible survival state should revoke Celestium flight permission", revoke.shouldRevokePermission());
        assertTrue("Survival revokes should disable the temporary flight permission", revoke.shouldDisableFlightOnRevoke());
    }

    private static void assertCreativePlayersKeepVanillaFlight() {
        assertTrue(
                "Creative mode should count as vanilla flight permission",
                CelestiumLeggingsManager.hasVanillaFlightPermission(true, false)
        );
        assertTrue(
                "Creative mode should restore allowFlying when it was cleared unexpectedly",
                CelestiumLeggingsManager.shouldRestoreVanillaAllowFlying(true, false)
        );
        assertFalse(
                "Creative mode should never let Celestium own the flight permission",
                CelestiumLeggingsManager.shouldManageCelestiumFlight(true, false)
        );

        CelestiumLeggingsManager.FlightPermissionSync sync =
                CelestiumLeggingsManager.resolveFlightPermission(false, true, false);

        assertFalse("Creative mode should not grant a Celestium-owned flight permission", sync.shouldGrantPermission());
        assertFalse("Creative mode with no active Celestium flight should not revoke anything", sync.shouldRevokePermission());
        assertFalse("Creative mode should not disable vanilla flight", sync.shouldDisableFlightOnRevoke());
    }

    private static void assertCreativeUnequipPreservesFlight() {
        CelestiumLeggingsManager.FlightPermissionSync sync =
                CelestiumLeggingsManager.resolveFlightPermission(true, true, false);

        assertFalse("Creative unequip should not re-grant Celestium flight", sync.shouldGrantPermission());
        assertTrue("Creative unequip should clear the stale Celestium flight state", sync.shouldRevokePermission());
        assertFalse("Creative unequip should keep vanilla allowFlying intact", sync.shouldDisableFlightOnRevoke());
        assertFalse(
                "Creative and spectator players should preserve vanilla flight when Celestium permission clears",
                CelestiumLeggingsManager.shouldDisableFlightOnPermissionClear(true)
        );
    }

    private static void assertCreativeFlightToggleBypassesCelestiumInterception() {
        assertTrue(
                "Active Celestium flight in survival should still intercept the jump packet",
                CelestiumLeggingsManager.shouldInterceptFlightToggle(true, false, false, true, true)
        );
        assertFalse(
                "Creative flight toggles should bypass Celestium interception and stay vanilla",
                CelestiumLeggingsManager.shouldInterceptFlightToggle(true, true, false, true, true)
        );
        assertFalse(
                "Spectator flight toggles should also bypass Celestium interception",
                CelestiumLeggingsManager.shouldInterceptFlightToggle(true, false, true, true, true)
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
