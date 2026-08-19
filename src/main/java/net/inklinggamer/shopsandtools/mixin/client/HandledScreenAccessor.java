package net.inklinggamer.shopsandtools.mixin.client;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractContainerScreen.class)
public interface HandledScreenAccessor {
    @Accessor("leftPos")
    int shopsandtools$getX();

    @Accessor("topPos")
    int shopsandtools$getY();

    @Accessor("hoveredSlot")
    Slot shopsandtools$getFocusedSlot();
}
