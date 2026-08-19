package net.inklinggamer.celestium.player;

import net.inklinggamer.celestium.item.CelestiumHoeHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class CelestiumHoeManager {
    private static final Map<UUID, ActiveHolder> ACTIVE_HOLDERS = new HashMap<>();

    private CelestiumHoeManager() {
    }

    public static void tickServer(MinecraftServer server) {
        ACTIVE_HOLDERS.entrySet().removeIf(entry -> server.getPlayerList().getPlayer(entry.getKey()) == null);
    }

    public static void tickPlayer(ServerPlayer player) {
        if (!player.isAlive() || player.isSpectator() || !isCelestiumHoeHeldForAura(player)) {
            ACTIVE_HOLDERS.remove(player.getUUID());
            return;
        }

        ACTIVE_HOLDERS.put(player.getUUID(), new ActiveHolder(player.level().dimension(), player.getX(), player.getZ()));
    }

    public static boolean isCelestiumHoeHeldForAura(Player player) {
        return CelestiumHoeHelper.isCelestiumHoe(player.getMainHandItem())
                || CelestiumHoeHelper.isCelestiumHoe(player.getOffhandItem());
    }

    public static boolean shouldApplyGrowthBoost(ServerLevel world, BlockPos pos, BlockState state) {
        return CelestiumHoeHelper.isGrowthBoostedCrop(state) && isCropGrowthBoosted(world, pos);
    }

    public static boolean isCropGrowthBoosted(ServerLevel world, BlockPos pos) {
        return anyHolderBoostsCrop(world.dimension(), pos, ACTIVE_HOLDERS.values());
    }

    static boolean anyHolderBoostsCrop(net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> worldKey, BlockPos pos, Iterable<ActiveHolder> holders) {
        for (ActiveHolder holder : holders) {
            if (holder.worldKey().equals(worldKey) && isWithinGrowthAura(holder.x(), holder.z(), pos)) {
                return true;
            }
        }

        return false;
    }

    public static boolean isWithinGrowthAura(double playerX, double playerZ, BlockPos pos) {
        return Math.abs((pos.getX() + 0.5D) - playerX) <= CelestiumHoeHelper.GROWTH_BOOST_RADIUS
                && Math.abs((pos.getZ() + 0.5D) - playerZ) <= CelestiumHoeHelper.GROWTH_BOOST_RADIUS;
    }

    public static boolean harvestAndReplant(ServerLevel world, Player player, ItemStack tool, BlockPos centerPos) {
        List<BlockPos> targets = CelestiumHoeHelper.getHarvestTargets(centerPos, world::getBlockState);
        if (targets.isEmpty()) {
            return false;
        }

        boolean harvestedAny = false;
        for (BlockPos targetPos : targets) {
            if (tool.isEmpty()) {
                break;
            }

            BlockState state = world.getBlockState(targetPos);
            if (!CelestiumHoeHelper.isSupportedMatureCrop(state)) {
                continue;
            }

            harvestCrop(world, player, tool, targetPos, state);
            harvestedAny = true;
        }

        return harvestedAny;
    }

    private static void harvestCrop(ServerLevel world, Player player, ItemStack tool, BlockPos pos, BlockState state) {
        List<ItemStack> drops = List.of();
        if (!player.isCreative()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            drops = new ArrayList<>(Block.getDrops(state, world, pos, blockEntity, player, tool));
            CelestiumHoeHelper.consumeReplantItem(drops, CelestiumHoeHelper.getReplantCostItem(state));
        }

        world.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(state));
        world.setBlock(pos, CelestiumHoeHelper.getReplantState(state), Block.UPDATE_ALL);
        world.playSound(null, pos, SoundEvents.CROP_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);

        for (ItemStack drop : drops) {
            if (!drop.isEmpty()) {
                Block.popResource(world, pos, drop);
            }
        }

        if (!player.isCreative()) {
            tool.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
        }
    }

    record ActiveHolder(net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> worldKey, double x, double z) {
    }
}
