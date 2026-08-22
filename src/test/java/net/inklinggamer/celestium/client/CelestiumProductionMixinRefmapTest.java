package net.inklinggamer.celestium.client;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class CelestiumProductionMixinRefmapTest {
    private static final String COMMON_CONFIG = "celestium.mixins.json";
    private static final String CLIENT_CONFIG = "celestium.client.mixins.json";
    private static final String COMMON_REFMAP = "celestium-1.21.1-refmap.json";
    private static final String CLIENT_REFMAP = "client-celestium-1.21.1-refmap.json";
    private static final List<String> CLIENT_MIXINS = List.of(
            "AnvilScreenMixin",
            "ClientPlayerInteractionManagerAccessor",
            "ClientPlayerInteractionManagerMixin",
            "ElytraFeatureRendererMixin",
            "HandledScreenAccessor",
            "InGameHudMixin",
            "LivingEntityClientMixin",
            "MinecraftClientMixin",
            "PlayerEntityClientMixin",
            "RenderLayerInvoker"
    );

    private CelestiumProductionMixinRefmapTest() {
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            throw new IllegalArgumentException("Expected the remapped production JAR path");
        }

        Path jarPath = Path.of(args[0]).toAbsolutePath().normalize();
        assertTrue("The remapped production JAR must exist", Files.isRegularFile(jarPath));

        try (ZipFile jar = new ZipFile(jarPath.toFile())) {
            String fabricMetadata = readRequired(jar, "fabric.mod.json");
            String commonConfig = readRequired(jar, COMMON_CONFIG);
            String clientConfig = readRequired(jar, CLIENT_CONFIG);
            String commonRefmap = readRequired(jar, COMMON_REFMAP);
            String clientRefmap = readRequired(jar, CLIENT_REFMAP);

            assertContains(
                    "Fabric metadata must register the client mixin configuration",
                    fabricMetadata,
                    "\"config\": \"" + CLIENT_CONFIG + "\"",
                    "\"environment\": \"client\""
            );
            assertContains(
                    "The common mixin configuration must use only the common refmap",
                    commonConfig,
                    "\"refmap\": \"" + COMMON_REFMAP + "\""
            );
            assertTrue(
                    "No client mixin may remain registered through the common configuration",
                    !commonConfig.contains("\"client\"")
                            && !commonConfig.contains("client.")
                            && !commonConfig.contains("ClientMixin")
                            && !commonConfig.contains("ElytraFeatureRendererMixin")
            );
            assertContains(
                    "The client mixin configuration must use only the split client refmap",
                    clientConfig,
                    "\"package\": \"net.inklinggamer.celestium.mixin.client\"",
                    "\"refmap\": \"" + CLIENT_REFMAP + "\""
            );

            for (String mixin : CLIENT_MIXINS) {
                assertContains("The client config must retain " + mixin, clientConfig, "\"" + mixin + "\"");
                assertContains(
                        "The client refmap must cover " + mixin,
                        clientRefmap,
                        "net/inklinggamer/celestium/mixin/client/" + mixin
                );
                assertTrue(
                        "The common refmap must not contain the client mixin " + mixin,
                        !commonRefmap.contains("net/inklinggamer/celestium/mixin/client/" + mixin)
                );
            }

            assertContains(
                    "MinecraftClientMixin#doItemUse must map to MinecraftClient.method_1583 on 1.21.1",
                    clientRefmap,
                    "\"doItemUse\": \"Lnet/minecraft/class_310;method_1583()V\""
            );
            assertContains(
                    "MinecraftClientMixin#doAttack must map to MinecraftClient.method_1536 on 1.21.1",
                    clientRefmap,
                    "\"doAttack\": \"Lnet/minecraft/class_310;method_1536()Z\""
            );
            assertContains(
                    "LivingEntityClientMixin#isHoldingOntoLadder must map to LivingEntity.method_21754 on 1.21.1",
                    clientRefmap,
                    "\"isHoldingOntoLadder\": \"Lnet/minecraft/class_1309;method_21754()Z\""
            );
            assertContains(
                    "AnvilScreenMixin#drawForeground must map to AnvilScreen.method_2388 on 1.21.1",
                    clientRefmap,
                    "\"drawForeground",
                    "Lnet/minecraft/class_471;method_2388"
            );
            assertContains(
                    "The fused elytra redirect must map ItemStack.isOf to method_31574 on 1.21.1",
                    clientRefmap,
                    "net/inklinggamer/celestium/mixin/client/ElytraFeatureRendererMixin",
                    "Lnet/minecraft/class_1799;method_31574(Lnet/minecraft/class_1792;)Z"
            );
            assertTrue(
                    "No armor-renderer suppression mixin may return in the production JAR",
                    !clientConfig.contains("ArmorFeatureRendererMixin")
                            && jar.getEntry("net/inklinggamer/celestium/mixin/client/ArmorFeatureRendererMixin.class") == null
            );
        }
    }

    private static String readRequired(ZipFile jar, String name) throws IOException {
        ZipEntry entry = jar.getEntry(name);
        assertTrue("Production JAR is missing " + name, entry != null);
        try (var input = jar.getInputStream(entry)) {
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static void assertContains(String scenario, String value, String... expectedValues) {
        for (String expected : expectedValues) {
            assertTrue(scenario + ": missing " + expected, value.contains(expected));
        }
    }

    private static void assertTrue(String scenario, boolean condition) {
        if (!condition) {
            throw new AssertionError(scenario);
        }
    }
}
