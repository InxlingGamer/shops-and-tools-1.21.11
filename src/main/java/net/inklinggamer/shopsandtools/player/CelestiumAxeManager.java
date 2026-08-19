package net.inklinggamer.shopsandtools.player;

import net.inklinggamer.shopsandtools.item.CelestiumAxeHelper;
import net.inklinggamer.shopsandtools.world.CelestiumPlacedLogState;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class CelestiumAxeManager {
    private static final float FULLY_CHARGED_HIT_THRESHOLD = 0.9F;
    private static final int SLOWNESS_DURATION_TICKS = 60;
    private static final int SLOWNESS_AMPLIFIER = 1;

    private CelestiumAxeManager() {
    }

    public static boolean isCelestiumAxeEquipped(Player player) {
        return CelestiumAxeHelper.isCelestiumAxe(player.getMainHandItem());
    }

    public static boolean isCelestiumAxeHeldForXp(Player player) {
        return CelestiumAxeHelper.isCelestiumAxe(player.getMainHandItem())
                || CelestiumAxeHelper.isCelestiumAxe(player.getOffhandItem());
    }

    public static void onDirectAxeDamage(ServerPlayer player, LivingEntity target, float attackCooldownProgress, float damageDealt) {
        if (!isCelestiumAxeEquipped(player) || damageDealt <= 0.0F || attackCooldownProgress <= FULLY_CHARGED_HIT_THRESHOLD) {
            return;
        }

        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, SLOWNESS_DURATION_TICKS, SLOWNESS_AMPLIFIER));
    }

    public static void onBlockPlaced(ServerLevel world, BlockPos pos, BlockState placedState) {
        if (CelestiumAxeHelper.isEligibleWoodBlock(placedState)) {
            CelestiumPlacedLogState.get(world).markPlaced(pos);
        }
    }

    public static void onBlockBroken(ServerPlayer player, BlockPos pos, BlockState brokenState, BlockEntity brokenBlockEntity, ItemStack breakingTool) {
        if (brokenState.isAir() || !CelestiumAxeHelper.isEligibleWoodBlock(brokenState)) {
            return;
        }

        ServerLevel world = (ServerLevel) player.level();
        CelestiumPlacedLogState placedLogState = CelestiumPlacedLogState.get(world);
        boolean playerPlacedLog = placedLogState.isPlayerPlaced(pos);
        if (playerPlacedLog) {
            placedLogState.unmark(pos);
        }

        if (playerPlacedLog
                || !isCelestiumAxeEquipped(player)
                || player.isCreative()
                || !player.getRandom().nextBoolean()) {
            return;
        }

        for (ItemStack drop : Block.getDrops(brokenState, world, pos, brokenBlockEntity, player, breakingTool)) {
            if (!drop.isEmpty()) {
                Block.popResource(world, pos, drop);
            }
        }
    }
}
