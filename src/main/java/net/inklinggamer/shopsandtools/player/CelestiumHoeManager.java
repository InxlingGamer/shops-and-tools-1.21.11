package net.inklinggamer.shopsandtools.player;

import net.inklinggamer.shopsandtools.item.CelestiumHoeHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldEvents;

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
        ACTIVE_HOLDERS.entrySet().removeIf(entry -> server.getPlayerManager().getPlayer(entry.getKey()) == null);
    }

    public static void tickPlayer(ServerPlayerEntity player) {
        if (!player.isAlive() || player.isSpectator() || !isCelestiumHoeHeldForAura(player)) {
            ACTIVE_HOLDERS.remove(player.getUuid());
            return;
        }

        ACTIVE_HOLDERS.put(player.getUuid(), new ActiveHolder(player.getEntityWorld().getRegistryKey(), player.getX(), player.getZ()));
    }

    public static boolean isCelestiumHoeHeldForAura(PlayerEntity player) {
        return CelestiumHoeHelper.isCelestiumHoe(player.getMainHandStack())
                || CelestiumHoeHelper.isCelestiumHoe(player.getOffHandStack());
    }

    public static boolean shouldApplyGrowthBoost(ServerWorld world, BlockPos pos, BlockState state) {
        return CelestiumHoeHelper.isGrowthBoostedCrop(state) && isCropGrowthBoosted(world, pos);
    }

    public static boolean isCropGrowthBoosted(ServerWorld world, BlockPos pos) {
        return anyHolderBoostsCrop(world.getRegistryKey(), pos, ACTIVE_HOLDERS.values());
    }

    static boolean anyHolderBoostsCrop(net.minecraft.registry.RegistryKey<net.minecraft.world.World> worldKey, BlockPos pos, Iterable<ActiveHolder> holders) {
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

    public static boolean harvestAndReplant(ServerWorld world, PlayerEntity player, ItemStack tool, BlockPos centerPos) {
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

    private static void harvestCrop(ServerWorld world, PlayerEntity player, ItemStack tool, BlockPos pos, BlockState state) {
        List<ItemStack> drops = List.of();
        if (!player.isCreative()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            drops = new ArrayList<>(Block.getDroppedStacks(state, world, pos, blockEntity, player, tool));
            CelestiumHoeHelper.consumeReplantItem(drops, CelestiumHoeHelper.getReplantCostItem(state));
        }

        world.syncWorldEvent(WorldEvents.BLOCK_BROKEN, pos, Block.getRawIdFromState(state));
        world.setBlockState(pos, CelestiumHoeHelper.getReplantState(state), Block.NOTIFY_ALL);
        world.playSound(null, pos, SoundEvents.BLOCK_CROP_BREAK, SoundCategory.BLOCKS, 1.0F, 1.0F);

        for (ItemStack drop : drops) {
            if (!drop.isEmpty()) {
                Block.dropStack(world, pos, drop);
            }
        }

        if (!player.isCreative()) {
            tool.damage(1, player, EquipmentSlot.MAINHAND);
        }
    }

    record ActiveHolder(net.minecraft.registry.RegistryKey<net.minecraft.world.World> worldKey, double x, double z) {
    }
}
