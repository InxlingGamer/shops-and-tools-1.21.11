package net.inklinggamer.shopsandtools.client;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;

public final class CelestiumThrustCooldownHud {
    private static final int DEFAULT_BAR_WIDTH = 74;
    private static final int OFFHAND_BAR_WIDTH = 56;
    private static final int BAR_HEIGHT = 5;
    private static final int HOTBAR_HALF_WIDTH = 91;
    private static final int HOTBAR_HEIGHT = 22;
    private static final int OFFHAND_SLOT_WIDTH = 29;
    private static final int BAR_GAP = 4;
    private static final int LEFT_SCREEN_MARGIN = 2;
    private static final int VERTICAL_ALIGNMENT_OFFSET = 8;
    private static final Identifier BACKGROUND_TEXTURE = Identifier.ofVanilla("boss_bar/pink_background");
    private static final Identifier PROGRESS_TEXTURE = Identifier.ofVanilla("boss_bar/pink_progress");
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

    public static void renderNearHotbar(DrawContext drawContext, PlayerEntity player) {
        if (!isActive() || player == null) {
            return;
        }

        int centerX = drawContext.getScaledWindowWidth() / 2;
        int hotbarY = drawContext.getScaledWindowHeight() - HOTBAR_HEIGHT;
        int barY = hotbarY + VERTICAL_ALIGNMENT_OFFSET;
        int barWidth = DEFAULT_BAR_WIDTH;
        int barX = centerX - HOTBAR_HALF_WIDTH - BAR_GAP - barWidth;

        ItemStack offhandStack = player.getOffHandStack();
        boolean leftOffhandVisible = !offhandStack.isEmpty() && player.getMainArm().getOpposite() == Arm.LEFT;
        if (leftOffhandVisible) {
            barWidth = OFFHAND_BAR_WIDTH;
            int leftOffhandSlotX = centerX - HOTBAR_HALF_WIDTH - OFFHAND_SLOT_WIDTH;
            barX = leftOffhandSlotX - BAR_GAP - barWidth;
        }

        barX = Math.max(LEFT_SCREEN_MARGIN, barX);
        renderBar(drawContext, barX, barY, barWidth);
    }

    private static void renderBar(DrawContext drawContext, int x, int y, int barWidth) {
        drawContext.drawGuiTexture(RENDER_PIPELINE, BACKGROUND_TEXTURE, x, y, barWidth, BAR_HEIGHT);

        int progressWidth = MathHelper.clamp((int) (getProgress() * barWidth), 0, barWidth);
        if (progressWidth > 0) {
            drawContext.drawGuiTexture(RENDER_PIPELINE, PROGRESS_TEXTURE, x, y, progressWidth, BAR_HEIGHT);
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
