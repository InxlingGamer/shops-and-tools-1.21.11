package net.inklinggamer.shopsandtools.mixin.client;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(HandledScreen.class)
public interface HandledScreenAccessor {
    @Accessor("x")
    int shopsandtools$getX();

    @Accessor("y")
    int shopsandtools$getY();
}
