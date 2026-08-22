package net.inklinggamer.celestium.client;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

public final class CelestiumTrialChamberMarkerRenderer {
    private static final float RADIUS = 0.325F;
    private static final float LINE_WIDTH = 3.5F;
    private static final int RED = 255;
    private static final int GREEN = 100;
    private static final int BLUE = 200;
    private static final RenderLayer LAYER = CelestiumRenderLayers.lines("celestium_trial_chamber_marker", LINE_WIDTH, false);

    private CelestiumTrialChamberMarkerRenderer() {
    }

    public static synchronized void render(WorldRenderContext context, BlockPos pos) {
        if (pos == null) {
            return;
        }

        MatrixStack matrices = context.matrixStack();
        if (matrices == null || context.consumers() == null) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) {
            return;
        }

        float minY = client.world.getBottomY();
        float maxY = client.world.getTopY();
        float centerX = pos.getX() + 0.5F;
        float centerZ = pos.getZ() + 0.5F;
        Vec3d cameraPos = context.camera().getPos();
        VertexConsumer consumer = context.consumers().getBuffer(LAYER);

        matrices.push();
        matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        drawBeam(matrices, consumer, centerX, minY, centerZ, maxY);
        matrices.pop();
    }

    private static void drawBeam(MatrixStack matrices, VertexConsumer consumer, float centerX, float minY, float centerZ, float maxY) {
        float minX = centerX - RADIUS;
        float maxX = centerX + RADIUS;
        float minZ = centerZ - RADIUS;
        float maxZ = centerZ + RADIUS;

        line(matrices, consumer, minX, minY, minZ, minX, maxY, minZ);
        line(matrices, consumer, maxX, minY, minZ, maxX, maxY, minZ);
        line(matrices, consumer, maxX, minY, maxZ, maxX, maxY, maxZ);
        line(matrices, consumer, minX, minY, maxZ, minX, maxY, maxZ);

        line(matrices, consumer, minX, minY, minZ, maxX, minY, maxZ);
        line(matrices, consumer, maxX, minY, minZ, minX, minY, maxZ);
        line(matrices, consumer, minX, maxY, minZ, maxX, maxY, maxZ);
        line(matrices, consumer, maxX, maxY, minZ, minX, maxY, maxZ);
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
