package net.inklinggamer.shopsandtools.mixin.client;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderSetup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(RenderLayer.class)
public interface RenderLayerInvoker {
    @Invoker("of")
    static RenderLayer shopsandtools$create(String name, RenderSetup renderSetup) {
        throw new AssertionError();
    }
}
