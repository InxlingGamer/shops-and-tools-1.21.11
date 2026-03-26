package net.inklinggamer.shopsandtools.item;

import org.joml.Vector3f;

public final class CelestiumItemTest {
    private CelestiumItemTest() {
    }

    public static void main(String[] args) {
        assertHeldLightMatchesPearlescentFroglight();
    }

    private static void assertHeldLightMatchesPearlescentFroglight() {
        int expectedPearlescentEmission = 15;
        Vector3f expectedColor = new Vector3f(1.1F, 0.5F, 0.9F);

        assertTrue("Pearlescent Froglight light emission should stay positive", expectedPearlescentEmission > 0);
        assertEquals("Celestium held-light emission should match Pearlescent Froglight light emission", expectedPearlescentEmission, CelestiumHeldLight.getPearlescentFroglightLightEmission());
        assertVectorEquals("Celestium held-light color should match Pearlescent Froglight pink", expectedColor, CelestiumHeldLight.createPearlescentFroglightColor());
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

    private static void assertVectorEquals(String scenario, Vector3f expected, Vector3f actual) {
        if (Float.compare(expected.x, actual.x) != 0
                || Float.compare(expected.y, actual.y) != 0
                || Float.compare(expected.z, actual.z) != 0) {
            throw new AssertionError(
                    scenario + " Expected (" + expected.x + ", " + expected.y + ", " + expected.z + ") but got ("
                            + actual.x + ", " + actual.y + ", " + actual.z + ")."
            );
        }
    }
}
