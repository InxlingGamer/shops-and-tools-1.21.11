package net.inklinggamer.celestium.mixin.client;

import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(AnvilScreen.class)
public abstract class AnvilScreenMixin {
    @ModifyConstant(method = "renderLabels", constant = @Constant(intValue = 40))
    private int celestium$removeTooExpensiveTextLimit(int vanillaLimit) {
        return Integer.MAX_VALUE;
    }
}
