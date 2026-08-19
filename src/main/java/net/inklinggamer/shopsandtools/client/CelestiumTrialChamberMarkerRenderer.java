package net.inklinggamer.shopsandtools.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public final class CelestiumTrialChamberMarkerRenderer {
    private static final float RADIUS = 0.325F;
    private static final float LINE_WIDTH = 3.5F;
    private static final int RED = 255;
    private static final int GREEN = 100;
    private static final int BLUE = 200;

    private CelestiumTrialChamberMarkerRenderer() {
    }

    public static void render(LevelRenderContext context, BlockPos pos) {
        Minecraft client = Minecraft.getInstance();
        if (pos == null || client.level == null) {
            return;
        }

        float minY = client.level.getMinY();
        float maxY = client.level.getMaxY() + 1.0F;
        float centerX = pos.getX() + 0.5F;
        float centerZ = pos.getZ() + 0.5F;
        Vec3 cameraPos = context.levelState().cameraRenderState.pos;
        PoseStack poseStack = context.poseStack();

        poseStack.pushPose();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        context.submitNodeCollector().submitCustomGeometry(poseStack, RenderTypes.linesTranslucent(),
                (pose, consumer) -> drawBeam(pose, consumer, centerX, minY, centerZ, maxY));
        poseStack.popPose();
    }

    private static void drawBeam(PoseStack.Pose pose, VertexConsumer consumer, float centerX, float minY, float centerZ, float maxY) {
        float minX = centerX - RADIUS;
        float maxX = centerX + RADIUS;
        float minZ = centerZ - RADIUS;
        float maxZ = centerZ + RADIUS;

        line(pose, consumer, minX, minY, minZ, minX, maxY, minZ);
        line(pose, consumer, maxX, minY, minZ, maxX, maxY, minZ);
        line(pose, consumer, maxX, minY, maxZ, maxX, maxY, maxZ);
        line(pose, consumer, minX, minY, maxZ, minX, maxY, maxZ);
        line(pose, consumer, minX, minY, minZ, maxX, minY, maxZ);
        line(pose, consumer, maxX, minY, minZ, minX, minY, maxZ);
        line(pose, consumer, minX, maxY, minZ, maxX, maxY, maxZ);
        line(pose, consumer, maxX, maxY, minZ, minX, maxY, maxZ);
    }

    private static void line(PoseStack.Pose pose, VertexConsumer consumer, float startX, float startY, float startZ, float endX, float endY, float endZ) {
        Vector3f direction = new Vector3f(endX - startX, endY - startY, endZ - startZ).normalize();
        consumer.addVertex(pose, startX, startY, startZ).setColor(RED, GREEN, BLUE, 255).setNormal(pose, direction).setLineWidth(LINE_WIDTH);
        consumer.addVertex(pose, endX, endY, endZ).setColor(RED, GREEN, BLUE, 255).setNormal(pose, direction).setLineWidth(LINE_WIDTH);
    }
}
