package net.inklinggamer.shopsandtools.screen;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.ItemStack;

public class CelestiumPortableCraftingScreenHandler extends CraftingMenu {
    private static final Component TITLE = Component.translatable("container.shopsandtools.celestium_portable_crafting");

    public CelestiumPortableCraftingScreenHandler(int syncId, Inventory playerInventory, ContainerLevelAccess context) {
        super(syncId, playerInventory, context);
    }

    public static void openFor(ServerPlayer player) {
        ItemStack cursorStack = player.containerMenu.getCarried().copy();
        if (!cursorStack.isEmpty()) {
            player.containerMenu.setCarried(ItemStack.EMPTY);
            player.containerMenu.broadcastChanges();
        }

        player.openMenu(new SimpleMenuProvider(
                (syncId, inventory, opener) -> new CelestiumPortableCraftingScreenHandler(
                        syncId,
                        inventory,
                        ContainerLevelAccess.create(opener.level(), opener.blockPosition())
                ),
                TITLE
        ));

        if (!cursorStack.isEmpty()) {
            player.containerMenu.setCarried(cursorStack);
            player.containerMenu.broadcastChanges();
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
