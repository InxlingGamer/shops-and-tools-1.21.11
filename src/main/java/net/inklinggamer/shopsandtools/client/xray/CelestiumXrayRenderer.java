package net.inklinggamer.shopsandtools.client.xray;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.inklinggamer.shopsandtools.ShopsAndTools;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderSetup;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;

public final class CelestiumXrayRenderer {
    private static final float LINE_WIDTH = 2.0f;
    private static final RenderPipeline XRAY_PIPELINE = createXrayPipeline();
    private static final RenderLayer XRAY_LAYER = createXrayLayer();

    private CelestiumXrayRenderer() {
    }

    public static void render(WorldRenderContext context, Collection<OreOutlineEntry> entries) {
        if (entries.isEmpty()) {
            return;
        }

        MatrixStack matrices = context.matrices();
        VertexConsumer consumer = context.consumers().getBuffer(XRAY_LAYER);
        Vec3d cameraPos = MinecraftClient.getInstance().gameRenderer.getCamera().getCameraPos();

        matrices.push();
        matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        for (OreOutlineEntry entry : entries) {
            drawBox(matrices, consumer, entry);
        }

        matrices.pop();
    }

    public static void clear() {
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

    private static RenderPipeline createXrayPipeline() {
        RenderPipeline.Snippet lineSnippet = getLineSnippet();

        return RenderPipeline.builder(lineSnippet)
                .withLocation(Identifier.of(ShopsAndTools.MOD_ID, "celestium_xray_lines"))
                .withVertexShader("core/rendertype_lines")
                .withFragmentShader("core/rendertype_lines")
                .withBlend(BlendFunction.TRANSLUCENT)
                .withCull(false)
                .withVertexFormat(VertexFormats.POSITION_COLOR_NORMAL_LINE_WIDTH, VertexFormat.DrawMode.LINES)
                .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                .build();
    }

    private static RenderPipeline.Snippet getLineSnippet() {
        try {
            Field snippetField = RenderPipelines.class.getDeclaredField("RENDERTYPE_LINES_SNIPPET");
            snippetField.setAccessible(true);
            return (RenderPipeline.Snippet) snippetField.get(null);
        } catch (NoSuchFieldException | IllegalAccessException exception) {
            throw new IllegalStateException("Unable to access the line render snippet", exception);
        }
    }

    private static RenderLayer createXrayLayer() {
        RenderSetup renderSetup = RenderSetup.builder(XRAY_PIPELINE)
                .translucent()
                .expectedBufferSize(4096)
                .build();

        try {
            Method factory = RenderLayer.class.getDeclaredMethod("of", String.class, RenderSetup.class);
            factory.setAccessible(true);
            return (RenderLayer) factory.invoke(null, "shopsandtools_celestium_xray", renderSetup);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException exception) {
            throw new IllegalStateException("Unable to create Celestium xray render layer", exception);
        }
    }
}
