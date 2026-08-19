package net.inklinggamer.celestium.item;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

public class CelestiumPickaxeItem extends Item {
    public CelestiumPickaxeItem(Properties settings) {
        super(settings);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay displayComponent, Consumer<Component> textConsumer, TooltipFlag type) {
        boolean silkModeEnabled = CelestiumPickaxeHelper.isSilkModeEnabled(stack);
        boolean areaMiningEnabled = CelestiumPickaxeHelper.isAreaMiningEnabled(stack);

        textConsumer.accept(Component.translatable(
                silkModeEnabled
                        ? "tooltip.celestium.celestium_pickaxe_mode_silk_touch"
                        : "tooltip.celestium.celestium_pickaxe_mode_fortune"
        ).withStyle(ChatFormatting.AQUA));
        textConsumer.accept(Component.translatable(
                areaMiningEnabled
                        ? "tooltip.celestium.celestium_pickaxe_area_enabled"
                        : "tooltip.celestium.celestium_pickaxe_area_disabled"
        ).withStyle(ChatFormatting.GREEN));
    }
}
