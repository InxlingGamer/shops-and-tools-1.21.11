package net.inklinggamer.shopsandtools.item;

import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.function.Consumer;

public class CelestiumPickaxeItem extends Item {
    public CelestiumPickaxeItem(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
        boolean silkModeEnabled = CelestiumPickaxeHelper.isSilkModeEnabled(stack);
        boolean areaMiningEnabled = CelestiumPickaxeHelper.isAreaMiningEnabled(stack);

        textConsumer.accept(Text.translatable(
                silkModeEnabled
                        ? "tooltip.shopsandtools.celestium_pickaxe_mode_silk_touch"
                        : "tooltip.shopsandtools.celestium_pickaxe_mode_fortune"
        ).formatted(Formatting.AQUA));
        textConsumer.accept(Text.translatable(
                areaMiningEnabled
                        ? "tooltip.shopsandtools.celestium_pickaxe_area_enabled"
                        : "tooltip.shopsandtools.celestium_pickaxe_area_disabled"
        ).formatted(Formatting.GREEN));
    }
}
