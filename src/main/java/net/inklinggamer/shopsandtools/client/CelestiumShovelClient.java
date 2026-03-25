package net.inklinggamer.shopsandtools.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.inklinggamer.shopsandtools.item.CelestiumShovelHelper;
import net.inklinggamer.shopsandtools.mixin.client.ClientPlayerInteractionManagerAccessor;
import net.inklinggamer.shopsandtools.network.ArmCelestiumShovelSlamPayload;
import net.inklinggamer.shopsandtools.network.ToggleCelestiumShovelAreaModePayload;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.List;

public final class CelestiumShovelClient {
    private static final int BREAKING_INFO_ID_BASE = 9200;

    private static final List<BlockPos> outlinePositions = new ArrayList<>();
    private static final List<BlockPos> breakingAnimationPositions = new ArrayList<>();

    private static boolean areaToggleHeld;
    private static boolean slamArmHeld;
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

    public static void tick(MinecraftClient client) {
        if (client.player == null || client.world == null) {
            areaToggleHeld = false;
            slamArmHeld = false;
            clearOutline();
            clearBreakingAnimation(client);
            clearTrialChamberMarker();
            return;
        }

        if (!client.options.useKey.isPressed()) {
            areaToggleHeld = false;
        }

        if (!client.options.attackKey.isPressed()) {
            slamArmHeld = false;
        }

        updateSlamSequence(client);
        updateOutline(client);
        updateBreakingAnimation(client);
        tickTrialChamberMarker(client);
    }

    public static void render(WorldRenderContext context) {
        CelestiumPickaxeOutlineRenderer.render(context, outlinePositions);
        CelestiumTrialChamberMarkerRenderer.render(context, trialChamberMarkerPos);
    }

    public static boolean handleRightClickToggle(MinecraftClient client) {
        if (client.player == null || client.world == null || client.interactionManager == null) {
            return false;
        }

        if (areaToggleHeld) {
            return true;
        }

        if (!CelestiumShovelHelper.canToggleAreaMining(
                client.player,
                client.world,
                client.crosshairTarget,
                client.interactionManager.getCurrentGameMode()
        )) {
            return false;
        }

        areaToggleHeld = true;
        ToggleCelestiumShovelAreaModePayload.send();
        return true;
    }

    public static boolean handleGroundSlamAttempt(MinecraftClient client) {
        if (client.player == null || client.world == null) {
            return false;
        }

        if (!slamJumped
                || !slamSneakPrimed
                || !CelestiumShovelHelper.canArmSlam(client.player, client.world, client.crosshairTarget)) {
            return false;
        }

        if (slamArmHeld) {
            return true;
        }

        slamArmHeld = true;
        client.player.swingHand(Hand.MAIN_HAND);
        ArmCelestiumShovelSlamPayload.send();
        return true;
    }

    public static void onBreakingAttempt(BlockPos pos, Direction direction) {
        breakingCenter = pos.toImmutable();
        breakingFace = direction;
    }

    public static void clearBreakingState() {
        breakingCenter = null;
        breakingFace = null;
    }

    public static float getAreaMiningDelta(MinecraftClient client, BlockPos pos, float fallbackDelta) {
        if (breakingFace == null || breakingCenter == null || !breakingCenter.equals(pos)) {
            return fallbackDelta;
        }

        float areaMiningDelta = shopsandtools$getAreaMiningTargets(client, pos, breakingFace).effectiveBreakingDelta();
        return areaMiningDelta > 0.0F ? areaMiningDelta : fallbackDelta;
    }

    public static boolean shouldDeferBreakPrediction(MinecraftClient client, BlockPos pos) {
        return shopsandtools$canUseAreaMining(client)
                && breakingCenter != null
                && breakingCenter.equals(pos);
    }

    public static void playDeferredBreakSound(MinecraftClient client, BlockPos pos) {
        if (client.player == null || client.world == null) {
            return;
        }

        BlockSoundGroup soundGroup = client.world.getBlockState(pos).getSoundGroup();
        if (soundGroup == null || soundGroup.getVolume() <= 0.0F) {
            return;
        }

        client.player.playSound(soundGroup.getBreakSound(), soundGroup.getVolume(), soundGroup.getPitch());
    }

    public static void syncTrialChamberMarker(BlockPos pos, Identifier dimensionId, int durationTicks) {
        trialChamberMarkerPos = pos.toImmutable();
        trialChamberMarkerDimensionId = dimensionId;
        trialChamberMarkerRemainingTicks = durationTicks;
    }

    private static void updateSlamSequence(MinecraftClient client) {
        boolean onGround = client.player.isOnGround();
        boolean sneaking = client.player.isSneaking();

        if (!CelestiumShovelHelper.canUseGroundSlam(client.player)) {
            resetSlamSequence();
            wasOnGround = onGround;
            wasSneaking = sneaking;
            return;
        }

        if (wasOnGround && !onGround && client.player.getVelocity().y > 0.0D) {
            slamJumped = true;
            slamSneakPrimed = false;
        }

        if (!onGround && slamJumped && !slamSneakPrimed && sneaking && !wasSneaking) {
            slamSneakPrimed = true;
        }

        if (onGround) {
            resetSlamSequence();
        }

        wasOnGround = onGround;
        wasSneaking = sneaking;
    }

    private static void updateOutline(MinecraftClient client) {
        outlinePositions.clear();
        if (!shopsandtools$canUseAreaMining(client) || client.interactionManager == null) {
            return;
        }

        if (!(client.crosshairTarget instanceof net.minecraft.util.hit.BlockHitResult hitResult)) {
            return;
        }

        if (!CelestiumShovelHelper.isValidMiningTarget(
                client.player,
                client.world,
                hitResult.getBlockPos(),
                client.interactionManager.getCurrentGameMode()
        )) {
            return;
        }

        outlinePositions.addAll(shopsandtools$getAreaMiningTargets(client, hitResult.getBlockPos(), hitResult.getSide()).outlinePositions());
    }

    private static void updateBreakingAnimation(MinecraftClient client) {
        ClientPlayerInteractionManager interactionManager = client.interactionManager;
        if (!shopsandtools$canUseAreaMining(client) || interactionManager == null) {
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
                client.world,
                currentBreakingPos,
                interactionManager.getCurrentGameMode()
        )) {
            clearBreakingAnimation(client);
            return;
        }

        if (breakingCenter == null || !breakingCenter.equals(currentBreakingPos) || breakingFace == null) {
            breakingCenter = currentBreakingPos.toImmutable();
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
            client.worldRenderer.setBlockBreakingInfo(BREAKING_INFO_ID_BASE + index, breakingAnimationPositions.get(index), currentStage);
        }
    }

    private static void tickTrialChamberMarker(MinecraftClient client) {
        if (trialChamberMarkerRemainingTicks <= 0 || trialChamberMarkerPos == null) {
            clearTrialChamberMarker();
            return;
        }

        if (trialChamberMarkerDimensionId != null
                && !client.world.getRegistryKey().getValue().equals(trialChamberMarkerDimensionId)) {
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

    private static void clearBreakingAnimation(MinecraftClient client) {
        for (int index = 0; index < breakingAnimationPositions.size(); index++) {
            client.worldRenderer.setBlockBreakingInfo(BREAKING_INFO_ID_BASE + index, breakingAnimationPositions.get(index), -1);
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
        slamJumped = false;
        slamSneakPrimed = false;
        wasOnGround = false;
        wasSneaking = false;
        breakingCenter = null;
        breakingFace = null;
        outlinePositions.clear();
        breakingAnimationPositions.clear();
        lastBreakingStage = -1;
        clearTrialChamberMarker();
    }

    private static boolean shopsandtools$canUseAreaMining(MinecraftClient client) {
        return client.player != null
                && CelestiumShovelHelper.isCelestiumShovel(client.player.getMainHandStack())
                && CelestiumShovelHelper.isAreaMiningEnabled(client.player.getMainHandStack());
    }

    private static CelestiumShovelHelper.AreaMiningTargets shopsandtools$getAreaMiningTargets(MinecraftClient client, BlockPos centerPos, Direction face) {
        if (!shopsandtools$canUseAreaMining(client) || client.player == null || client.world == null || client.interactionManager == null) {
            return new CelestiumShovelHelper.AreaMiningTargets(List.of(), List.of(), 0.0F);
        }

        return CelestiumShovelHelper.getAreaMiningTargets(
                client.player,
                client.world,
                centerPos,
                face,
                client.interactionManager.getCurrentGameMode()
        );
    }
}
