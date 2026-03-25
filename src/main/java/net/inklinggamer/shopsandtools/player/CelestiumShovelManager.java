package net.inklinggamer.shopsandtools.player;

import com.mojang.datafixers.util.Pair;
import net.inklinggamer.shopsandtools.item.CelestiumShovelHelper;
import net.inklinggamer.shopsandtools.network.SyncCelestiumTrialChamberMarkerPayload;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureKeys;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class CelestiumShovelManager {
    private static final ThreadLocal<Boolean> AREA_BREAK_IN_PROGRESS = ThreadLocal.withInitial(() -> false);

    private static final int TRIAL_CHAMBER_MARKER_DURATION_TICKS = 1200;
    private static final int TRIAL_CHAMBER_SEARCH_RADIUS_CHUNKS = 512;
    private static final float RAW_GOLD_DROP_CHANCE = 0.05F;
    private static final float RAW_IRON_DROP_CHANCE = 0.03F;
    private static final float DIAMOND_DROP_CHANCE = 0.003F;
    private static final DustParticleEffect SLAM_PARTICLE = new DustParticleEffect(0xFF59C0, 1.75F);

    private static final Map<UUID, PlayerState> STATES = new HashMap<>();

    private CelestiumShovelManager() {
    }

    public static void tickServer(MinecraftServer server) {
        STATES.entrySet().removeIf(entry -> {
            ServerPlayerEntity player = server.getPlayerManager().getPlayer(entry.getKey());
            return player == null || entry.getValue().canDiscard(CelestiumShovelHelper.canUseGroundSlam(player));
        });
    }

    public static void tickPlayer(ServerPlayerEntity player) {
        if (!player.isAlive() || player.isSpectator()) {
            STATES.remove(player.getUuid());
            return;
        }

        boolean canUseGroundSlam = CelestiumShovelHelper.canUseGroundSlam(player);
        PlayerState state = STATES.get(player.getUuid());
        if (state == null) {
            if (!canUseGroundSlam) {
                return;
            }

            state = new PlayerState(player.isOnGround(), player.isSneaking());
            STATES.put(player.getUuid(), state);
        }

        updateGroundSlamTracking(player, state, canUseGroundSlam);

        if (state.canDiscard(canUseGroundSlam)) {
            STATES.remove(player.getUuid());
        }
    }

    public static boolean isHoldingCelestiumShovel(ServerPlayerEntity player) {
        return CelestiumShovelHelper.isCelestiumShovel(player.getMainHandStack());
    }

    public static boolean isAreaMiningEnabled(ServerPlayerEntity player) {
        return CelestiumShovelHelper.isAreaMiningEnabled(player.getMainHandStack());
    }

    public static HitResult getCurrentTarget(ServerPlayerEntity player) {
        return player.raycast(player.getBlockInteractionRange(), 1.0F, false);
    }

    public static boolean canToggleAreaMining(ServerPlayerEntity player, HitResult hitResult) {
        return CelestiumShovelHelper.canToggleAreaMining(
                player,
                player.getEntityWorld(),
                hitResult,
                player.interactionManager.getGameMode()
        );
    }

    public static boolean isValidMiningTarget(ServerPlayerEntity player, BlockPos pos) {
        return CelestiumShovelHelper.isValidMiningTarget(
                player,
                player.getEntityWorld(),
                pos,
                player.interactionManager.getGameMode()
        );
    }

    public static boolean toggleAreaMining(ServerPlayerEntity player) {
        return CelestiumShovelHelper.toggleAreaMining(player.getMainHandStack());
    }

    public static void beginMiningSelection(ServerPlayerEntity player, BlockPos pos, Direction face) {
        if (!isHoldingCelestiumShovel(player) || !isValidMiningTarget(player, pos)) {
            clearMiningSelection(player);
            return;
        }

        PlayerState state = STATES.computeIfAbsent(player.getUuid(), uuid -> new PlayerState(player.isOnGround(), player.isSneaking()));
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
        if (state.canDiscard(CelestiumShovelHelper.canUseGroundSlam(player))) {
            STATES.remove(player.getUuid());
        }
    }

    public static boolean armSlam(ServerPlayerEntity player) {
        PlayerState state = STATES.computeIfAbsent(player.getUuid(), uuid -> new PlayerState(player.isOnGround(), player.isSneaking()));
        HitResult hitResult = getCurrentTarget(player);
        if (!state.slamJumped
                || !state.slamSneakPrimed
                || !CelestiumShovelHelper.canArmSlam(player, player.getEntityWorld(), hitResult)) {
            return false;
        }

        state.slamArmed = true;
        return true;
    }

    public static void onBlockBroken(
            ServerPlayerEntity player,
            ServerPlayerInteractionManager interactionManager,
            BlockPos centerPos,
            @Nullable BlockState brokenState,
            @Nullable BlockEntity brokenBlockEntity,
            ItemStack breakingTool
    ) {
        trySpawnBonusDrops(player, centerPos, brokenState, brokenBlockEntity, breakingTool);

        if (AREA_BREAK_IN_PROGRESS.get()) {
            return;
        }

        if (!isAreaMiningEnabled(player)) {
            clearMiningSelection(player);
            return;
        }

        PlayerState state = STATES.get(player.getUuid());
        if (state == null || state.miningCenter == null || state.miningFace == null || !state.miningCenter.equals(centerPos)) {
            return;
        }

        AREA_BREAK_IN_PROGRESS.set(true);
        try {
            for (BlockPos targetPos : getAreaMiningTargets(player, centerPos)) {
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

    public static float getAreaMiningDelta(ServerPlayerEntity player, BlockPos centerPos, float fallbackDelta) {
        PlayerState state = getActiveMiningState(player, centerPos);
        if (state == null) {
            return fallbackDelta;
        }

        float areaMiningDelta = CelestiumShovelHelper.getAreaMiningTargets(
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

        return CelestiumShovelHelper.getAreaMiningTargets(
                player,
                player.getEntityWorld(),
                centerPos,
                state.miningFace,
                player.interactionManager.getGameMode()
        ).breakablePositions();
    }

    private static PlayerState getActiveMiningState(ServerPlayerEntity player, BlockPos centerPos) {
        if (!isAreaMiningEnabled(player)) {
            return null;
        }

        PlayerState state = STATES.get(player.getUuid());
        if (state == null || state.miningCenter == null || state.miningFace == null || !state.miningCenter.equals(centerPos)) {
            return null;
        }

        return state;
    }

    private static void trySpawnBonusDrops(
            ServerPlayerEntity player,
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

        ServerWorld world = (ServerWorld) player.getEntityWorld();
        if (Block.getDroppedStacks(brokenState, world, pos, brokenBlockEntity, player, breakingTool).isEmpty()) {
            return;
        }

        maybeDropBonus(world, pos, player.getRandom().nextFloat(), RAW_GOLD_DROP_CHANCE, new ItemStack(Items.RAW_GOLD));
        maybeDropBonus(world, pos, player.getRandom().nextFloat(), RAW_IRON_DROP_CHANCE, new ItemStack(Items.RAW_IRON));
        maybeDropBonus(world, pos, player.getRandom().nextFloat(), DIAMOND_DROP_CHANCE, new ItemStack(Items.DIAMOND));
    }

    private static void maybeDropBonus(ServerWorld world, BlockPos pos, float roll, float chance, ItemStack stack) {
        if (roll < chance) {
            Block.dropStack(world, pos.up(), stack);
        }
    }

    private static void updateGroundSlamTracking(ServerPlayerEntity player, PlayerState state, boolean canUseGroundSlam) {
        boolean onGround = player.isOnGround();
        boolean sneaking = player.isSneaking();

        if (!canUseGroundSlam) {
            state.resetSlamState();
            state.wasOnGround = onGround;
            state.wasSneaking = sneaking;
            return;
        }

        if (state.wasOnGround && !onGround && player.getVelocity().y > 0.0D) {
            state.slamJumped = true;
            state.slamSneakPrimed = false;
            state.slamArmed = false;
        }

        if (!onGround && state.slamJumped && !state.slamSneakPrimed && sneaking && !state.wasSneaking) {
            state.slamSneakPrimed = true;
        }

        if (!state.wasOnGround && onGround) {
            if (state.slamArmed) {
                triggerGroundSlam(player);
            }
            state.resetSlamState();
        }

        state.wasOnGround = onGround;
        state.wasSneaking = sneaking;
    }

    private static void triggerGroundSlam(ServerPlayerEntity player) {
        ServerWorld world = (ServerWorld) player.getEntityWorld();
        world.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.ENTITY_WARDEN_SONIC_BOOM,
                SoundCategory.PLAYERS,
                2.0F,
                1.0F
        );
        world.spawnParticles(
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

        RegistryEntryList<Structure> trialChambers = RegistryEntryList.of(
                world.getRegistryManager().getOrThrow(RegistryKeys.STRUCTURE)
                        .getEntry(world.getRegistryManager().getOrThrow(RegistryKeys.STRUCTURE).getValueOrThrow(StructureKeys.TRIAL_CHAMBERS))
        );
        Pair<BlockPos, ?> locatedTrialChamber = world.getChunkManager().getChunkGenerator().locateStructure(
                world,
                trialChambers,
                player.getBlockPos(),
                TRIAL_CHAMBER_SEARCH_RADIUS_CHUNKS,
                false
        );
        BlockPos trialChamberPos = locatedTrialChamber == null ? null : locatedTrialChamber.getFirst();
        if (trialChamberPos == null) {
            player.sendMessage(Text.translatable("message.shopsandtools.celestium_shovel_trial_chamber_not_found"), true);
            return;
        }

        SyncCelestiumTrialChamberMarkerPayload.send(
                player,
                trialChamberPos,
                world.getRegistryKey().getValue(),
                TRIAL_CHAMBER_MARKER_DURATION_TICKS
        );
    }

    private static final class PlayerState {
        private BlockPos miningCenter;
        private Direction miningFace;
        private boolean slamJumped;
        private boolean slamSneakPrimed;
        private boolean slamArmed;
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

        private boolean canDiscard(boolean keepForGroundSlamTracking) {
            return this.miningCenter == null
                    && this.miningFace == null
                    && !this.slamJumped
                    && !this.slamSneakPrimed
                    && !this.slamArmed
                    && !keepForGroundSlamTracking;
        }
    }
}
