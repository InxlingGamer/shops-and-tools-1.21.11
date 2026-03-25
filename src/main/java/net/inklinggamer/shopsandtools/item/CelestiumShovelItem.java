package net.inklinggamer.shopsandtools.item;

import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.function.Consumer;

public class CelestiumShovelItem extends ShovelItem {
    public CelestiumShovelItem(Settings settings) {
        super(ModToolMaterials.CELESTIUM, 1.5F, -3.0F, settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
        boolean areaMiningEnabled = CelestiumShovelHelper.isAreaMiningEnabled(stack);

        textConsumer.accept(Text.translatable(
                areaMiningEnabled
                        ? "tooltip.shopsandtools.celestium_shovel_area_enabled"
                        : "tooltip.shopsandtools.celestium_shovel_area_disabled"
        ).formatted(Formatting.GREEN));
    }
}
