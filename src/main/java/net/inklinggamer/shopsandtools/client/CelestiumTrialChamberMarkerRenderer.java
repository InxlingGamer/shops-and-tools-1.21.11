package net.inklinggamer.shopsandtools.client;

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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

public final class CelestiumTrialChamberMarkerRenderer {
    private static final float RADIUS = 0.325F;
    private static final float LINE_WIDTH = 3.5F;
    private static final int RED = 255;
    private static final int GREEN = 100;
    private static final int BLUE = 200;
    private static RenderPipeline pipeline;
    private static RenderLayer layer;
    private static boolean rendererDisabled;

    private CelestiumTrialChamberMarkerRenderer() {
    }

    public static synchronized void render(WorldRenderContext context, BlockPos pos) {
        if (pos == null || !ensureInitialized()) {
            return;
        }

        MatrixStack matrices = context.matrices();
        if (matrices == null || context.consumers() == null) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) {
            return;
        }

        float minY = client.world.getBottomY();
        float maxY = client.world.getTopYInclusive() + 1.0F;
        float centerX = pos.getX() + 0.5F;
        float centerZ = pos.getZ() + 0.5F;
        Vec3d cameraPos = client.gameRenderer.getCamera().getCameraPos();
        VertexConsumer consumer = context.consumers().getBuffer(layer);

        matrices.push();
        matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        drawBeam(matrices, consumer, centerX, minY, centerZ, maxY);
        matrices.pop();
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
                    .withLocation(Identifier.of(ShopsAndTools.MOD_ID, "celestium_trial_chamber_marker"))
                    .withVertexShader("core/rendertype_lines")
                    .withFragmentShader("core/rendertype_lines")
                    .withBlend(BlendFunction.TRANSLUCENT)
                    .withCull(false)
                    .withVertexFormat(VertexFormats.POSITION_COLOR_NORMAL_LINE_WIDTH, VertexFormat.DrawMode.LINES)
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
                    RenderSetup.builder(pipeline).translucent().expectedBufferSize(4096).build()
            );
            return true;
        } catch (RuntimeException exception) {
            rendererDisabled = true;
            ShopsAndTools.LOGGER.error("Failed to initialize the Celestium trial chamber marker renderer; disabling it for this session", exception);
            return false;
        }
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
                .normal(entry, direction)
                .lineWidth(LINE_WIDTH);
        consumer.vertex(entry, endX, endY, endZ)
                .color(RED, GREEN, BLUE, 255)
                .normal(entry, direction)
                .lineWidth(LINE_WIDTH);
    }
}
