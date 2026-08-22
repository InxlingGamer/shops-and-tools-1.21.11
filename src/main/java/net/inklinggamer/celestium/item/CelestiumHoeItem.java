package net.inklinggamer.celestium.item;

import net.inklinggamer.celestium.player.CelestiumHoeManager;
import net.minecraft.item.HoeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;

import java.util.List;

public class CelestiumHoeItem extends HoeItem {
    public CelestiumHoeItem(Settings settings) {
        super(ModToolMaterials.CELESTIUM, settings);
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
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("tooltip.celestium.celestium_hoe_harvest").formatted(Formatting.GREEN));
        tooltip.add(Text.translatable("tooltip.celestium.celestium_hoe_growth_aura").formatted(Formatting.AQUA));
    }
}
