package net.inklinggamer.shopsandtools.item;

import net.inklinggamer.shopsandtools.player.CelestiumHoeManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import java.util.function.Consumer;

public class CelestiumHoeItem extends HoeItem {
    public CelestiumHoeItem(Properties settings) {
        super(ModToolMaterials.CELESTIUM, -4.0F, 0.0F, settings);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getPlayer() == null || !CelestiumHoeHelper.isSupportedMatureCrop(context.getLevel().getBlockState(context.getClickedPos()))) {
            return super.useOn(context);
        }

        if (context.getLevel().isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        boolean harvested = CelestiumHoeManager.harvestAndReplant(
                (ServerLevel) context.getLevel(),
                context.getPlayer(),
                context.getItemInHand(),
                context.getClickedPos()
        );
        return harvested ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay displayComponent, Consumer<Component> textConsumer, TooltipFlag type) {
        textConsumer.accept(Component.translatable("tooltip.shopsandtools.celestium_hoe_harvest").withStyle(ChatFormatting.GREEN));
        textConsumer.accept(Component.translatable("tooltip.shopsandtools.celestium_hoe_growth_aura").withStyle(ChatFormatting.AQUA));
    }
}
