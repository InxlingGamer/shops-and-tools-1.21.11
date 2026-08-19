package net.inklinggamer.shopsandtools.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.inklinggamer.shopsandtools.item.CelestiumShovelHelper;
import net.inklinggamer.shopsandtools.mixin.client.ClientPlayerInteractionManagerAccessor;
import net.inklinggamer.shopsandtools.network.ArmCelestiumShovelSlamPayload;
import net.inklinggamer.shopsandtools.network.ToggleCelestiumShovelAreaModePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.SoundType;
import java.util.ArrayList;
import java.util.List;

public final class CelestiumShovelClient {
    private static final int BREAKING_INFO_ID_BASE = 9200;

    private static final List<BlockPos> outlinePositions = new ArrayList<>();
    private static final List<BlockPos> breakingAnimationPositions = new ArrayList<>();

    private static boolean areaToggleHeld;
    private static boolean slamArmHeld;
    private static boolean slamMiningSuppressed;
    private static boolean slamJumped;
    private static boolean slamSneakPrimed;
    private static boolean wasOnGround;
    private static boolean wasSneaking;
    private static BlockPos breakingCenter;
    private static Direction breakingFace;
    private static int lastBreakingStage = -1;
    private static BlockPos trialChamberMarkerPos;
    private static Identifier trialChamberMarkerDimensionId;
    private static int trialChamberMarkerRemainingTicks;

    private CelestiumShovelClient() {
    }

    public static void initialize() {
        ClientTickEvents.END_CLIENT_TICK.register(CelestiumShovelClient::tick);
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> resetState());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> resetState());
    }

    public static void tick(Minecraft client) {
        if (client.player == null || client.level == null) {
            areaToggleHeld = false;
            slamArmHeld = false;
            slamMiningSuppressed = false;
            clearOutline();
            clearBreakingState();
            clearBreakingAnimation(client);
            clearTrialChamberMarker();
            return;
        }

        if (!client.options.keyUse.isDown()) {
            areaToggleHeld = false;
        }

        if (!client.options.keyAttack.isDown()) {
            slamArmHeld = false;
            slamMiningSuppressed = false;
        }

        updateSlamSequence(client);
        if (slamMiningSuppressed) {
            clearBreakingState();
            clearOutline();
            clearBreakingAnimation(client);
        }
        updateOutline(client);
        updateBreakingAnimation(client);
        tickTrialChamberMarker(client);
    }

    public static void render(WorldRenderContext context) {
        CelestiumPickaxeOutlineRenderer.render(context, outlinePositions);
        CelestiumTrialChamberMarkerRenderer.render(context, trialChamberMarkerPos);
    }

    public static boolean handleRightClickToggle(Minecraft client) {
        if (client.player == null || client.level == null || client.gameMode == null) {
            return false;
        }

        if (areaToggleHeld) {
            return true;
        }

        if (!CelestiumShovelHelper.canToggleAreaMining(
                client.player,
                client.level,
                client.hitResult,
                client.gameMode.getPlayerMode()
        )) {
            return false;
        }

        areaToggleHeld = true;
        ToggleCelestiumShovelAreaModePayload.send();
        return true;
    }

    public static boolean handleGroundSlamAttempt(Minecraft client) {
        if (client.player == null || client.level == null) {
            return false;
        }

        if (slamMiningSuppressed) {
            return true;
        }

        if (!slamJumped
                || !slamSneakPrimed
                || !CelestiumShovelHelper.canArmSlam(client.player, client.level, client.hitResult)) {
            return false;
        }

        if (slamArmHeld) {
            return true;
        }

        slamArmHeld = true;
        slamMiningSuppressed = true;
        clearBreakingState();
        clearOutline();
        clearBreakingAnimation(client);
        client.player.swing(InteractionHand.MAIN_HAND);
        ArmCelestiumShovelSlamPayload.send();
        return true;
    }

    public static void onBreakingAttempt(BlockPos pos, Direction direction) {
        if (slamMiningSuppressed) {
            clearBreakingState();
            return;
        }

        breakingCenter = pos.immutable();
        breakingFace = direction;
    }

    public static void clearBreakingState() {
        breakingCenter = null;
        breakingFace = null;
    }

    public static float getAreaMiningDelta(Minecraft client, BlockPos pos, float fallbackDelta) {
        if (slamMiningSuppressed) {
            return fallbackDelta;
        }

        if (breakingFace == null || breakingCenter == null || !breakingCenter.equals(pos)) {
            return fallbackDelta;
        }

        float areaMiningDelta = shopsandtools$getAreaMiningTargets(client, pos, breakingFace).effectiveBreakingDelta();
        return areaMiningDelta > 0.0F ? areaMiningDelta : fallbackDelta;
    }

    public static boolean shouldDeferBreakPrediction(Minecraft client, BlockPos pos) {
        if (slamMiningSuppressed) {
            return false;
        }

        return shopsandtools$canUseAreaMining(client)
                && breakingCenter != null
                && breakingCenter.equals(pos);
    }

    public static void playDeferredBreakSound(Minecraft client, BlockPos pos) {
        if (client.player == null || client.level == null) {
            return;
        }

        SoundType soundGroup = client.level.getBlockState(pos).getSoundType();
        if (soundGroup == null || soundGroup.getVolume() <= 0.0F) {
            return;
        }

        client.player.playSound(soundGroup.getBreakSound(), soundGroup.getVolume(), soundGroup.getPitch());
    }

    public static void syncTrialChamberMarker(BlockPos pos, Identifier dimensionId, int durationTicks) {
        trialChamberMarkerPos = pos.immutable();
        trialChamberMarkerDimensionId = dimensionId;
        trialChamberMarkerRemainingTicks = durationTicks;
    }

    private static void updateSlamSequence(Minecraft client) {
        boolean onGround = client.player.onGround();
        boolean sneaking = client.player.isShiftKeyDown();

        if (!CelestiumShovelHelper.canUseGroundSlam(client.player)) {
            resetSlamSequence();
            slamMiningSuppressed = false;
            wasOnGround = onGround;
            wasSneaking = sneaking;
            return;
        }

        if (wasOnGround && !onGround && client.player.getDeltaMovement().y > 0.0D) {
            slamJumped = true;
            slamSneakPrimed = false;
        }

        if (!onGround && slamJumped && !slamSneakPrimed && sneaking && !wasSneaking) {
            slamSneakPrimed = true;
        }

        if (onGround) {
            resetSlamSequence();
            slamMiningSuppressed = false;
        }

        wasOnGround = onGround;
        wasSneaking = sneaking;
    }

    private static void updateOutline(Minecraft client) {
        outlinePositions.clear();
        if (slamMiningSuppressed || !shopsandtools$canUseAreaMining(client) || client.gameMode == null) {
            return;
        }

        if (!(client.hitResult instanceof net.minecraft.world.phys.BlockHitResult hitResult)) {
            return;
        }

        if (!CelestiumShovelHelper.isValidMiningTarget(
                client.player,
                client.level,
                hitResult.getBlockPos(),
                client.gameMode.getPlayerMode()
        )) {
            return;
        }

        outlinePositions.addAll(shopsandtools$getAreaMiningTargets(client, hitResult.getBlockPos(), hitResult.getDirection()).outlinePositions());
    }

    private static void updateBreakingAnimation(Minecraft client) {
        MultiPlayerGameMode interactionManager = client.gameMode;
        if (slamMiningSuppressed || !shopsandtools$canUseAreaMining(client) || interactionManager == null) {
            clearBreakingAnimation(client);
            return;
        }

        ClientPlayerInteractionManagerAccessor accessor = (ClientPlayerInteractionManagerAccessor) interactionManager;
        if (!accessor.shopsandtools$isBreakingBlock()) {
            clearBreakingAnimation(client);
            return;
        }

        BlockPos currentBreakingPos = accessor.shopsandtools$getCurrentBreakingPos();
        if (currentBreakingPos == null) {
            clearBreakingAnimation(client);
            return;
        }

        if (!CelestiumShovelHelper.isValidMiningTarget(
                client.player,
                client.level,
                currentBreakingPos,
                interactionManager.getPlayerMode()
        )) {
            clearBreakingAnimation(client);
            return;
        }

        if (breakingCenter == null || !breakingCenter.equals(currentBreakingPos) || breakingFace == null) {
            breakingCenter = currentBreakingPos.immutable();
            if (breakingFace == null) {
                clearBreakingAnimation(client);
                return;
            }
        }

        int currentStage = Math.max(-1, Math.min(9, (int) (accessor.shopsandtools$getCurrentBreakingProgress() * 10.0F) - 1));
        List<BlockPos> targets = shopsandtools$getAreaMiningTargets(client, currentBreakingPos, breakingFace).breakablePositions().stream()
                .filter(pos -> !pos.equals(currentBreakingPos))
                .toList();

        if (currentStage == lastBreakingStage && breakingAnimationPositions.equals(targets)) {
            return;
        }

        clearBreakingAnimation(client);
        lastBreakingStage = currentStage;
        breakingAnimationPositions.addAll(targets);
        if (currentStage < 0) {
            return;
        }

        for (int index = 0; index < breakingAnimationPositions.size(); index++) {
            client.levelRenderer.destroyBlockProgress(BREAKING_INFO_ID_BASE + index, breakingAnimationPositions.get(index), currentStage);
        }
    }

    private static void tickTrialChamberMarker(Minecraft client) {
        if (trialChamberMarkerRemainingTicks <= 0 || trialChamberMarkerPos == null) {
            clearTrialChamberMarker();
            return;
        }

        if (trialChamberMarkerDimensionId != null
                && !client.level.dimension().identifier().equals(trialChamberMarkerDimensionId)) {
            clearTrialChamberMarker();
            return;
        }

        trialChamberMarkerRemainingTicks--;
        if (trialChamberMarkerRemainingTicks <= 0) {
            clearTrialChamberMarker();
        }
    }

    private static void clearOutline() {
        outlinePositions.clear();
    }

    private static void clearBreakingAnimation(Minecraft client) {
        for (int index = 0; index < breakingAnimationPositions.size(); index++) {
            client.levelRenderer.destroyBlockProgress(BREAKING_INFO_ID_BASE + index, breakingAnimationPositions.get(index), -1);
        }
        breakingAnimationPositions.clear();
        lastBreakingStage = -1;
    }

    private static void clearTrialChamberMarker() {
        trialChamberMarkerPos = null;
        trialChamberMarkerDimensionId = null;
        trialChamberMarkerRemainingTicks = 0;
    }

    private static void resetSlamSequence() {
        slamJumped = false;
        slamSneakPrimed = false;
    }

    private static void resetState() {
        areaToggleHeld = false;
        slamArmHeld = false;
        slamMiningSuppressed = false;
        slamJumped = false;
        slamSneakPrimed = false;
        wasOnGround = false;
        wasSneaking = false;
        clearBreakingState();
        outlinePositions.clear();
        breakingAnimationPositions.clear();
        lastBreakingStage = -1;
        clearTrialChamberMarker();
    }

    private static boolean shopsandtools$canUseAreaMining(Minecraft client) {
        return client.player != null
                && CelestiumShovelHelper.isCelestiumShovel(client.player.getMainHandItem())
                && CelestiumShovelHelper.isAreaMiningEnabled(client.player.getMainHandItem());
    }

    private static CelestiumShovelHelper.AreaMiningTargets shopsandtools$getAreaMiningTargets(Minecraft client, BlockPos centerPos, Direction face) {
        if (!shopsandtools$canUseAreaMining(client) || client.player == null || client.level == null || client.gameMode == null) {
            return new CelestiumShovelHelper.AreaMiningTargets(List.of(), List.of(), 0.0F);
        }

        return CelestiumShovelHelper.getAreaMiningTargets(
                client.player,
                client.level,
                centerPos,
                face,
                client.gameMode.getPlayerMode()
        );
    }
}
