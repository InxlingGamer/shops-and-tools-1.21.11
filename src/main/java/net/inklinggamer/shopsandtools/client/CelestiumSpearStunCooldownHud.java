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

public final class CelestiumSpearStunCooldownHud {
    private static final Identifier BACKGROUND_TEXTURE = Identifier.ofVanilla("boss_bar/white_background");
    private static final Identifier PROGRESS_TEXTURE = Identifier.ofVanilla("boss_bar/white_progress");
    private static final RenderPipeline RENDER_PIPELINE = RenderPipelines.GUI_TEXTURED;

    private static long cooldownStartedAtMs;
    private static long cooldownDurationMs;

    private CelestiumSpearStunCooldownHud() {
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

    public static void renderNearHotbar(DrawContext drawContext, PlayerEntity player) {
        if (!isVisible(player)) {
            return;
        }

        LeftHotbarStatusBarLayout.Layout layout = LeftHotbarStatusBarLayout.resolve(drawContext, player);
        drawContext.drawGuiTexture(RENDER_PIPELINE, BACKGROUND_TEXTURE, layout.x(), layout.y(), layout.width(), LeftHotbarStatusBarLayout.BAR_HEIGHT);

        int progressWidth = MathHelper.clamp((int) (getProgress() * layout.width()), 0, layout.width());
        if (progressWidth > 0) {
            drawContext.drawGuiTexture(RENDER_PIPELINE, PROGRESS_TEXTURE, layout.x(), layout.y(), progressWidth, LeftHotbarStatusBarLayout.BAR_HEIGHT);
        }
    }

    private static boolean isVisible(PlayerEntity player) {
        return player != null
                && isActive()
                && player.getMainHandStack().isOf(ModItems.CELESTIUM_SPEAR);
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
