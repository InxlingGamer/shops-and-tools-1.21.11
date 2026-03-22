package net.inklinggamer.shopsandtools.mixin.client;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.gl.RenderPipelines;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderPipelines.class)
public interface RenderPipelinesAccessor {
    @Accessor("RENDERTYPE_LINES_SNIPPET")
    static RenderPipeline.Snippet shopsandtools$getLineSnippet() {
        throw new AssertionError();
    }
}
