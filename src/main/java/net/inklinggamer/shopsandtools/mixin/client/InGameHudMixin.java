package net.inklinggamer.shopsandtools.mixin.client;

import net.inklinggamer.shopsandtools.client.CelestiumThrustCooldownHud;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "renderHotbar", at = @At("RETURN"))
    private void shopsandtools$renderCelestiumThrustCooldownNearHotbar(DrawContext drawContext, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (this.client.player == null || !CelestiumThrustCooldownHud.isActive()) {
            return;
        }

        CelestiumThrustCooldownHud.renderNearHotbar(drawContext, this.client.player);
    }

    @ModifyConstant(method = "renderHeldItemTooltip", constant = @Constant(intValue = 59))
    private int shopsandtools$raiseHeldItemTooltipForExtraHeartRows(int vanillaOffset) {
        if (this.client.player == null || this.client.interactionManager == null || !this.client.interactionManager.hasStatusBars()) {
            return vanillaOffset;
        }

        PlayerEntity player = this.client.player;
        float maxHealth = (float) player.getAttributeValue(EntityAttributes.MAX_HEALTH);
        float displayedHealth = Math.max(maxHealth, player.getHealth());
        int absorption = MathHelper.ceil(player.getAbsorptionAmount());
        int heartRows = MathHelper.ceil((displayedHealth + (float) absorption) / 2.0F / 10.0F);
        if (heartRows <= 1) {
            return vanillaOffset;
        }

        int rowHeight = Math.max(10 - (heartRows - 2), 3);
        int extraOffset = (heartRows - 1) * rowHeight;
        return vanillaOffset + extraOffset;
    }
}
