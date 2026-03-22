package net.inklinggamer.shopsandtools.client;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.inklinggamer.shopsandtools.mixin.client.HandledScreenAccessor;
import net.inklinggamer.shopsandtools.network.OpenCelestiumCraftingPayload;
import net.inklinggamer.shopsandtools.network.ReturnToInventoryPayload;
import net.inklinggamer.shopsandtools.player.CelestiumLeggingsManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.CraftingScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public final class CelestiumLeggingsClient {
    private static final int INVENTORY_BUTTON_WIDTH = 28;
    private static final int INVENTORY_BUTTON_HEIGHT = 20;
    private static final int INVENTORY_BUTTON_OFFSET_X = 127;
    private static final int INVENTORY_BUTTON_OFFSET_Y = 58;
    private static final int RETURN_BUTTON_WIDTH = 44;
    private static final int RETURN_BUTTON_HEIGHT = 20;
    private static final int RETURN_BUTTON_OFFSET_X = 108;
    private static final int RETURN_BUTTON_OFFSET_Y = 58;
    private static final String PORTABLE_CRAFTING_TITLE_KEY = "container.shopsandtools.celestium_portable_crafting";

    private CelestiumLeggingsClient() {
    }

    public static void initialize() {
        ScreenEvents.AFTER_INIT.register(CelestiumLeggingsClient::onAfterInit);
    }

    private static void onAfterInit(MinecraftClient client, Screen screen, int scaledWidth, int scaledHeight) {
        if (screen instanceof InventoryScreen inventoryScreen) {
            addInventoryCraftingButton(client, screen, inventoryScreen);
            return;
        }

        if (screen instanceof CraftingScreen craftingScreen && isCelestiumPortableCraftingScreen(craftingScreen)) {
            addReturnButton(client, screen, craftingScreen);
        }
    }

    private static void addInventoryCraftingButton(MinecraftClient client, Screen screen, InventoryScreen inventoryScreen) {
        ButtonWidget button = ButtonWidget.builder(
                        Text.translatable("button.shopsandtools.celestium_portable_crafting"),
                        ignored -> OpenCelestiumCraftingPayload.send()
                )
                .dimensions(0, 0, INVENTORY_BUTTON_WIDTH, INVENTORY_BUTTON_HEIGHT)
                .build();

        updateInventoryButton(client, inventoryScreen, button);
        Screens.getButtons(screen).add(button);
        ScreenEvents.afterTick(screen).register(ignored -> updateInventoryButton(client, inventoryScreen, button));
    }

    private static void addReturnButton(MinecraftClient client, Screen screen, CraftingScreen craftingScreen) {
        ButtonWidget button = ButtonWidget.builder(
                        Text.translatable("button.shopsandtools.celestium_return_to_inventory"),
                        ignored -> returnToInventory(client)
                )
                .dimensions(0, 0, RETURN_BUTTON_WIDTH, RETURN_BUTTON_HEIGHT)
                .build();

        updateReturnButton(craftingScreen, button);
        Screens.getButtons(screen).add(button);
        ScreenEvents.afterTick(screen).register(ignored -> updateReturnButton(craftingScreen, button));
    }

    private static void updateInventoryButton(MinecraftClient client, InventoryScreen inventoryScreen, ClickableWidget button) {
        positionInventoryButton(inventoryScreen, button);

        boolean wearingLeggings = client.player != null && CelestiumLeggingsManager.isCelestiumLeggingsEquipped(client.player);
        button.visible = wearingLeggings;
        button.active = wearingLeggings;
    }

    private static void updateReturnButton(CraftingScreen craftingScreen, ClickableWidget button) {
        positionReturnButton(craftingScreen, button);
    }

    private static void positionInventoryButton(InventoryScreen inventoryScreen, ClickableWidget button) {
        HandledScreenAccessor accessor = (HandledScreenAccessor) inventoryScreen;
        button.setX(accessor.shopsandtools$getX() + INVENTORY_BUTTON_OFFSET_X);
        button.setY(accessor.shopsandtools$getY() + INVENTORY_BUTTON_OFFSET_Y);
    }

    private static void positionReturnButton(CraftingScreen craftingScreen, ClickableWidget button) {
        HandledScreenAccessor accessor = (HandledScreenAccessor) craftingScreen;
        button.setX(accessor.shopsandtools$getX() + RETURN_BUTTON_OFFSET_X);
        button.setY(accessor.shopsandtools$getY() + RETURN_BUTTON_OFFSET_Y);
    }

    private static boolean isCelestiumPortableCraftingScreen(CraftingScreen craftingScreen) {
        return craftingScreen.getTitle().getString().equals(Text.translatable(PORTABLE_CRAFTING_TITLE_KEY).getString());
    }

    private static void returnToInventory(MinecraftClient client) {
        if (client.player == null) {
            return;
        }

        double cursorX = client.mouse.getX();
        double cursorY = client.mouse.getY();
        ItemStack cursorStack = client.player.currentScreenHandler.getCursorStack().copy();
        if (!cursorStack.isEmpty()) {
            client.player.currentScreenHandler.setCursorStack(ItemStack.EMPTY);
            client.player.playerScreenHandler.setCursorStack(cursorStack);
            client.player.currentScreenHandler.sendContentUpdates();
            client.player.playerScreenHandler.sendContentUpdates();
        }

        client.player.currentScreenHandler = client.player.playerScreenHandler;
        ReturnToInventoryPayload.send();
        client.setScreen(new InventoryScreen(client.player));
        GLFW.glfwSetCursorPos(client.getWindow().getHandle(), cursorX, cursorY);
    }
}
