package net.inklinggamer.shopsandtools.player;

import com.mojang.datafixers.util.Pair;
import net.inklinggamer.shopsandtools.item.CelestiumShovelHelper;
import net.inklinggamer.shopsandtools.network.SyncCelestiumTrialChamberMarkerPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderSet;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.phys.HitResult;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class CelestiumShovelManager {
    private static final ThreadLocal<Boolean> AREA_BREAK_IN_PROGRESS = ThreadLocal.withInitial(() -> false);

    private static final int SLAM_MINING_SUPPRESSION_TICKS = 6;
    private static final int TRIAL_CHAMBER_MARKER_DURATION_TICKS = 1200;
    private static final int TRIAL_CHAMBER_SEARCH_RADIUS_CHUNKS = 512;
    private static final float RAW_GOLD_DROP_CHANCE = 0.05F;
    private static final float RAW_IRON_DROP_CHANCE = 0.03F;
    private static final float DIAMOND_DROP_CHANCE = 0.003F;
    private static final DustParticleOptions SLAM_PARTICLE = new DustParticleOptions(0xFF59C0, 1.75F);

    private static final Map<UUID, PlayerState> STATES = new HashMap<>();

    private CelestiumShovelManager() {
    }

    public static void tickServer(MinecraftServer server) {
        STATES.entrySet().removeIf(entry -> {
            ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
            return player == null || entry.getValue().canDiscard(CelestiumShovelHelper.canUseGroundSlam(player));
        });
    }

    public static void tickPlayer(ServerPlayer player) {
        if (!player.isAlive() || player.isSpectator()) {
            STATES.remove(player.getUUID());
            return;
        }

        boolean canUseGroundSlam = CelestiumShovelHelper.canUseGroundSlam(player);
        PlayerState state = STATES.get(player.getUUID());
        if (state == null) {
            if (!canUseGroundSlam) {
                return;
            }

            state = new PlayerState(player.onGround(), player.isShiftKeyDown());
            STATES.put(player.getUUID(), state);
        }

        state.tickSuppression();
        updateGroundSlamTracking(player, state, canUseGroundSlam);

        if (state.canDiscard(canUseGroundSlam)) {
            STATES.remove(player.getUUID());
        }
    }

    public static boolean isHoldingCelestiumShovel(ServerPlayer player) {
        return CelestiumShovelHelper.isCelestiumShovel(player.getMainHandItem());
    }

    public static boolean isAreaMiningEnabled(ServerPlayer player) {
        return CelestiumShovelHelper.isAreaMiningEnabled(player.getMainHandItem());
    }

    public static HitResult getCurrentTarget(ServerPlayer player) {
        return player.pick(player.blockInteractionRange(), 1.0F, false);
    }

    public static boolean canToggleAreaMining(ServerPlayer player, HitResult hitResult) {
        return CelestiumShovelHelper.canToggleAreaMining(
                player,
                player.level(),
                hitResult,
                player.gameMode.getGameModeForPlayer()
        );
    }

    public static boolean isValidMiningTarget(ServerPlayer player, BlockPos pos) {
        return CelestiumShovelHelper.isValidMiningTarget(
                player,
                player.level(),
                pos,
                player.gameMode.getGameModeForPlayer()
        );
    }

    public static boolean toggleAreaMining(ServerPlayer player) {
        return CelestiumShovelHelper.toggleAreaMining(player.getMainHandItem());
    }

    public static void beginMiningSelection(ServerPlayer player, BlockPos pos, Direction face) {
        PlayerState state = STATES.get(player.getUUID());
        if (state != null && state.isMiningSuppressed()) {
            clearMiningSelection(player);
            return;
        }

        if (!isHoldingCelestiumShovel(player) || !isValidMiningTarget(player, pos)) {
            clearMiningSelection(player);
            return;
        }

        state = STATES.computeIfAbsent(player.getUUID(), uuid -> new PlayerState(player.onGround(), player.isShiftKeyDown()));
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
        if (state.canDiscard(CelestiumShovelHelper.canUseGroundSlam(player))) {
            STATES.remove(player.getUUID());
        }
    }

    public static boolean armSlam(ServerPlayer player) {
        PlayerState state = STATES.computeIfAbsent(player.getUUID(), uuid -> new PlayerState(player.onGround(), player.isShiftKeyDown()));
        HitResult hitResult = getCurrentTarget(player);
        if (!state.slamJumped
                || !state.slamSneakPrimed
                || !CelestiumShovelHelper.canArmSlam(player, player.level(), hitResult)) {
            return false;
        }

        clearMiningSelection(player);
        state.slamArmed = true;
        state.slamSearchActive = true;
        return true;
    }

    public static void onBlockBroken(
            ServerPlayer player,
            ServerPlayerGameMode interactionManager,
            BlockPos centerPos,
            @Nullable BlockState brokenState,
            @Nullable BlockEntity brokenBlockEntity,
            ItemStack breakingTool
    ) {
        trySpawnBonusDrops(player, centerPos, brokenState, brokenBlockEntity, breakingTool);

        if (AREA_BREAK_IN_PROGRESS.get()) {
            return;
        }

        PlayerState state = STATES.get(player.getUUID());
        if (state != null && state.isMiningSuppressed()) {
            clearMiningSelection(player);
            return;
        }

        if (!isAreaMiningEnabled(player)) {
            clearMiningSelection(player);
            return;
        }

        if (state == null || state.miningCenter == null || state.miningFace == null || !state.miningCenter.equals(centerPos)) {
            return;
        }

        AREA_BREAK_IN_PROGRESS.set(true);
        try {
            for (BlockPos targetPos : getAreaMiningTargets(player, centerPos)) {
                if (targetPos.equals(centerPos)) {
                    continue;
                }

                interactionManager.destroyBlock(targetPos);
            }
        } finally {
            AREA_BREAK_IN_PROGRESS.set(false);
            clearMiningSelection(player);
        }
    }

    public static float getAreaMiningDelta(ServerPlayer player, BlockPos centerPos, float fallbackDelta) {
        PlayerState state = getActiveMiningState(player, centerPos);
        if (state == null) {
            return fallbackDelta;
        }

        float areaMiningDelta = CelestiumShovelHelper.getAreaMiningTargets(
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

        return CelestiumShovelHelper.getAreaMiningTargets(
                player,
                player.level(),
                centerPos,
                state.miningFace,
                player.gameMode.getGameModeForPlayer()
        ).breakablePositions();
    }

    private static PlayerState getActiveMiningState(ServerPlayer player, BlockPos centerPos) {
        if (!isAreaMiningEnabled(player)) {
            return null;
        }

        PlayerState state = STATES.get(player.getUUID());
        if (state == null
                || state.isMiningSuppressed()
                || state.miningCenter == null
                || state.miningFace == null
                || !state.miningCenter.equals(centerPos)) {
            return null;
        }

        return state;
    }

    private static void trySpawnBonusDrops(
            ServerPlayer player,
            BlockPos pos,
            @Nullable BlockState brokenState,
            @Nullable BlockEntity brokenBlockEntity,
            ItemStack breakingTool
    ) {
        if (player.isCreative()
                || brokenState == null
                || brokenState.isAir()
                || !CelestiumShovelHelper.isCelestiumShovel(breakingTool)
                || !CelestiumShovelHelper.isLooseEarthBlock(brokenState)) {
            return;
        }

        ServerLevel world = (ServerLevel) player.level();
        if (Block.getDrops(brokenState, world, pos, brokenBlockEntity, player, breakingTool).isEmpty()) {
            return;
        }

        maybeDropBonus(world, pos, player.getRandom().nextFloat(), RAW_GOLD_DROP_CHANCE, new ItemStack(Items.RAW_GOLD));
        maybeDropBonus(world, pos, player.getRandom().nextFloat(), RAW_IRON_DROP_CHANCE, new ItemStack(Items.RAW_IRON));
        maybeDropBonus(world, pos, player.getRandom().nextFloat(), DIAMOND_DROP_CHANCE, new ItemStack(Items.DIAMOND));
    }

    private static void maybeDropBonus(ServerLevel world, BlockPos pos, float roll, float chance, ItemStack stack) {
        if (roll < chance) {
            Block.popResource(world, pos.above(), stack);
        }
    }

    private static void updateGroundSlamTracking(ServerPlayer player, PlayerState state, boolean canUseGroundSlam) {
        boolean onGround = player.onGround();
        boolean sneaking = player.isShiftKeyDown();

        if (!canUseGroundSlam) {
            state.resetSlamState();
            if (!state.isMiningSuppressed()) {
                state.slamSearchActive = false;
            }
            state.wasOnGround = onGround;
            state.wasSneaking = sneaking;
            return;
        }

        if (state.wasOnGround && !onGround && player.getDeltaMovement().y > 0.0D) {
            state.slamJumped = true;
            state.slamSneakPrimed = false;
            state.slamArmed = false;
            if (!state.isMiningSuppressed()) {
                state.slamSearchActive = false;
            }
        }

        if (!onGround && state.slamJumped && !state.slamSneakPrimed && sneaking && !state.wasSneaking) {
            state.slamSneakPrimed = true;
        }

        if (!state.wasOnGround && onGround) {
            if (state.slamArmed) {
                triggerGroundSlam(player, state);
            }
            state.resetSlamState();
            if (!state.isMiningSuppressed()) {
                state.slamSearchActive = false;
            }
        }

        state.wasOnGround = onGround;
        state.wasSneaking = sneaking;
    }

    private static void triggerGroundSlam(ServerPlayer player, PlayerState state) {
        ServerLevel world = (ServerLevel) player.level();
        clearMiningSelection(player);
        state.startMiningSuppression();

        world.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.WARDEN_SONIC_BOOM,
                SoundSource.PLAYERS,
                2.0F,
                1.0F
        );
        world.sendParticles(
                SLAM_PARTICLE,
                player.getX(),
                player.getY() + 0.1D,
                player.getZ(),
                48,
                1.4D,
                0.12D,
                1.4D,
                0.02D
        );

        HolderSet<Structure> trialChambers = HolderSet.direct(
                world.registryAccess().lookupOrThrow(Registries.STRUCTURE)
                        .wrapAsHolder(world.registryAccess().lookupOrThrow(Registries.STRUCTURE).getValueOrThrow(BuiltinStructures.TRIAL_CHAMBERS))
        );
        Pair<BlockPos, ?> locatedTrialChamber = world.getChunkSource().getGenerator().findNearestMapStructure(
                world,
                trialChambers,
                player.blockPosition(),
                TRIAL_CHAMBER_SEARCH_RADIUS_CHUNKS,
                false
        );
        BlockPos trialChamberPos = locatedTrialChamber == null ? null : locatedTrialChamber.getFirst();
        if (trialChamberPos == null) {
            player.displayClientMessage(Component.translatable("message.shopsandtools.celestium_shovel_trial_chamber_not_found"), true);
            return;
        }

        SyncCelestiumTrialChamberMarkerPayload.send(
                player,
                trialChamberPos,
                world.dimension().identifier(),
                TRIAL_CHAMBER_MARKER_DURATION_TICKS
        );
    }

    private static final class PlayerState {
        private BlockPos miningCenter;
        private Direction miningFace;
        private boolean slamJumped;
        private boolean slamSneakPrimed;
        private boolean slamArmed;
        private boolean slamSearchActive;
        private int miningSuppressionTicks;
        private boolean wasOnGround;
        private boolean wasSneaking;

        private PlayerState(boolean wasOnGround, boolean wasSneaking) {
            this.wasOnGround = wasOnGround;
            this.wasSneaking = wasSneaking;
        }

        private void resetSlamState() {
            this.slamJumped = false;
            this.slamSneakPrimed = false;
            this.slamArmed = false;
        }

        private void startMiningSuppression() {
            this.miningSuppressionTicks = SLAM_MINING_SUPPRESSION_TICKS;
        }

        private void tickSuppression() {
            if (this.miningSuppressionTicks > 0) {
                this.miningSuppressionTicks--;
            }

            if (this.miningSuppressionTicks == 0 && !this.slamJumped && !this.slamSneakPrimed && !this.slamArmed) {
                this.slamSearchActive = false;
            }
        }

        private boolean isMiningSuppressed() {
            return this.slamSearchActive && this.miningSuppressionTicks > 0;
        }

        private boolean canDiscard(boolean keepForGroundSlamTracking) {
            return this.miningCenter == null
                    && this.miningFace == null
                    && !this.slamJumped
                    && !this.slamSneakPrimed
                    && !this.slamArmed
                    && !this.slamSearchActive
                    && this.miningSuppressionTicks <= 0
                    && !keepForGroundSlamTracking;
        }
    }
}
