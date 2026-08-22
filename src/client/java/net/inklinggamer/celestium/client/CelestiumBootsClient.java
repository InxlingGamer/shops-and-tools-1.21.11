package net.inklinggamer.celestium.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.inklinggamer.celestium.network.SyncCelestiumWallClimbInputPayload;
import net.inklinggamer.celestium.player.CelestiumBootsManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public final class CelestiumBootsClient {
    private static final float WALL_CLIMB_SOUND_VOLUME_MULTIPLIER = 1.15F;
    private static boolean hasObservedState;
    private static boolean observedSneakKeyHeld;
    private static boolean observedForwardKeyHeld;
    private static boolean observedBackwardKeyHeld;
    private static boolean observedLeftKeyHeld;
    private static boolean observedRightKeyHeld;
    private static boolean observedBootsEquipped;
    private static boolean pendingSync = true;
    private static boolean wallClimbActive;
    private static Direction syncedWallClimbDirection;
    private static Vec3d syncedWallClimbVelocity = Vec3d.ZERO;
    private static BlockPos lastWallClimbSoundPos;

    private CelestiumBootsClient() {
    }

    public static void initialize() {
        ClientTickEvents.END_CLIENT_TICK.register(CelestiumBootsClient::tick);
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> resetTracking());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> resetTracking());
    }

    private static void tick(MinecraftClient client) {
        if (client.player == null || client.world == null) {
            clearServerWallClimbInput();
            resetTracking();
            return;
        }

        boolean sneakKeyHeld = client.options.sneakKey.isPressed();
        boolean forwardKeyHeld = client.options.forwardKey.isPressed();
        boolean backwardKeyHeld = client.options.backKey.isPressed();
        boolean leftKeyHeld = client.options.leftKey.isPressed();
        boolean rightKeyHeld = client.options.rightKey.isPressed();
        boolean bootsEquipped = CelestiumBootsManager.isCelestiumBootsEquipped(client.player);
        if (!hasObservedState
                || sneakKeyHeld != observedSneakKeyHeld
                || forwardKeyHeld != observedForwardKeyHeld
                || backwardKeyHeld != observedBackwardKeyHeld
                || leftKeyHeld != observedLeftKeyHeld
                || rightKeyHeld != observedRightKeyHeld
                || bootsEquipped != observedBootsEquipped) {
            hasObservedState = true;
            observedSneakKeyHeld = sneakKeyHeld;
            observedForwardKeyHeld = forwardKeyHeld;
            observedBackwardKeyHeld = backwardKeyHeld;
            observedLeftKeyHeld = leftKeyHeld;
            observedRightKeyHeld = rightKeyHeld;
            observedBootsEquipped = bootsEquipped;
            pendingSync = true;
        }

        if (pendingSync && ClientPlayNetworking.canSend(SyncCelestiumWallClimbInputPayload.ID)) {
            CelestiumClientNetworking.send(new SyncCelestiumWallClimbInputPayload(
                    sneakKeyHeld, forwardKeyHeld, backwardKeyHeld, leftKeyHeld, rightKeyHeld));
            pendingSync = false;
        }

        if (!bootsEquipped) {
            resetWallClimbState();
        }

        applyLocalWallClimbMovement(client);
        tickWallClimbSound(client, forwardKeyHeld, backwardKeyHeld, leftKeyHeld, rightKeyHeld);
    }

    public static void syncWallClimbState(boolean active, Direction wallDirection, Vec3d velocity) {
        if (!active || wallDirection == null) {
            resetWallClimbState();
            return;
        }

        wallClimbActive = true;
        syncedWallClimbDirection = wallDirection;
        syncedWallClimbVelocity = velocity;
    }

    public static boolean shouldUseSyncedWallClimb(PlayerEntity player) {
        MinecraftClient client = MinecraftClient.getInstance();
        return client.player == player && wallClimbActive && syncedWallClimbDirection != null;
    }

    private static void applyLocalWallClimbMovement(MinecraftClient client) {
        if (!wallClimbActive || syncedWallClimbDirection == null) {
            return;
        }

        client.player.setVelocity(syncedWallClimbVelocity);
        client.player.fallDistance = 0.0F;
    }

    private static void tickWallClimbSound(
            MinecraftClient client,
            boolean forwardKeyHeld,
            boolean backwardKeyHeld,
            boolean leftKeyHeld,
            boolean rightKeyHeld
    ) {
        if (!wallClimbActive || syncedWallClimbDirection == null) {
            clearWallClimbSoundProgress();
            return;
        }

        if (!CelestiumBootsManager.hasWallClimbMovementInput(forwardKeyHeld, backwardKeyHeld, leftKeyHeld, rightKeyHeld)) {
            clearWallClimbSoundProgress();
            return;
        }

        BlockPos soundPos = CelestiumBootsManager.resolveWallClimbSoundPos(client.player, syncedWallClimbDirection);
        CelestiumBootsManager.WallClimbSoundTransition transition =
                CelestiumBootsManager.evaluateWallClimbSoundTransition(lastWallClimbSoundPos, soundPos);
        lastWallClimbSoundPos = transition.trackedSoundPos();
        if (!transition.shouldPlaySound() || soundPos == null) {
            return;
        }

        playLocalWallClimbStepSound(client, soundPos);
    }

    private static void playLocalWallClimbStepSound(MinecraftClient client, BlockPos soundPos) {
        BlockSoundGroup soundGroup = client.world.getBlockState(soundPos).getSoundGroup();
        if (soundGroup != null && soundGroup.getVolume() > 0.0F) {
            client.player.playSound(soundGroup.getStepSound(), Math.max(0.1F, soundGroup.getVolume() * WALL_CLIMB_SOUND_VOLUME_MULTIPLIER), soundGroup.getPitch());
        }
    }

    private static void clearServerWallClimbInput() {
        if (hasObservedState
                && (observedSneakKeyHeld || observedForwardKeyHeld || observedBackwardKeyHeld || observedLeftKeyHeld || observedRightKeyHeld)
                && ClientPlayNetworking.canSend(SyncCelestiumWallClimbInputPayload.ID)) {
            CelestiumClientNetworking.send(new SyncCelestiumWallClimbInputPayload(false, false, false, false, false));
        }
    }

    private static void resetTracking() {
        hasObservedState = false;
        observedSneakKeyHeld = false;
        observedForwardKeyHeld = false;
        observedBackwardKeyHeld = false;
        observedLeftKeyHeld = false;
        observedRightKeyHeld = false;
        observedBootsEquipped = false;
        pendingSync = true;
        resetWallClimbState();
    }

    private static void clearWallClimbSoundProgress() {
        lastWallClimbSoundPos = null;
    }

    private static void resetWallClimbState() {
        wallClimbActive = false;
        syncedWallClimbDirection = null;
        syncedWallClimbVelocity = Vec3d.ZERO;
        clearWallClimbSoundProgress();
    }
}
