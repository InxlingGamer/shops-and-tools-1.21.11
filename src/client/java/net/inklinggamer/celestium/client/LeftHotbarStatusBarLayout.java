package net.inklinggamer.celestium.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class LeftHotbarStatusBarLayout {
    public static final int DEFAULT_BAR_WIDTH = 74;
    public static final int OFFHAND_BAR_WIDTH = 56;
    public static final int BAR_HEIGHT = 5;
    private static final int HOTBAR_HALF_WIDTH = 91;
    private static final int HOTBAR_HEIGHT = 22;
    private static final int OFFHAND_SLOT_WIDTH = 29;
    private static final int BAR_GAP = 4;
    private static final int LEFT_SCREEN_MARGIN = 2;
    private static final int VERTICAL_ALIGNMENT_OFFSET = 15;

    private LeftHotbarStatusBarLayout() {
    }

    public static Layout resolve(GuiGraphicsExtractor drawContext, Player player) {
        return resolve(
                drawContext.guiWidth(),
                drawContext.guiHeight(),
                isLeftOffhandVisible(player)
        );
    }

    static Layout resolve(int screenWidth, int screenHeight, boolean leftOffhandVisible) {
        int centerX = screenWidth / 2;
        int hotbarY = screenHeight - HOTBAR_HEIGHT;
        int barY = hotbarY + VERTICAL_ALIGNMENT_OFFSET;
        int barWidth = DEFAULT_BAR_WIDTH;
        int barX = centerX - HOTBAR_HALF_WIDTH - BAR_GAP - barWidth;

        if (leftOffhandVisible) {
            barWidth = OFFHAND_BAR_WIDTH;
            int leftOffhandSlotX = centerX - HOTBAR_HALF_WIDTH - OFFHAND_SLOT_WIDTH;
            barX = leftOffhandSlotX - BAR_GAP - barWidth;
        }

        return new Layout(Math.max(LEFT_SCREEN_MARGIN, barX), barY, barWidth);
    }

    private static boolean isLeftOffhandVisible(Player player) {
        ItemStack offhandStack = player.getOffhandItem();
        return !offhandStack.isEmpty() && player.getMainArm().getOpposite() == HumanoidArm.LEFT;
    }

    public record Layout(int x, int y, int width) {
    }
}
