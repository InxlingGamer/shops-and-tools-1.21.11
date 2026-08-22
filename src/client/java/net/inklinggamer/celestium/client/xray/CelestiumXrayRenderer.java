package net.inklinggamer.celestium.client.xray;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.inklinggamer.celestium.client.CelestiumRenderLayers;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

import java.util.Collection;

public final class CelestiumXrayRenderer {
    private static final float LINE_WIDTH = 2.0f;
    private static final RenderLayer XRAY_LAYER = CelestiumRenderLayers.lines("celestium_xray", LINE_WIDTH, true);

    private CelestiumXrayRenderer() {
    }

    public static synchronized void render(WorldRenderContext context, Collection<OreOutlineEntry> entries) {
        if (entries.isEmpty()) {
            return;
        }

        MatrixStack matrices = context.matrixStack();
        if (matrices == null || context.consumers() == null) {
            return;
        }

        renderBuffered(matrices, context.consumers().getBuffer(XRAY_LAYER), entries);
    }

    public static synchronized void markDirty() {
        // Buffered rendering draws directly from the current scan results,
        // so there is no cached GPU state to invalidate here.
    }

    private static void renderBuffered(MatrixStack matrices, VertexConsumer consumer, Collection<OreOutlineEntry> entries) {
        Vec3d cameraPos = MinecraftClient.getInstance().gameRenderer.getCamera().getPos();

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
                .normal(entry, direction.x(), direction.y(), direction.z());
        consumer.vertex(entry, endX, endY, endZ)
                .color(red, green, blue, 255)
                .normal(entry, direction.x(), direction.y(), direction.z());
    }
}
