package net.inklinggamer.shopsandtools.item;

import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import org.joml.Vector3f;

public class CelestiumBlockItem extends BlockItem {
    public CelestiumBlockItem(Block block, Settings settings) {
        super(block, settings);
    }

    public Vector3f getLightColor(PlayerEntity player, ItemStack stack) {
        return CelestiumHeldLight.createPearlescentFroglightColor();
    }
}
