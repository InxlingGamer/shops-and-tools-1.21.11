package net.inklinggamer.celestium.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.joml.Vector3f;

public class CelestiumBlockItem extends BlockItem {
    public CelestiumBlockItem(Block block, Properties settings) {
        super(block, settings);
    }

    public Vector3f getLightColor(Player player, ItemStack stack) {
        return CelestiumHeldLight.createPearlescentFroglightColor();
    }
}
