package net.inklinggamer.shopsandtools.client;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.inklinggamer.shopsandtools.item.ModItems;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.JumpingMount;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public final class CelestiumRageHud {
    private static final int MAX_RAGE_STACKS = 10;
    private static final int BAR_WIDTH = 182;
    private static final int BAR_HEIGHT = 5;
    private static final int HOTBAR_Y_OFFSET = 32;
    private static final int BAR_GAP = 1;
    private static final Identifier BACKGROUND_TEXTURE = Identifier.ofVanilla("hud/jump_bar_background");
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

        int x = drawContext.getScaledWindowWidth() / 2 - BAR_WIDTH / 2;
        int y = drawContext.getScaledWindowHeight() - HOTBAR_Y_OFFSET;
        if (player.getVehicle() instanceof JumpingMount) {
            y -= BAR_HEIGHT + BAR_GAP;
        }

        drawContext.drawGuiTexture(RENDER_PIPELINE, BACKGROUND_TEXTURE, x, y, BAR_WIDTH, BAR_HEIGHT);

        int progressWidth = MathHelper.clamp((int) ((rageStacks / (float) MAX_RAGE_STACKS) * BAR_WIDTH), 0, BAR_WIDTH);
        if (progressWidth <= 0) {
            return;
        }

        drawContext.drawGuiTexture(RENDER_PIPELINE, PROGRESS_TEXTURE, x, y, progressWidth, BAR_HEIGHT);
        drawContext.drawCenteredTextWithShadow(
                MinecraftClient.getInstance().textRenderer,
                Text.literal(Integer.toString(rageStacks)),
                x + BAR_WIDTH / 2,
                y - 9,
                0xFF5555
        );
    }

    private static boolean isVisible(PlayerEntity player) {
        return player != null && rageStacks > 0 && player.getMainHandStack().isOf(ModItems.CELESTIUM_SWORD);
    }
}
