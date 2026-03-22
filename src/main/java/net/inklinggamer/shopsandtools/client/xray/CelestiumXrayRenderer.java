package net.inklinggamer.shopsandtools.client.xray;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.inklinggamer.shopsandtools.ShopsAndTools;
import net.inklinggamer.shopsandtools.mixin.client.RenderLayerInvoker;
import net.inklinggamer.shopsandtools.mixin.client.RenderPipelinesAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderSetup;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

import java.util.Collection;

public final class CelestiumXrayRenderer {
    private static final float LINE_WIDTH = 2.0f;
    private static RenderPipeline xrayPipeline;
    private static RenderLayer xrayLayer;
    private static boolean rendererDisabled;

    private CelestiumXrayRenderer() {
    }

    public static synchronized void render(WorldRenderContext context, Collection<OreOutlineEntry> entries) {
        if (entries.isEmpty() || !ensureInitialized()) {
            return;
        }

        MatrixStack matrices = context.matrices();
        if (matrices == null || context.consumers() == null) {
            return;
        }

        renderBuffered(matrices, context.consumers().getBuffer(xrayLayer), entries);
    }

    public static synchronized void markDirty() {
        // Buffered rendering draws directly from the current scan results,
        // so there is no cached GPU state to invalidate here.
    }

    private static synchronized boolean ensureInitialized() {
        if (rendererDisabled) {
            return false;
        }

        if (xrayLayer != null) {
            return true;
        }

        try {
            if (xrayPipeline == null) {
                xrayPipeline = createPipeline();
            }
            xrayLayer = createLayer();
            return true;
        } catch (RuntimeException exception) {
            rendererDisabled = true;
            ShopsAndTools.LOGGER.error("Failed to initialize Celestium xray renderer; disabling xray rendering for this session", exception);
            return false;
        }
    }

    static synchronized RenderPipeline getOrCreatePipeline() {
        if (rendererDisabled) {
            return null;
        }

        if (xrayPipeline != null) {
            return xrayPipeline;
        }

        try {
            xrayPipeline = createPipeline();
            return xrayPipeline;
        } catch (RuntimeException exception) {
            rendererDisabled = true;
            ShopsAndTools.LOGGER.error("Failed to create the Celestium xray render pipeline; disabling xray rendering for this session", exception);
            return null;
        }
    }

    private static void renderBuffered(MatrixStack matrices, VertexConsumer consumer, Collection<OreOutlineEntry> entries) {
        Vec3d cameraPos = MinecraftClient.getInstance().gameRenderer.getCamera().getCameraPos();

        matrices.push();
        matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        for (OreOutlineEntry entry : entries) {
            drawBox(matrices, consumer, entry);
        }

        matrices.pop();
    }

    public static synchronized void clear() {
        // Buffered rendering does not keep per-world GPU buffers to release.
    }

    private static void drawBox(MatrixStack matrices, VertexConsumer consumer, OreOutlineEntry entry) {
        float minX = entry.pos().getX();
        float minY = entry.pos().getY();
        float minZ = entry.pos().getZ();
        float maxX = minX + 1.0f;
        float maxY = minY + 1.0f;
        float maxZ = minZ + 1.0f;

        int red = entry.color().red();
        int green = entry.color().green();
        int blue = entry.color().blue();

        line(matrices, consumer, minX, minY, minZ, maxX, minY, minZ, red, green, blue);
        line(matrices, consumer, maxX, minY, minZ, maxX, minY, maxZ, red, green, blue);
        line(matrices, consumer, maxX, minY, maxZ, minX, minY, maxZ, red, green, blue);
        line(matrices, consumer, minX, minY, maxZ, minX, minY, minZ, red, green, blue);

        line(matrices, consumer, minX, maxY, minZ, maxX, maxY, minZ, red, green, blue);
        line(matrices, consumer, maxX, maxY, minZ, maxX, maxY, maxZ, red, green, blue);
        line(matrices, consumer, maxX, maxY, maxZ, minX, maxY, maxZ, red, green, blue);
        line(matrices, consumer, minX, maxY, maxZ, minX, maxY, minZ, red, green, blue);

        line(matrices, consumer, minX, minY, minZ, minX, maxY, minZ, red, green, blue);
        line(matrices, consumer, maxX, minY, minZ, maxX, maxY, minZ, red, green, blue);
        line(matrices, consumer, maxX, minY, maxZ, maxX, maxY, maxZ, red, green, blue);
        line(matrices, consumer, minX, minY, maxZ, minX, maxY, maxZ, red, green, blue);
    }

    private static void line(
            MatrixStack matrices,
            VertexConsumer consumer,
            float startX,
            float startY,
            float startZ,
            float endX,
            float endY,
            float endZ,
            int red,
            int green,
            int blue
    ) {
        Vector3f direction = new Vector3f(endX - startX, endY - startY, endZ - startZ).normalize();
        MatrixStack.Entry entry = matrices.peek();

        consumer.vertex(entry, startX, startY, startZ)
                .color(red, green, blue, 255)
                .normal(entry, direction)
                .lineWidth(LINE_WIDTH);
        consumer.vertex(entry, endX, endY, endZ)
                .color(red, green, blue, 255)
                .normal(entry, direction)
                .lineWidth(LINE_WIDTH);
    }

    private static RenderPipeline createPipeline() {
        return RenderPipeline.builder(RenderPipelinesAccessor.shopsandtools$getLineSnippet())
                .withLocation(Identifier.of(ShopsAndTools.MOD_ID, "celestium_xray_lines"))
                .withVertexShader("core/rendertype_lines")
                .withFragmentShader("core/rendertype_lines")
                .withBlend(BlendFunction.TRANSLUCENT)
                .withCull(false)
                .withVertexFormat(VertexFormats.POSITION_COLOR_NORMAL_LINE_WIDTH, VertexFormat.DrawMode.LINES)
                .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                .build();
    }

    private static RenderLayer createLayer() {
        RenderSetup renderSetup = RenderSetup.builder(xrayPipeline)
                .translucent()
                .expectedBufferSize(4096)
                .build();

        return RenderLayerInvoker.shopsandtools$create("shopsandtools_celestium_xray", renderSetup);
    }
}
