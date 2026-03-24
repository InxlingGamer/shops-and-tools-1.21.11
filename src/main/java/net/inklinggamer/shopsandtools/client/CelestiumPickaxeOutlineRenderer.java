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

import java.util.Collection;

public final class CelestiumPickaxeOutlineRenderer {
    private static final float OUTLINE_OFFSET = 0.002F;
    private static final float LINE_WIDTH = 2.5F;
    private static final int RED = 120;
    private static final int GREEN = 255;
    private static final int BLUE = 180;
    private static RenderPipeline pipeline;
    private static RenderLayer layer;
    private static boolean rendererDisabled;

    private CelestiumPickaxeOutlineRenderer() {
    }

    public static synchronized void render(WorldRenderContext context, Collection<BlockPos> positions) {
        if (positions.isEmpty() || !ensureInitialized()) {
            return;
        }

        MatrixStack matrices = context.matrices();
        if (matrices == null || context.consumers() == null) {
            return;
        }

        Vec3d cameraPos = MinecraftClient.getInstance().gameRenderer.getCamera().getCameraPos();
        VertexConsumer consumer = context.consumers().getBuffer(layer);

        matrices.push();
        matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        for (BlockPos pos : positions) {
            drawBox(matrices, consumer, pos);
        }
        matrices.pop();
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
                    "shopsandtools_celestium_pickaxe_outline",
                    RenderSetup.builder(pipeline).translucent().expectedBufferSize(4096).build()
            );
            return true;
        } catch (RuntimeException exception) {
            rendererDisabled = true;
            ShopsAndTools.LOGGER.error("Failed to initialize Celestium pickaxe outline renderer; disabling it for this session", exception);
            return false;
        }
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
                    .withLocation(Identifier.of(ShopsAndTools.MOD_ID, "celestium_pickaxe_outline"))
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
            ShopsAndTools.LOGGER.error("Failed to create the Celestium pickaxe outline pipeline; disabling it for this session", exception);
            return null;
        }
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
                .normal(entry, direction)
                .lineWidth(LINE_WIDTH);
        consumer.vertex(entry, endX, endY, endZ)
                .color(RED, GREEN, BLUE, 255)
                .normal(entry, direction)
                .lineWidth(LINE_WIDTH);
    }
}
