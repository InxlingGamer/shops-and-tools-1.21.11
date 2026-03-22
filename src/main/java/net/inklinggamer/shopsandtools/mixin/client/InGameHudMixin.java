package net.inklinggamer.shopsandtools.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    @Shadow
    @Final
    private MinecraftClient client;

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
