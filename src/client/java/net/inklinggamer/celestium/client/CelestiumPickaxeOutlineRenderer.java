package net.inklinggamer.celestium.client;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

import java.util.Collection;

public final class CelestiumPickaxeOutlineRenderer {
    private static final float OUTLINE_OFFSET = 0.002F;
    private static final float LINE_WIDTH = 2.5F;
    private static final int RED = 120;
    private static final int GREEN = 255;
    private static final int BLUE = 180;
    private static final RenderLayer LAYER = CelestiumRenderLayers.lines("celestium_pickaxe_outline", LINE_WIDTH, false);

    private CelestiumPickaxeOutlineRenderer() {
    }

    public static synchronized void render(WorldRenderContext context, Collection<BlockPos> positions) {
        if (positions.isEmpty()) {
            return;
        }

        MatrixStack matrices = context.matrixStack();
        if (matrices == null || context.consumers() == null) {
            return;
        }

        Vec3d cameraPos = context.camera().getPos();
        VertexConsumer consumer = context.consumers().getBuffer(LAYER);

        matrices.push();
        matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        for (BlockPos pos : positions) {
            drawBox(matrices, consumer, pos);
        }
        matrices.pop();
    }

    private static void drawBox(MatrixStack matrices, VertexConsumer consumer, BlockPos pos) {
        float minX = pos.getX() - OUTLINE_OFFSET;
        float minY = pos.getY() - OUTLINE_OFFSET;
        float minZ = pos.getZ() - OUTLINE_OFFSET;
        float maxX = pos.getX() + 1.0F + OUTLINE_OFFSET;
        float maxY = pos.getY() + 1.0F + OUTLINE_OFFSET;
        float maxZ = pos.getZ() + 1.0F + OUTLINE_OFFSET;

        line(matrices, consumer, minX, minY, minZ, maxX, minY, minZ);
        line(matrices, consumer, maxX, minY, minZ, maxX, minY, maxZ);
        line(matrices, consumer, maxX, minY, maxZ, minX, minY, maxZ);
        line(matrices, consumer, minX, minY, maxZ, minX, minY, minZ);

        line(matrices, consumer, minX, maxY, minZ, maxX, maxY, minZ);
        line(matrices, consumer, maxX, maxY, minZ, maxX, maxY, maxZ);
        line(matrices, consumer, maxX, maxY, maxZ, minX, maxY, maxZ);
        line(matrices, consumer, minX, maxY, maxZ, minX, maxY, minZ);

        line(matrices, consumer, minX, minY, minZ, minX, maxY, minZ);
        line(matrices, consumer, maxX, minY, minZ, maxX, maxY, minZ);
        line(matrices, consumer, maxX, minY, maxZ, maxX, maxY, maxZ);
        line(matrices, consumer, minX, minY, maxZ, minX, maxY, maxZ);
    }

    private static void line(MatrixStack matrices, VertexConsumer consumer, float startX, float startY, float startZ, float endX, float endY, float endZ) {
        Vector3f direction = new Vector3f(endX - startX, endY - startY, endZ - startZ).normalize();
        MatrixStack.Entry entry = matrices.peek();

        consumer.vertex(entry, startX, startY, startZ)
                .color(RED, GREEN, BLUE, 255)
                .normal(entry, direction.x(), direction.y(), direction.z());
        consumer.vertex(entry, endX, endY, endZ)
                .color(RED, GREEN, BLUE, 255)
                .normal(entry, direction.x(), direction.y(), direction.z());
    }
}
