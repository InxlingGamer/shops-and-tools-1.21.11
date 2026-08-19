package net.inklinggamer.celestium.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.Collection;
import java.util.List;

public final class CelestiumPickaxeOutlineRenderer {
    private static final float OUTLINE_OFFSET = 0.002F;
    private static final float LINE_WIDTH = 2.5F;
    private static final int RED = 120;
    private static final int GREEN = 255;
    private static final int BLUE = 180;

    private CelestiumPickaxeOutlineRenderer() {
    }

    public static void render(LevelRenderContext context, Collection<BlockPos> positions) {
        if (positions.isEmpty()) {
            return;
        }

        PoseStack poseStack = context.poseStack();
        Vec3 cameraPos = context.levelState().cameraRenderState.pos;
        List<BlockPos> snapshot = List.copyOf(positions);

        poseStack.pushPose();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        context.submitNodeCollector().submitCustomGeometry(poseStack, RenderTypes.linesTranslucent(), (pose, consumer) -> {
            for (BlockPos pos : snapshot) {
                drawBox(pose, consumer, pos);
            }
        });
        poseStack.popPose();
    }

    private static void drawBox(PoseStack.Pose pose, VertexConsumer consumer, BlockPos pos) {
        float minX = pos.getX() - OUTLINE_OFFSET;
        float minY = pos.getY() - OUTLINE_OFFSET;
        float minZ = pos.getZ() - OUTLINE_OFFSET;
        float maxX = pos.getX() + 1.0F + OUTLINE_OFFSET;
        float maxY = pos.getY() + 1.0F + OUTLINE_OFFSET;
        float maxZ = pos.getZ() + 1.0F + OUTLINE_OFFSET;

        line(pose, consumer, minX, minY, minZ, maxX, minY, minZ);
        line(pose, consumer, maxX, minY, minZ, maxX, minY, maxZ);
        line(pose, consumer, maxX, minY, maxZ, minX, minY, maxZ);
        line(pose, consumer, minX, minY, maxZ, minX, minY, minZ);
        line(pose, consumer, minX, maxY, minZ, maxX, maxY, minZ);
        line(pose, consumer, maxX, maxY, minZ, maxX, maxY, maxZ);
        line(pose, consumer, maxX, maxY, maxZ, minX, maxY, maxZ);
        line(pose, consumer, minX, maxY, maxZ, minX, maxY, minZ);
        line(pose, consumer, minX, minY, minZ, minX, maxY, minZ);
        line(pose, consumer, maxX, minY, minZ, maxX, maxY, minZ);
        line(pose, consumer, maxX, minY, maxZ, maxX, maxY, maxZ);
        line(pose, consumer, minX, minY, maxZ, minX, maxY, maxZ);
    }

    private static void line(PoseStack.Pose pose, VertexConsumer consumer, float startX, float startY, float startZ, float endX, float endY, float endZ) {
        Vector3f direction = new Vector3f(endX - startX, endY - startY, endZ - startZ).normalize();
        consumer.addVertex(pose, startX, startY, startZ).setColor(RED, GREEN, BLUE, 255).setNormal(pose, direction).setLineWidth(LINE_WIDTH);
        consumer.addVertex(pose, endX, endY, endZ).setColor(RED, GREEN, BLUE, 255).setNormal(pose, direction).setLineWidth(LINE_WIDTH);
    }
}
