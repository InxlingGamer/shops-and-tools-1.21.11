package net.inklinggamer.celestium.item;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class CelestiumPickaxeItem extends PickaxeItem {
    public CelestiumPickaxeItem(Settings settings) {
        super(ModToolMaterials.CELESTIUM, settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
        boolean silkModeEnabled = CelestiumPickaxeHelper.isSilkModeEnabled(stack);
        boolean areaMiningEnabled = CelestiumPickaxeHelper.isAreaMiningEnabled(stack);

        tooltip.add(Text.translatable(
                silkModeEnabled
                        ? "tooltip.celestium.celestium_pickaxe_mode_silk_touch"
                        : "tooltip.celestium.celestium_pickaxe_mode_fortune"
        ).formatted(Formatting.AQUA));
        tooltip.add(Text.translatable(
                areaMiningEnabled
                        ? "tooltip.celestium.celestium_pickaxe_area_enabled"
                        : "tooltip.celestium.celestium_pickaxe_area_disabled"
        ).formatted(Formatting.GREEN));
    }
}
