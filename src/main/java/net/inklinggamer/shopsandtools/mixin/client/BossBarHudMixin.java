package net.inklinggamer.shopsandtools.mixin.client;

import net.inklinggamer.shopsandtools.client.CelestiumThrustCooldownHud;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.client.gui.hud.ClientBossBar;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.UUID;

@Mixin(BossBarHud.class)
public abstract class BossBarHudMixin {
    @Shadow
    @Final
    private Map<UUID, ClientBossBar> bossBars;

    @Inject(method = "render", at = @At("RETURN"))
    private void shopsandtools$renderCelestiumThrustCooldownBar(DrawContext drawContext, CallbackInfo ci) {
        if (!CelestiumThrustCooldownHud.isActive()) {
            return;
        }

        if (this.bossBars.isEmpty()) {
            drawContext.createNewRootLayer();
        }

        CelestiumThrustCooldownHud.render(drawContext, getVisibleBossBarCount(drawContext.getScaledWindowHeight()));
    }

    private int getVisibleBossBarCount(int screenHeight) {
        int visibleBossBars = 0;
        int nextY = 12;

        for (ClientBossBar ignored : this.bossBars.values()) {
            visibleBossBars++;
            nextY += 19;
            if (nextY >= screenHeight / 3) {
                break;
            }
        }

        return visibleBossBars;
    }
}
