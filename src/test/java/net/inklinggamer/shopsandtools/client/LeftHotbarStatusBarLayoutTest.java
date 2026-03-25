package net.inklinggamer.shopsandtools.client;

public final class LeftHotbarStatusBarLayoutTest {
    private LeftHotbarStatusBarLayoutTest() {
    }

    public static void main(String[] args) {
        assertDefaultLayout();
        assertLeftOffhandLayout();
        assertLeftEdgeClamp();
    }

    private static void assertDefaultLayout() {
        LeftHotbarStatusBarLayout.Layout layout = LeftHotbarStatusBarLayout.resolve(500, 240, false);

        assertEquals("Default layout should use the full rage width", 74, layout.width());
        assertEquals("Default layout X should match the rage bar anchor", 81, layout.x());
        assertEquals("Default layout Y should match the rage bar vertical offset", 233, layout.y());
    }

    private static void assertLeftOffhandLayout() {
        LeftHotbarStatusBarLayout.Layout layout = LeftHotbarStatusBarLayout.resolve(500, 240, true);

        assertEquals("Offhand layout should shrink to the rage offhand width", 56, layout.width());
        assertEquals("Offhand layout X should move left of the offhand slot", 70, layout.x());
        assertEquals("Offhand layout Y should still match the rage bar vertical offset", 233, layout.y());
    }

    private static void assertLeftEdgeClamp() {
        LeftHotbarStatusBarLayout.Layout layout = LeftHotbarStatusBarLayout.resolve(250, 200, false);

        assertEquals("The layout should clamp to the left screen margin", 2, layout.x());
        assertEquals("The height should stay tied to the hotbar row", 193, layout.y());
    }

    private static void assertEquals(String scenario, int expected, int actual) {
        if (expected != actual) {
            throw new AssertionError(scenario + " expected " + expected + " but got " + actual);
        }
    }
}
