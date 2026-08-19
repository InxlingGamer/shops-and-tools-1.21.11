package net.inklinggamer.celestium.player;

import net.inklinggamer.celestium.item.CelestiumPickaxeHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class CelestiumPickaxeManager {
    private static final ThreadLocal<Boolean> AREA_BREAK_IN_PROGRESS = ThreadLocal.withInitial(() -> false);

    private static final Map<UUID, PlayerState> STATES = new HashMap<>();

    private CelestiumPickaxeManager() {
    }

    public static void tickServer(MinecraftServer server) {
        STATES.entrySet().removeIf(entry -> server.getPlayerList().getPlayer(entry.getKey()) == null || entry.getValue().canDiscard());
    }

    public static boolean isHoldingCelestiumPickaxe(ServerPlayer player) {
        return CelestiumPickaxeHelper.isCelestiumPickaxe(player.getMainHandItem());
    }

    public static boolean isCelestiumPickaxeHeldForXp(Player player) {
        return CelestiumPickaxeHelper.isCelestiumPickaxe(player.getMainHandItem())
                || CelestiumPickaxeHelper.isCelestiumPickaxe(player.getOffhandItem());
    }

    public static boolean isAreaMiningEnabled(ServerPlayer player) {
        return CelestiumPickaxeHelper.isAreaMiningEnabled(player.getMainHandItem());
    }

    public static HitResult getCurrentTarget(ServerPlayer player) {
        return player.pick(player.blockInteractionRange(), 1.0F, false);
    }

    public static boolean canToggleAreaMining(ServerPlayer player, HitResult hitResult) {
        return CelestiumPickaxeHelper.canToggleAreaMining(
                player,
                player.level(),
                hitResult,
                player.gameMode.getGameModeForPlayer()
        );
    }

    public static boolean isValidMiningTarget(ServerPlayer player, BlockPos pos) {
        return CelestiumPickaxeHelper.isValidMiningTarget(
                player,
                player.level(),
                pos,
                player.gameMode.getGameModeForPlayer()
        );
    }

    public static boolean toggleAreaMining(ServerPlayer player) {
        return CelestiumPickaxeHelper.toggleAreaMining(player.getMainHandItem());
    }

    public static void beginMiningSelection(ServerPlayer player, BlockPos pos, Direction face) {
        if (!isHoldingCelestiumPickaxe(player)
                || !CelestiumPickaxeHelper.isAreaMiningCenterEligible(
                        player,
                        player.level(),
                        pos,
                        player.gameMode.getGameModeForPlayer()
                )) {
            clearMiningSelection(player);
            return;
        }

        PlayerState state = STATES.computeIfAbsent(player.getUUID(), uuid -> new PlayerState());
        state.miningCenter = pos.immutable();
        state.miningFace = face;
    }

    public static void clearMiningSelection(ServerPlayer player) {
        PlayerState state = STATES.get(player.getUUID());
        if (state == null) {
            return;
        }

        state.miningCenter = null;
        state.miningFace = null;
        if (state.canDiscard()) {
            STATES.remove(player.getUUID());
        }
    }

    public static void onBlockBroken(
            ServerPlayer player,
            ServerPlayerGameMode interactionManager,
            BlockPos centerPos,
            BlockState centerState,
            ItemStack breakingTool
    ) {
        if (AREA_BREAK_IN_PROGRESS.get()) {
            return;
        }

        boolean areaMiningEnabled = isAreaMiningEnabled(player);
        PlayerState state = STATES.get(player.getUUID());
        boolean hasStoredAreaBreak = state != null
                && state.miningCenter != null
                && state.miningFace != null
                && CelestiumPickaxeHelper.shouldProcessStoredAreaBreak(areaMiningEnabled, state.miningCenter.equals(centerPos));
        List<BlockPos> areaTargets = hasStoredAreaBreak
                ? celestium$getStoredAreaMiningTargets(player, centerPos, state.miningFace)
                : List.of();
        Set<BlockPos> veinTargets = new LinkedHashSet<>();

        if (CelestiumPickaxeHelper.shouldApplyVeinMining(player.isShiftKeyDown())) {
            celestium$collectVeinTargets(player, centerPos, centerState, veinTargets);
            for (BlockPos targetPos : areaTargets) {
                celestium$collectVeinTargets(player, targetPos, player.level().getBlockState(targetPos), veinTargets);
            }
        }

        List<BlockPos> secondaryTargets = CelestiumPickaxeHelper.combineSecondaryBreakTargets(centerPos, areaTargets, veinTargets);
        boolean veinMiningActivated = !veinTargets.isEmpty();
        if (secondaryTargets.isEmpty()) {
            if (!areaMiningEnabled || hasStoredAreaBreak) {
                clearMiningSelection(player);
            }
            return;
        }

        AREA_BREAK_IN_PROGRESS.set(true);
        try {
            for (BlockPos targetPos : secondaryTargets) {
                BlockState targetState = player.level().getBlockState(targetPos);
                CelestiumPickaxeHelper.synchronizeEnchantAndModeComponents(player.getMainHandItem(), breakingTool);
                if (interactionManager.destroyBlock(targetPos) && veinMiningActivated) {
                    celestium$playSecondaryBreakSound(player, targetPos, targetState);
                }
            }
        } finally {
            AREA_BREAK_IN_PROGRESS.set(false);
            if (!areaMiningEnabled || hasStoredAreaBreak) {
                clearMiningSelection(player);
            }
        }
    }

    public static boolean toggleEnchantMode(ItemStack stack, ServerPlayer player) {
        return CelestiumPickaxeHelper.toggleEnchantMode(stack, player.registryAccess());
    }

    public static float getAreaMiningDelta(ServerPlayer player, BlockPos centerPos, float fallbackDelta) {
        PlayerState state = getActiveMiningState(player, centerPos);
        if (state == null) {
            return fallbackDelta;
        }

        float areaMiningDelta = CelestiumPickaxeHelper.getAreaMiningTargets(
                player,
                player.level(),
                centerPos,
                state.miningFace,
                player.gameMode.getGameModeForPlayer()
        ).effectiveBreakingDelta();
        return areaMiningDelta > 0.0F ? areaMiningDelta : fallbackDelta;
    }

    public static List<BlockPos> getAreaMiningTargets(ServerPlayer player, BlockPos centerPos) {
        PlayerState state = getActiveMiningState(player, centerPos);
        if (state == null) {
            return List.of();
        }

        return CelestiumPickaxeHelper.getAreaMiningTargets(
                player,
                player.level(),
                centerPos,
                state.miningFace,
                player.gameMode.getGameModeForPlayer()
        ).positions();
    }

    private static PlayerState getActiveMiningState(ServerPlayer player, BlockPos centerPos) {
        if (!CelestiumPickaxeHelper.shouldApplyAreaMining(
                isAreaMiningEnabled(player),
                CelestiumPickaxeHelper.isAreaMiningCenterEligible(
                        player,
                        player.level(),
                        centerPos,
                        player.gameMode.getGameModeForPlayer()
                )
        )) {
            return null;
        }

        PlayerState state = STATES.get(player.getUUID());
        if (state == null || state.miningCenter == null || state.miningFace == null || !state.miningCenter.equals(centerPos)) {
            return null;
        }

        return state;
    }

    private static List<BlockPos> celestium$getStoredAreaMiningTargets(ServerPlayer player, BlockPos centerPos, Direction face) {
        return CelestiumPickaxeHelper.getMiningPlane(centerPos, face).stream()
                .filter(pos -> CelestiumPickaxeHelper.getMiningDelta(
                        player,
                        player.level(),
                        pos,
                        player.gameMode.getGameModeForPlayer()
                ) > 0.0F)
                .map(BlockPos::immutable)
                .toList();
    }

    private static void celestium$collectVeinTargets(ServerPlayer player, BlockPos centerPos, BlockState centerState, Set<BlockPos> veinTargets) {
        veinTargets.addAll(CelestiumPickaxeHelper.getVeinMiningTargets(
                player,
                player.level(),
                centerPos,
                centerState,
                player.gameMode.getGameModeForPlayer()
        ));
    }

    private static void celestium$playSecondaryBreakSound(ServerPlayer player, BlockPos pos, BlockState state) {
        if (state.isAir()) {
            return;
        }

        SoundType soundGroup = state.getSoundType();
        if (soundGroup == null || soundGroup.getVolume() <= 0.0F) {
            return;
        }

        player.level().playSound(
                null,
                pos,
                soundGroup.getBreakSound(),
                SoundSource.BLOCKS,
                soundGroup.getVolume(),
                soundGroup.getPitch()
        );
    }

    private static final class PlayerState {
        private BlockPos miningCenter;
        private Direction miningFace;

        private boolean canDiscard() {
            return this.miningCenter == null && this.miningFace == null;
        }
    }
}
