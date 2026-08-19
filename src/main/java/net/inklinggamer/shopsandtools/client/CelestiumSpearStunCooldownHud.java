package net.inklinggamer.shopsandtools.client;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.inklinggamer.shopsandtools.item.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.entity.player.Player;

public final class CelestiumSpearStunCooldownHud {
    private static final Identifier BACKGROUND_TEXTURE = Identifier.withDefaultNamespace("boss_bar/white_background");
    private static final Identifier PROGRESS_TEXTURE = Identifier.withDefaultNamespace("boss_bar/white_progress");
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

        cooldownStartedAtMs = Util.getMillis();
        cooldownDurationMs = remainingTicks * 50L;
    }

    public static void tick(Minecraft client) {
        if (client.player == null || client.level == null) {
            clear();
        }
    }

    public static boolean isActive() {
        if (cooldownDurationMs <= 0L) {
            return false;
        }

        if (Util.getMillis() >= cooldownStartedAtMs + cooldownDurationMs) {
            clear();
            return false;
        }

        return true;
    }

    public static void renderNearHotbar(GuiGraphicsExtractor drawContext, Player player) {
        if (!isVisible(player)) {
            return;
        }

        LeftHotbarStatusBarLayout.Layout layout = LeftHotbarStatusBarLayout.resolve(drawContext, player);
        drawContext.blitSprite(RENDER_PIPELINE, BACKGROUND_TEXTURE, layout.x(), layout.y(), layout.width(), LeftHotbarStatusBarLayout.BAR_HEIGHT);

        int progressWidth = Mth.clamp((int) (getProgress() * layout.width()), 0, layout.width());
        if (progressWidth > 0) {
            drawContext.blitSprite(RENDER_PIPELINE, PROGRESS_TEXTURE, layout.x(), layout.y(), progressWidth, LeftHotbarStatusBarLayout.BAR_HEIGHT);
        }
    }

    private static boolean isVisible(Player player) {
        return player != null
                && isActive()
                && player.getMainHandItem().is(ModItems.CELESTIUM_SPEAR);
    }

    private static float getProgress() {
        if (cooldownDurationMs <= 0L) {
            return 1.0F;
        }

        long elapsedMs = Util.getMillis() - cooldownStartedAtMs;
        return Mth.clamp((float) elapsedMs / (float) cooldownDurationMs, 0.0F, 1.0F);
    }

    private static void clear() {
        cooldownStartedAtMs = 0L;
        cooldownDurationMs = 0L;
    }
}
