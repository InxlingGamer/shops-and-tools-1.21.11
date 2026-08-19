package net.inklinggamer.shopsandtools.client.xray;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.Collection;
import java.util.List;

public final class CelestiumXrayRenderer {
    private static final float LINE_WIDTH = 2.0F;

    private CelestiumXrayRenderer() {
    }

    public static void render(LevelRenderContext context, Collection<OreOutlineEntry> entries) {
        if (entries.isEmpty()) {
            return;
        }

        PoseStack poseStack = context.poseStack();
        Vec3 cameraPos = context.levelState().cameraRenderState.pos;
        List<OreOutlineEntry> snapshot = List.copyOf(entries);

        poseStack.pushPose();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        context.submitNodeCollector().submitCustomGeometry(poseStack, RenderTypes.linesTranslucent(), (pose, consumer) -> {
            for (OreOutlineEntry entry : snapshot) {
                drawBox(pose, consumer, entry);
            }
        });
        poseStack.popPose();
    }

    public static void markDirty() {
    }

    public static void clear() {
    }

    private static void drawBox(PoseStack.Pose pose, VertexConsumer consumer, OreOutlineEntry entry) {
        float minX = entry.pos().getX();
        float minY = entry.pos().getY();
        float minZ = entry.pos().getZ();
        float maxX = minX + 1.0F;
        float maxY = minY + 1.0F;
        float maxZ = minZ + 1.0F;
        int red = entry.color().red();
        int green = entry.color().green();
        int blue = entry.color().blue();

        line(pose, consumer, minX, minY, minZ, maxX, minY, minZ, red, green, blue);
        line(pose, consumer, maxX, minY, minZ, maxX, minY, maxZ, red, green, blue);
        line(pose, consumer, maxX, minY, maxZ, minX, minY, maxZ, red, green, blue);
        line(pose, consumer, minX, minY, maxZ, minX, minY, minZ, red, green, blue);
        line(pose, consumer, minX, maxY, minZ, maxX, maxY, minZ, red, green, blue);
        line(pose, consumer, maxX, maxY, minZ, maxX, maxY, maxZ, red, green, blue);
        line(pose, consumer, maxX, maxY, maxZ, minX, maxY, maxZ, red, green, blue);
        line(pose, consumer, minX, maxY, maxZ, minX, maxY, minZ, red, green, blue);
        line(pose, consumer, minX, minY, minZ, minX, maxY, minZ, red, green, blue);
        line(pose, consumer, maxX, minY, minZ, maxX, maxY, minZ, red, green, blue);
        line(pose, consumer, maxX, minY, maxZ, maxX, maxY, maxZ, red, green, blue);
        line(pose, consumer, minX, minY, maxZ, minX, maxY, maxZ, red, green, blue);
    }

    private static void line(PoseStack.Pose pose, VertexConsumer consumer, float startX, float startY, float startZ,
                             float endX, float endY, float endZ, int red, int green, int blue) {
        Vector3f direction = new Vector3f(endX - startX, endY - startY, endZ - startZ).normalize();
        consumer.addVertex(pose, startX, startY, startZ).setColor(red, green, blue, 255).setNormal(pose, direction).setLineWidth(LINE_WIDTH);
        consumer.addVertex(pose, endX, endY, endZ).setColor(red, green, blue, 255).setNormal(pose, direction).setLineWidth(LINE_WIDTH);
    }
}
