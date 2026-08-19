package net.inklinggamer.shopsandtools.client;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.inklinggamer.shopsandtools.mixin.client.HandledScreenAccessor;
import net.inklinggamer.shopsandtools.network.OpenCelestiumCraftingPayload;
import net.inklinggamer.shopsandtools.network.ReturnToInventoryPayload;
import net.inklinggamer.shopsandtools.player.CelestiumLeggingsManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
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
    private static boolean expectingPortableCraftingScreen;

    private CelestiumLeggingsClient() {
    }

    public static void initialize() {
        ScreenEvents.AFTER_INIT.register(CelestiumLeggingsClient::onAfterInit);
    }

    private static void onAfterInit(Minecraft client, Screen screen, int scaledWidth, int scaledHeight) {
        if (!(screen instanceof CraftingScreen)) {
            expectingPortableCraftingScreen = false;
        }

        if (screen instanceof InventoryScreen inventoryScreen) {
            addInventoryCraftingButton(client, screen, inventoryScreen);
            return;
        }

        if (screen instanceof CraftingScreen craftingScreen && expectingPortableCraftingScreen) {
            expectingPortableCraftingScreen = false;
            addReturnButton(client, screen, craftingScreen);
        }
    }

    private static void addInventoryCraftingButton(Minecraft client, Screen screen, InventoryScreen inventoryScreen) {
        Button button = Button.builder(
                        Component.translatable("button.shopsandtools.celestium_portable_crafting"),
                        ignored -> openPortableCrafting()
                )
                .bounds(0, 0, INVENTORY_BUTTON_WIDTH, INVENTORY_BUTTON_HEIGHT)
                .build();

        updateInventoryButton(client, inventoryScreen, button);
        Screens.getWidgets(screen).add(button);
        ScreenEvents.afterTick(screen).register(ignored -> updateInventoryButton(client, inventoryScreen, button));
    }

    private static void addReturnButton(Minecraft client, Screen screen, CraftingScreen craftingScreen) {
        Button button = Button.builder(
                        Component.translatable("button.shopsandtools.celestium_return_to_inventory"),
                        ignored -> returnToInventory(client)
                )
                .bounds(0, 0, RETURN_BUTTON_WIDTH, RETURN_BUTTON_HEIGHT)
                .build();

        updateReturnButton(craftingScreen, button);
        Screens.getWidgets(screen).add(button);
        ScreenEvents.afterTick(screen).register(ignored -> updateReturnButton(craftingScreen, button));
    }

    private static void updateInventoryButton(Minecraft client, InventoryScreen inventoryScreen, AbstractWidget button) {
        positionInventoryButton(inventoryScreen, button);

        boolean wearingLeggings = client.player != null && CelestiumLeggingsManager.isCelestiumLeggingsEquipped(client.player);
        button.visible = wearingLeggings;
        button.active = wearingLeggings;
    }

    private static void updateReturnButton(CraftingScreen craftingScreen, AbstractWidget button) {
        positionReturnButton(craftingScreen, button);
    }

    private static void positionInventoryButton(InventoryScreen inventoryScreen, AbstractWidget button) {
        HandledScreenAccessor accessor = (HandledScreenAccessor) inventoryScreen;
        button.setX(accessor.shopsandtools$getX() + INVENTORY_BUTTON_OFFSET_X);
        button.setY(accessor.shopsandtools$getY() + INVENTORY_BUTTON_OFFSET_Y);
    }

    private static void positionReturnButton(CraftingScreen craftingScreen, AbstractWidget button) {
        HandledScreenAccessor accessor = (HandledScreenAccessor) craftingScreen;
        button.setX(accessor.shopsandtools$getX() + RETURN_BUTTON_OFFSET_X);
        button.setY(accessor.shopsandtools$getY() + RETURN_BUTTON_OFFSET_Y);
    }

    private static void openPortableCrafting() {
        expectingPortableCraftingScreen = true;
        OpenCelestiumCraftingPayload.send();
    }

    private static void returnToInventory(Minecraft client) {
        if (client.player == null) {
            return;
        }

        expectingPortableCraftingScreen = false;
        double cursorX = client.mouseHandler.xpos();
        double cursorY = client.mouseHandler.ypos();
        ItemStack cursorStack = client.player.containerMenu.getCarried().copy();
        if (!cursorStack.isEmpty()) {
            client.player.containerMenu.setCarried(ItemStack.EMPTY);
            client.player.inventoryMenu.setCarried(cursorStack);
            client.player.containerMenu.broadcastChanges();
            client.player.inventoryMenu.broadcastChanges();
        }

        client.player.containerMenu = client.player.inventoryMenu;
        ReturnToInventoryPayload.send();
        client.gui.setScreen(new InventoryScreen(client.player));
        GLFW.glfwSetCursorPos(client.getWindow().handle(), cursorX, cursorY);
    }
}
