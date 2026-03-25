package net.inklinggamer.shopsandtools.item;

import net.inklinggamer.shopsandtools.player.CelestiumHoeManager;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.item.HoeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;

import java.util.function.Consumer;

public class CelestiumHoeItem extends HoeItem {
    public CelestiumHoeItem(Settings settings) {
        super(ModToolMaterials.CELESTIUM, -4.0F, 0.0F, settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (context.getPlayer() == null || !CelestiumHoeHelper.isSupportedMatureCrop(context.getWorld().getBlockState(context.getBlockPos()))) {
            return super.useOnBlock(context);
        }

        if (context.getWorld().isClient()) {
            return ActionResult.SUCCESS;
        }

        boolean harvested = CelestiumHoeManager.harvestAndReplant(
                (ServerWorld) context.getWorld(),
                context.getPlayer(),
                context.getStack(),
                context.getBlockPos()
        );
        return harvested ? ActionResult.SUCCESS : ActionResult.PASS;
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
        textConsumer.accept(Text.translatable("tooltip.shopsandtools.celestium_hoe_harvest").formatted(Formatting.GREEN));
        textConsumer.accept(Text.translatable("tooltip.shopsandtools.celestium_hoe_growth_aura").formatted(Formatting.AQUA));
    }
}
