package net.inklinggamer.shopsandtools.client;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.inklinggamer.shopsandtools.item.ModItems;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;

public final class CelestiumRageHud {
    private static final int MAX_RAGE_STACKS = 10;
    private static final Identifier BACKGROUND_TEXTURE = Identifier.ofVanilla("boss_bar/red_background");
    private static final Identifier PROGRESS_TEXTURE = Identifier.ofVanilla("boss_bar/red_progress");
    private static final RenderPipeline RENDER_PIPELINE = RenderPipelines.GUI_TEXTURED;
    private static final long MAX_STACK_PULSE_PERIOD_MS = 1050L;
    private static final int MAX_STACK_DARK_RED_RGB = 0x6E0000;
    private static final int MAX_STACK_LIGHT_RED_RGB = 0xFF8A8A;

    private static int rageStacks;

    private CelestiumRageHud() {
    }

    public static void syncStacks(int stacks) {
        rageStacks = MathHelper.clamp(stacks, 0, MAX_RAGE_STACKS);
    }

    public static void tick(MinecraftClient client) {
        if (client.player == null || client.world == null) {
            rageStacks = 0;
        }
    }

    public static void renderNearHotbar(DrawContext drawContext, PlayerEntity player) {
        if (!isVisible(player)) {
            return;
        }

        LeftHotbarStatusBarLayout.Layout layout = LeftHotbarStatusBarLayout.resolve(drawContext, player);
        int barX = layout.x();
        int barY = layout.y();
        int barWidth = layout.width();

        drawContext.drawGuiTexture(RENDER_PIPELINE, BACKGROUND_TEXTURE, barX, barY, barWidth, LeftHotbarStatusBarLayout.BAR_HEIGHT);

        int progressWidth = MathHelper.clamp((int) ((rageStacks / (float) MAX_RAGE_STACKS) * barWidth), 0, barWidth);
        if (progressWidth <= 0) {
            return;
        }

        drawContext.drawGuiTexture(RENDER_PIPELINE, PROGRESS_TEXTURE, barX, barY, progressWidth, LeftHotbarStatusBarLayout.BAR_HEIGHT);
        if (rageStacks == MAX_RAGE_STACKS) {
            renderMaxStackPulse(drawContext, barX, barY, progressWidth);
        }
    }

    private static boolean isVisible(PlayerEntity player) {
        return player != null
                && rageStacks > 0
                && (player.getMainHandStack().isOf(ModItems.CELESTIUM_SWORD) || player.getMainHandStack().isOf(ModItems.CELESTIUM_AXE));
    }

    private static void renderMaxStackPulse(DrawContext drawContext, int x, int y, int progressWidth) {
        float cycle = (Util.getMeasuringTimeMs() % MAX_STACK_PULSE_PERIOD_MS) / (float) MAX_STACK_PULSE_PERIOD_MS;
        float breathe = 0.5F + 0.5F * MathHelper.sin(cycle * (float) (Math.PI * 2.0D));
        int darkAlpha = MathHelper.clamp((int) MathHelper.lerp(breathe, 48.0F, 92.0F), 0, 255);
        drawContext.fill(x, y, x + progressWidth, y + LeftHotbarStatusBarLayout.BAR_HEIGHT, withAlpha(MAX_STACK_DARK_RED_RGB, darkAlpha));

        float bandWidth = Math.max(10.0F, progressWidth * 0.28F);
        float bandCenter = MathHelper.lerp(cycle, -bandWidth, progressWidth + bandWidth);
        float highlightStrength = 0.65F + 0.35F * breathe;

        for (int column = 0; column < progressWidth; column++) {
            float distance = Math.abs((column + 0.5F) - bandCenter);
            float normalized = 1.0F - distance / bandWidth;
            if (normalized <= 0.0F) {
                continue;
            }

            float intensity = normalized * normalized * highlightStrength;
            int lightAlpha = MathHelper.clamp((int) (intensity * 170.0F), 0, 255);
            if (lightAlpha > 0) {
                drawContext.fill(x + column, y, x + column + 1, y + LeftHotbarStatusBarLayout.BAR_HEIGHT, withAlpha(MAX_STACK_LIGHT_RED_RGB, lightAlpha));
            }
        }
    }

    private static int withAlpha(int rgb, int alpha) {
        return (alpha << 24) | rgb;
    }
}
