package net.inklinggamer.celestium.mixin.client;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractContainerScreen.class)
public interface HandledScreenAccessor {
    @Accessor("leftPos")
    int celestium$getX();

    @Accessor("topPos")
    int celestium$getY();

    @Accessor("hoveredSlot")
    Slot celestium$getFocusedSlot();
}
