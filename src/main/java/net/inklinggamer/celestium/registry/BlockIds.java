package net.inklinggamer.celestium.registry;

import net.inklinggamer.celestium.Celestium;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;

public final class BlockIds {
    public static final ResourceKey<Block> CELESTIUM_BLOCK = key("celestium_block");

    private BlockIds() {
    }

    public static ResourceKey<Block> key(String path) {
        return ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(Celestium.MOD_ID, path));
    }
}
