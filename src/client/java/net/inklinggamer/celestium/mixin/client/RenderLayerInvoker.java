package net.inklinggamer.celestium.mixin.client;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexFormat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(RenderLayer.class)
public interface RenderLayerInvoker {
    @Invoker("of")
    static RenderLayer.MultiPhase celestium$create(String name, VertexFormat format, VertexFormat.DrawMode drawMode, int expectedBufferSize, RenderLayer.MultiPhaseParameters phases) {
        throw new AssertionError();
    }
}
