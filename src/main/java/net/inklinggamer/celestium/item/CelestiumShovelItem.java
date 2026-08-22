package net.inklinggamer.celestium.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class CelestiumShovelItem extends ShovelItem {
    public CelestiumShovelItem(Settings settings) {
        super(ModToolMaterials.CELESTIUM, settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
        boolean areaMiningEnabled = CelestiumShovelHelper.isAreaMiningEnabled(stack);

        tooltip.add(Text.translatable(
                areaMiningEnabled
                        ? "tooltip.celestium.celestium_shovel_area_enabled"
                        : "tooltip.celestium.celestium_shovel_area_disabled"
        ).formatted(Formatting.GREEN));
    }
}
