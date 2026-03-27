package net.inklinggamer.shopsandtools.player;

import net.inklinggamer.shopsandtools.item.CelestiumPickaxeHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class CelestiumPickaxeManager {
    private static final ThreadLocal<Boolean> AREA_BREAK_IN_PROGRESS = ThreadLocal.withInitial(() -> false);

    private static final Map<UUID, PlayerState> STATES = new HashMap<>();

    private CelestiumPickaxeManager() {
    }

    public static void tickServer(MinecraftServer server) {
        STATES.entrySet().removeIf(entry -> server.getPlayerManager().getPlayer(entry.getKey()) == null || entry.getValue().canDiscard());
    }

    public static boolean isHoldingCelestiumPickaxe(ServerPlayerEntity player) {
        return CelestiumPickaxeHelper.isCelestiumPickaxe(player.getMainHandStack());
    }

    public static boolean isCelestiumPickaxeHeldForXp(PlayerEntity player) {
        return CelestiumPickaxeHelper.isCelestiumPickaxe(player.getMainHandStack())
                || CelestiumPickaxeHelper.isCelestiumPickaxe(player.getOffHandStack());
    }

    public static boolean isAreaMiningEnabled(ServerPlayerEntity player) {
        return CelestiumPickaxeHelper.isAreaMiningEnabled(player.getMainHandStack());
    }

    public static HitResult getCurrentTarget(ServerPlayerEntity player) {
        return player.raycast(player.getBlockInteractionRange(), 1.0F, false);
    }

    public static boolean canToggleAreaMining(ServerPlayerEntity player, HitResult hitResult) {
        return CelestiumPickaxeHelper.canToggleAreaMining(
                player,
                player.getEntityWorld(),
                hitResult,
                player.interactionManager.getGameMode()
        );
    }

    public static boolean isValidMiningTarget(ServerPlayerEntity player, BlockPos pos) {
        return CelestiumPickaxeHelper.isValidMiningTarget(
                player,
                player.getEntityWorld(),
                pos,
                player.interactionManager.getGameMode()
        );
    }

    public static boolean toggleAreaMining(ServerPlayerEntity player) {
        return CelestiumPickaxeHelper.toggleAreaMining(player.getMainHandStack());
    }

    public static void beginMiningSelection(ServerPlayerEntity player, BlockPos pos, Direction face) {
        if (!isHoldingCelestiumPickaxe(player)
                || !CelestiumPickaxeHelper.isAreaMiningCenterEligible(
                        player,
                        player.getEntityWorld(),
                        pos,
                        player.interactionManager.getGameMode()
                )) {
            clearMiningSelection(player);
            return;
        }

        PlayerState state = STATES.computeIfAbsent(player.getUuid(), uuid -> new PlayerState());
        state.miningCenter = pos.toImmutable();
        state.miningFace = face;
    }

    public static void clearMiningSelection(ServerPlayerEntity player) {
        PlayerState state = STATES.get(player.getUuid());
        if (state == null) {
            return;
        }

        state.miningCenter = null;
        state.miningFace = null;
        if (state.canDiscard()) {
            STATES.remove(player.getUuid());
        }
    }

    public static void onBlockBroken(ServerPlayerEntity player, ServerPlayerInteractionManager interactionManager, BlockPos centerPos) {
        if (AREA_BREAK_IN_PROGRESS.get()) {
            return;
        }

        if (!isAreaMiningEnabled(player)) {
            clearMiningSelection(player);
            return;
        }

        PlayerState state = STATES.get(player.getUuid());
        if (state == null
                || state.miningCenter == null
                || state.miningFace == null
                || !CelestiumPickaxeHelper.shouldProcessStoredAreaBreak(isAreaMiningEnabled(player), state.miningCenter.equals(centerPos))) {
            return;
        }

        AREA_BREAK_IN_PROGRESS.set(true);
        try {
            for (BlockPos targetPos : shopsandtools$getStoredAreaMiningTargets(player, centerPos, state.miningFace)) {
                if (targetPos.equals(centerPos)) {
                    continue;
                }

                interactionManager.tryBreakBlock(targetPos);
            }
        } finally {
            AREA_BREAK_IN_PROGRESS.set(false);
            clearMiningSelection(player);
        }
    }

    public static boolean toggleEnchantMode(ItemStack stack, ServerPlayerEntity player) {
        return CelestiumPickaxeHelper.toggleEnchantMode(stack, player.getRegistryManager());
    }

    public static float getAreaMiningDelta(ServerPlayerEntity player, BlockPos centerPos, float fallbackDelta) {
        PlayerState state = getActiveMiningState(player, centerPos);
        if (state == null) {
            return fallbackDelta;
        }

        float areaMiningDelta = CelestiumPickaxeHelper.getAreaMiningTargets(
                player,
                player.getEntityWorld(),
                centerPos,
                state.miningFace,
                player.interactionManager.getGameMode()
        ).effectiveBreakingDelta();
        return areaMiningDelta > 0.0F ? areaMiningDelta : fallbackDelta;
    }

    public static List<BlockPos> getAreaMiningTargets(ServerPlayerEntity player, BlockPos centerPos) {
        PlayerState state = getActiveMiningState(player, centerPos);
        if (state == null) {
            return List.of();
        }

        return CelestiumPickaxeHelper.getAreaMiningTargets(
                player,
                player.getEntityWorld(),
                centerPos,
                state.miningFace,
                player.interactionManager.getGameMode()
        ).positions();
    }

    private static PlayerState getActiveMiningState(ServerPlayerEntity player, BlockPos centerPos) {
        if (!CelestiumPickaxeHelper.shouldApplyAreaMining(
                isAreaMiningEnabled(player),
                CelestiumPickaxeHelper.isAreaMiningCenterEligible(
                        player,
                        player.getEntityWorld(),
                        centerPos,
                        player.interactionManager.getGameMode()
                )
        )) {
            return null;
        }

        PlayerState state = STATES.get(player.getUuid());
        if (state == null || state.miningCenter == null || state.miningFace == null || !state.miningCenter.equals(centerPos)) {
            return null;
        }

        return state;
    }

    private static List<BlockPos> shopsandtools$getStoredAreaMiningTargets(ServerPlayerEntity player, BlockPos centerPos, Direction face) {
        return CelestiumPickaxeHelper.getMiningPlane(centerPos, face).stream()
                .filter(pos -> CelestiumPickaxeHelper.getMiningDelta(
                        player,
                        player.getEntityWorld(),
                        pos,
                        player.interactionManager.getGameMode()
                ) > 0.0F)
                .map(BlockPos::toImmutable)
                .toList();
    }

    private static final class PlayerState {
        private BlockPos miningCenter;
        private Direction miningFace;

        private boolean canDiscard() {
            return this.miningCenter == null && this.miningFace == null;
        }
    }
}
