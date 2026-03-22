package net.inklinggamer.shopsandtools.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class CelestiumPortableCraftingScreenHandler extends CraftingScreenHandler {
    private static final Text TITLE = Text.translatable("container.shopsandtools.celestium_portable_crafting");

    public CelestiumPortableCraftingScreenHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        super(syncId, playerInventory, context);
    }

    public static void openFor(ServerPlayerEntity player) {
        ItemStack cursorStack = player.currentScreenHandler.getCursorStack().copy();
        if (!cursorStack.isEmpty()) {
            player.currentScreenHandler.setCursorStack(ItemStack.EMPTY);
            player.currentScreenHandler.sendContentUpdates();
        }

        player.openHandledScreen(new SimpleNamedScreenHandlerFactory(
                (syncId, inventory, opener) -> new CelestiumPortableCraftingScreenHandler(
                        syncId,
                        inventory,
                        ScreenHandlerContext.create(opener.getEntityWorld(), opener.getBlockPos())
                ),
                TITLE
        ));

        if (!cursorStack.isEmpty()) {
            player.currentScreenHandler.setCursorStack(cursorStack);
            player.currentScreenHandler.sendContentUpdates();
        }
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }
}
