package net.inklinggamer.shopsandtools.client;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;

public final class CelestiumThrustCooldownHud {
    private static final int BAR_WIDTH = 182;
    private static final int BAR_HEIGHT = 5;
    private static final int TOP_MARGIN = 12;
    private static final int BAR_SPACING = 19;
    private static final Identifier BACKGROUND_TEXTURE = Identifier.ofVanilla("boss_bar/green_background");
    private static final Identifier PROGRESS_TEXTURE = Identifier.ofVanilla("boss_bar/green_progress");
    private static final RenderPipeline RENDER_PIPELINE = RenderPipelines.GUI_TEXTURED;

    private static long cooldownStartedAtMs;
    private static long cooldownDurationMs;

    private CelestiumThrustCooldownHud() {
    }

    public static void syncCooldown(int remainingTicks) {
        if (remainingTicks <= 0) {
            clear();
            return;
        }

        cooldownStartedAtMs = Util.getMeasuringTimeMs();
        cooldownDurationMs = remainingTicks * 50L;
    }

    public static void tick(MinecraftClient client) {
        if (client.player == null || client.world == null) {
            clear();
        }
    }

    public static boolean isActive() {
        if (cooldownDurationMs <= 0L) {
            return false;
        }

        if (Util.getMeasuringTimeMs() >= cooldownStartedAtMs + cooldownDurationMs) {
            clear();
            return false;
        }

        return true;
    }

    public static void render(DrawContext drawContext, int visibleBossBars) {
        if (!isActive()) {
            return;
        }

        int y = TOP_MARGIN + visibleBossBars * BAR_SPACING;
        if (y >= drawContext.getScaledWindowHeight() / 3) {
            return;
        }

        int x = drawContext.getScaledWindowWidth() / 2 - BAR_WIDTH / 2;
        drawContext.drawGuiTexture(RENDER_PIPELINE, BACKGROUND_TEXTURE, BAR_WIDTH, BAR_HEIGHT, 0, 0, x, y, BAR_WIDTH, BAR_HEIGHT);

        int progressWidth = MathHelper.clamp((int) (getProgress() * BAR_WIDTH), 0, BAR_WIDTH);
        if (progressWidth > 0) {
            drawContext.drawGuiTexture(RENDER_PIPELINE, PROGRESS_TEXTURE, BAR_WIDTH, BAR_HEIGHT, 0, 0, x, y, progressWidth, BAR_HEIGHT);
        }
    }

    private static float getProgress() {
        if (cooldownDurationMs <= 0L) {
            return 1.0F;
        }

        long elapsedMs = Util.getMeasuringTimeMs() - cooldownStartedAtMs;
        return MathHelper.clamp((float) elapsedMs / (float) cooldownDurationMs, 0.0F, 1.0F);
    }

    private static void clear() {
        cooldownStartedAtMs = 0L;
        cooldownDurationMs = 0L;
    }
}
