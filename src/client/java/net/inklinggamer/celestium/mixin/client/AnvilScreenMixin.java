package net.inklinggamer.celestium.mixin.client;

import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(AnvilScreen.class)
public abstract class AnvilScreenMixin {
    @ModifyConstant(
            method = "extractLabels(Lnet/minecraft/client/gui/GuiGraphicsExtractor;II)V",
            constant = @Constant(intValue = 40),
            require = 1,
            expect = 1
    )
    private int celestium$removeTooExpensiveTextLimit(int vanillaLimit) {
        return Integer.MAX_VALUE;
    }
}
