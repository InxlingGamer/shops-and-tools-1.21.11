package net.inklinggamer.shopsandtools.item;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

public class CelestiumShovelItem extends ShovelItem {
    public CelestiumShovelItem(Properties settings) {
        super(ModToolMaterials.CELESTIUM, 1.5F, -3.0F, settings);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay displayComponent, Consumer<Component> textConsumer, TooltipFlag type) {
        boolean areaMiningEnabled = CelestiumShovelHelper.isAreaMiningEnabled(stack);

        textConsumer.accept(Component.translatable(
                areaMiningEnabled
                        ? "tooltip.shopsandtools.celestium_shovel_area_enabled"
                        : "tooltip.shopsandtools.celestium_shovel_area_disabled"
        ).withStyle(ChatFormatting.GREEN));
    }
}
