package net.inklinggamer.celestium.client;

import net.inklinggamer.celestium.mixin.client.RenderLayerInvoker;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;

import java.util.OptionalDouble;

public final class CelestiumRenderLayers extends RenderPhase {
    private CelestiumRenderLayers() {
        super("celestium_render_layers", () -> { }, () -> { });
    }

    public static RenderLayer lines(String name, double width, boolean seeThroughWalls) {
        RenderLayer.MultiPhaseParameters phases = RenderLayer.MultiPhaseParameters.builder()
                .program(LINES_PROGRAM)
                .transparency(TRANSLUCENT_TRANSPARENCY)
                .depthTest(seeThroughWalls ? ALWAYS_DEPTH_TEST : LEQUAL_DEPTH_TEST)
                .cull(DISABLE_CULLING)
                .writeMaskState(ALL_MASK)
                .lineWidth(new LineWidth(OptionalDouble.of(width)))
                .build(false);
        return RenderLayerInvoker.celestium$create(
                name,
                VertexFormats.LINES,
                VertexFormat.DrawMode.LINES,
                4096,
                phases
        );
    }
}
