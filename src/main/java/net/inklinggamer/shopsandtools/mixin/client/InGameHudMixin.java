package net.inklinggamer.shopsandtools.mixin.client;

import net.inklinggamer.shopsandtools.client.CelestiumRageHud;
import net.inklinggamer.shopsandtools.client.CelestiumSpearStunCooldownHud;
import net.inklinggamer.shopsandtools.client.CelestiumThrustCooldownHud;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class InGameHudMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "renderItemHotbar", at = @At("RETURN"))
    private void shopsandtools$renderCelestiumThrustCooldownNearHotbar(GuiGraphics drawContext, DeltaTracker tickCounter, CallbackInfo ci) {
        if (this.minecraft.player == null) {
            return;
        }

        if (CelestiumThrustCooldownHud.isActive()) {
            CelestiumThrustCooldownHud.renderNearHotbar(drawContext, this.minecraft.player);
        }
        if (CelestiumSpearStunCooldownHud.isActive()) {
            CelestiumSpearStunCooldownHud.renderNearHotbar(drawContext, this.minecraft.player);
        }
        CelestiumRageHud.renderNearHotbar(drawContext, this.minecraft.player);
    }

    @ModifyConstant(method = "renderSelectedItemName", constant = @Constant(intValue = 59))
    private int shopsandtools$raiseHeldItemTooltipForExtraHeartRows(int vanillaOffset) {
        if (this.minecraft.player == null || this.minecraft.gameMode == null || !this.minecraft.gameMode.canHurtPlayer()) {
            return vanillaOffset;
        }

        Player player = this.minecraft.player;
        float maxHealth = (float) player.getAttributeValue(Attributes.MAX_HEALTH);
        float displayedHealth = Math.max(maxHealth, player.getHealth());
        int absorption = Mth.ceil(player.getAbsorptionAmount());
        int heartRows = Mth.ceil((displayedHealth + (float) absorption) / 2.0F / 10.0F);
        if (heartRows <= 1) {
            return vanillaOffset;
        }

        int rowHeight = Math.max(10 - (heartRows - 2), 3);
        int extraOffset = (heartRows - 1) * rowHeight;
        return vanillaOffset + extraOffset;
    }
}
