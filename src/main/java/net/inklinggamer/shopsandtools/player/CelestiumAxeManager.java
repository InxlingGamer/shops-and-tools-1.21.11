package net.inklinggamer.shopsandtools.player;

import net.inklinggamer.shopsandtools.item.CelestiumAxeHelper;
import net.inklinggamer.shopsandtools.world.CelestiumPlacedLogState;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public final class CelestiumAxeManager {
    private static final float FULLY_CHARGED_HIT_THRESHOLD = 0.9F;
    private static final int SLOWNESS_DURATION_TICKS = 60;
    private static final int SLOWNESS_AMPLIFIER = 1;

    private CelestiumAxeManager() {
    }

    public static boolean isCelestiumAxeEquipped(PlayerEntity player) {
        return CelestiumAxeHelper.isCelestiumAxe(player.getMainHandStack());
    }

    public static boolean isCelestiumAxeHeldForXp(PlayerEntity player) {
        return CelestiumAxeHelper.isCelestiumAxe(player.getMainHandStack())
                || CelestiumAxeHelper.isCelestiumAxe(player.getOffHandStack());
    }

    public static void onDirectAxeDamage(ServerPlayerEntity player, LivingEntity target, float attackCooldownProgress, float damageDealt) {
        if (!isCelestiumAxeEquipped(player) || damageDealt <= 0.0F || attackCooldownProgress <= FULLY_CHARGED_HIT_THRESHOLD) {
            return;
        }

        target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, SLOWNESS_DURATION_TICKS, SLOWNESS_AMPLIFIER));
    }

    public static void onBlockPlaced(ServerWorld world, BlockPos pos, BlockState placedState) {
        if (CelestiumAxeHelper.isEligibleWoodBlock(placedState)) {
            CelestiumPlacedLogState.get(world).markPlaced(pos);
        }
    }

    public static void onBlockBroken(ServerPlayerEntity player, BlockPos pos, BlockState brokenState, BlockEntity brokenBlockEntity, ItemStack breakingTool) {
        if (brokenState.isAir() || !CelestiumAxeHelper.isEligibleWoodBlock(brokenState)) {
            return;
        }

        ServerWorld world = (ServerWorld) player.getEntityWorld();
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

        for (ItemStack drop : Block.getDroppedStacks(brokenState, world, pos, brokenBlockEntity, player, breakingTool)) {
            if (!drop.isEmpty()) {
                Block.dropStack(world, pos, drop);
            }
        }
    }
}
