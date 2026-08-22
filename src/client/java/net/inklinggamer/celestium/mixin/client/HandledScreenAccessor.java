package net.inklinggamer.celestium.mixin.client;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(HandledScreen.class)
public interface HandledScreenAccessor {
    @Accessor("x")
    int celestium$getX();

    @Accessor("y")
    int celestium$getY();

    @Accessor("focusedSlot")
    Slot celestium$getFocusedSlot();
}
