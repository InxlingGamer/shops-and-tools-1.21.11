package net.inklinggamer.shopsandtools.player;

import net.inklinggamer.shopsandtools.item.CelestiumPickaxeHelper;
import net.minecraft.block.BlockState;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

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

    public static void onBlockBroken(
            ServerPlayerEntity player,
            ServerPlayerInteractionManager interactionManager,
            BlockPos centerPos,
            BlockState centerState,
            ItemStack breakingTool
    ) {
        if (AREA_BREAK_IN_PROGRESS.get()) {
            return;
        }

        boolean areaMiningEnabled = isAreaMiningEnabled(player);
        PlayerState state = STATES.get(player.getUuid());
        boolean hasStoredAreaBreak = state != null
                && state.miningCenter != null
                && state.miningFace != null
                && CelestiumPickaxeHelper.shouldProcessStoredAreaBreak(areaMiningEnabled, state.miningCenter.equals(centerPos));
        List<BlockPos> areaTargets = hasStoredAreaBreak
                ? shopsandtools$getStoredAreaMiningTargets(player, centerPos, state.miningFace)
                : List.of();
        Set<BlockPos> veinTargets = new LinkedHashSet<>();

        if (CelestiumPickaxeHelper.shouldApplyVeinMining(player.isSneaking())) {
            shopsandtools$collectVeinTargets(player, centerPos, centerState, veinTargets);
            for (BlockPos targetPos : areaTargets) {
                shopsandtools$collectVeinTargets(player, targetPos, player.getEntityWorld().getBlockState(targetPos), veinTargets);
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
                BlockState targetState = player.getEntityWorld().getBlockState(targetPos);
                CelestiumPickaxeHelper.synchronizeEnchantAndModeComponents(player.getMainHandStack(), breakingTool);
                if (interactionManager.tryBreakBlock(targetPos) && veinMiningActivated) {
                    shopsandtools$playSecondaryBreakSound(player, targetPos, targetState);
                }
            }
        } finally {
            AREA_BREAK_IN_PROGRESS.set(false);
            if (!areaMiningEnabled || hasStoredAreaBreak) {
                clearMiningSelection(player);
            }
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

    private static void shopsandtools$collectVeinTargets(ServerPlayerEntity player, BlockPos centerPos, BlockState centerState, Set<BlockPos> veinTargets) {
        veinTargets.addAll(CelestiumPickaxeHelper.getVeinMiningTargets(
                player,
                player.getEntityWorld(),
                centerPos,
                centerState,
                player.interactionManager.getGameMode()
        ));
    }

    private static void shopsandtools$playSecondaryBreakSound(ServerPlayerEntity player, BlockPos pos, BlockState state) {
        if (state.isAir()) {
            return;
        }

        BlockSoundGroup soundGroup = state.getSoundGroup();
        if (soundGroup == null || soundGroup.getVolume() <= 0.0F) {
            return;
        }

        player.getEntityWorld().playSound(
                null,
                pos,
                soundGroup.getBreakSound(),
                SoundCategory.BLOCKS,
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
