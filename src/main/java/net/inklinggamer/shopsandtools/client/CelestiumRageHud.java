package net.inklinggamer.shopsandtools.client;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.inklinggamer.shopsandtools.item.ModItems;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public final class CelestiumRageHud {
    private static final int MAX_RAGE_STACKS = 10;
    private static final int DEFAULT_BAR_WIDTH = 74;
    private static final int OFFHAND_BAR_WIDTH = 56;
    private static final int BAR_HEIGHT = 5;
    private static final int HOTBAR_HALF_WIDTH = 91;
    private static final int HOTBAR_HEIGHT = 22;
    private static final int OFFHAND_SLOT_WIDTH = 29;
    private static final int BAR_GAP = 4;
    private static final int LEFT_SCREEN_MARGIN = 2;
    private static final int VERTICAL_ALIGNMENT_OFFSET = 15;
    private static final Identifier BACKGROUND_TEXTURE = Identifier.ofVanilla("boss_bar/red_background");
    private static final Identifier PROGRESS_TEXTURE = Identifier.ofVanilla("boss_bar/red_progress");
    private static final RenderPipeline RENDER_PIPELINE = RenderPipelines.GUI_TEXTURED;

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
        drawContext.drawGuiTexture(RENDER_PIPELINE, BACKGROUND_TEXTURE, barX, barY, barWidth, BAR_HEIGHT);

        int progressWidth = MathHelper.clamp((int) ((rageStacks / (float) MAX_RAGE_STACKS) * barWidth), 0, barWidth);
        if (progressWidth <= 0) {
            return;
        }

        drawContext.drawGuiTexture(RENDER_PIPELINE, PROGRESS_TEXTURE, barX, barY, progressWidth, BAR_HEIGHT);
    }

    private static boolean isVisible(PlayerEntity player) {
        return player != null && rageStacks > 0 && player.getMainHandStack().isOf(ModItems.CELESTIUM_SWORD);
    }
}
