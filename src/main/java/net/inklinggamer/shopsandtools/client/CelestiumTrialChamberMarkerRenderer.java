package net.inklinggamer.shopsandtools.client;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.inklinggamer.shopsandtools.ShopsAndTools;
import net.inklinggamer.shopsandtools.mixin.client.RenderLayerInvoker;
import net.inklinggamer.shopsandtools.mixin.client.RenderPipelinesAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public final class CelestiumTrialChamberMarkerRenderer {
    private static final float RADIUS = 0.325F;
    private static final float LINE_WIDTH = 3.5F;
    private static final int RED = 255;
    private static final int GREEN = 100;
    private static final int BLUE = 200;
    private static RenderPipeline pipeline;
    private static RenderType layer;
    private static boolean rendererDisabled;

    private CelestiumTrialChamberMarkerRenderer() {
    }

    public static synchronized void render(WorldRenderContext context, BlockPos pos) {
        if (pos == null || !ensureInitialized()) {
            return;
        }

        PoseStack matrices = context.matrices();
        if (matrices == null || context.consumers() == null) {
            return;
        }

        Minecraft client = Minecraft.getInstance();
        if (client.level == null) {
            return;
        }

        float minY = client.level.getMinY();
        float maxY = client.level.getMaxY() + 1.0F;
        float centerX = pos.getX() + 0.5F;
        float centerZ = pos.getZ() + 0.5F;
        Vec3 cameraPos = client.gameRenderer.getMainCamera().position();
        VertexConsumer consumer = context.consumers().getBuffer(layer);

        matrices.pushPose();
        matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        drawBeam(matrices, consumer, centerX, minY, centerZ, maxY);
        matrices.popPose();
    }

    static synchronized RenderPipeline getOrCreatePipeline() {
        if (rendererDisabled) {
            return null;
        }

        if (pipeline != null) {
            return pipeline;
        }

        try {
            pipeline = RenderPipeline.builder(RenderPipelinesAccessor.shopsandtools$getLineSnippet())
                    .withLocation(Identifier.fromNamespaceAndPath(ShopsAndTools.MOD_ID, "celestium_trial_chamber_marker"))
                    .withVertexShader("core/rendertype_lines")
                    .withFragmentShader("core/rendertype_lines")
                    .withBlend(BlendFunction.TRANSLUCENT)
                    .withCull(false)
                    .withVertexFormat(DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH, VertexFormat.Mode.LINES)
                    .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
                    .build();
            return pipeline;
        } catch (RuntimeException exception) {
            rendererDisabled = true;
            ShopsAndTools.LOGGER.error("Failed to create the Celestium trial chamber marker pipeline; disabling it for this session", exception);
            return null;
        }
    }

    private static boolean ensureInitialized() {
        if (rendererDisabled) {
            return false;
        }

        if (layer != null) {
            return true;
        }

        try {
            pipeline = getOrCreatePipeline();
            if (pipeline == null) {
                return false;
            }

            layer = RenderLayerInvoker.shopsandtools$create(
                    "shopsandtools_celestium_trial_chamber_marker",
                    RenderSetup.builder(pipeline).sortOnUpload().bufferSize(4096).createRenderSetup()
            );
            return true;
        } catch (RuntimeException exception) {
            rendererDisabled = true;
            ShopsAndTools.LOGGER.error("Failed to initialize the Celestium trial chamber marker renderer; disabling it for this session", exception);
            return false;
        }
    }

    private static void drawBeam(PoseStack matrices, VertexConsumer consumer, float centerX, float minY, float centerZ, float maxY) {
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

    private static void line(PoseStack matrices, VertexConsumer consumer, float startX, float startY, float startZ, float endX, float endY, float endZ) {
        Vector3f direction = new Vector3f(endX - startX, endY - startY, endZ - startZ).normalize();
        PoseStack.Pose entry = matrices.last();

        consumer.addVertex(entry, startX, startY, startZ)
                .setColor(RED, GREEN, BLUE, 255)
                .setNormal(entry, direction)
                .setLineWidth(LINE_WIDTH);
        consumer.addVertex(entry, endX, endY, endZ)
                .setColor(RED, GREEN, BLUE, 255)
                .setNormal(entry, direction)
                .setLineWidth(LINE_WIDTH);
    }
}
