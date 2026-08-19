package net.inklinggamer.celestium.registry;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

public final class BlockItemIds {
    public static final ResourceKey<Item> CELESTIUM_BLOCK = key("celestium_block");

    private BlockItemIds() {
    }

    public static ResourceKey<Item> key(String path) {
        return ItemIds.key(path);
    }
}
