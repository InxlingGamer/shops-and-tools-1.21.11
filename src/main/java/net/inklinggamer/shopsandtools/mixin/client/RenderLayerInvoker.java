package net.inklinggamer.shopsandtools.mixin.client;

import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(RenderType.class)
public interface RenderLayerInvoker {
    @Invoker("create")
    static RenderType shopsandtools$create(String name, RenderSetup renderSetup) {
        throw new AssertionError();
    }
}
