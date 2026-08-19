package net.inklinggamer.shopsandtools.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.inklinggamer.shopsandtools.network.SyncCelestiumWallClimbInputPayload;
import net.inklinggamer.shopsandtools.player.CelestiumBootsManager;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.phys.Vec3;

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
    private static Vec3 syncedWallClimbVelocity = Vec3.ZERO;
    private static BlockPos lastWallClimbSoundPos;

    private CelestiumBootsClient() {
    }

    public static void initialize() {
        ClientTickEvents.END_CLIENT_TICK.register(CelestiumBootsClient::tick);
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> resetTracking());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> resetTracking());
    }

    private static void tick(Minecraft client) {
        if (client.player == null || client.level == null) {
            clearServerWallClimbInput();
            resetTracking();
            return;
        }

        boolean sneakKeyHeld = client.options.keyShift.isDown();
        boolean forwardKeyHeld = client.options.keyUp.isDown();
        boolean backwardKeyHeld = client.options.keyDown.isDown();
        boolean leftKeyHeld = client.options.keyLeft.isDown();
        boolean rightKeyHeld = client.options.keyRight.isDown();
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
            SyncCelestiumWallClimbInputPayload.send(sneakKeyHeld, forwardKeyHeld, backwardKeyHeld, leftKeyHeld, rightKeyHeld);
            pendingSync = false;
        }

        if (!bootsEquipped) {
            resetWallClimbState();
        }

        applyLocalWallClimbMovement(client);
        tickWallClimbSound(client, forwardKeyHeld, backwardKeyHeld, leftKeyHeld, rightKeyHeld);
    }

    public static void syncWallClimbState(boolean active, Direction wallDirection, Vec3 velocity) {
        if (!active || wallDirection == null) {
            resetWallClimbState();
            return;
        }

        wallClimbActive = true;
        syncedWallClimbDirection = wallDirection;
        syncedWallClimbVelocity = velocity;
    }

    public static boolean shouldUseSyncedWallClimb(Player player) {
        Minecraft client = Minecraft.getInstance();
        return client.player == player && wallClimbActive && syncedWallClimbDirection != null;
    }

    private static void applyLocalWallClimbMovement(Minecraft client) {
        if (!wallClimbActive || syncedWallClimbDirection == null) {
            return;
        }

        client.player.setDeltaMovement(syncedWallClimbVelocity);
        client.player.fallDistance = 0.0F;
    }

    private static void tickWallClimbSound(
            Minecraft client,
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

    private static void playLocalWallClimbStepSound(Minecraft client, BlockPos soundPos) {
        SoundType soundGroup = client.level.getBlockState(soundPos).getSoundType();
        if (soundGroup != null && soundGroup.getVolume() > 0.0F) {
            client.player.playSound(soundGroup.getStepSound(), Math.max(0.1F, soundGroup.getVolume() * WALL_CLIMB_SOUND_VOLUME_MULTIPLIER), soundGroup.getPitch());
        }
    }

    private static void clearServerWallClimbInput() {
        if (hasObservedState
                && (observedSneakKeyHeld || observedForwardKeyHeld || observedBackwardKeyHeld || observedLeftKeyHeld || observedRightKeyHeld)
                && ClientPlayNetworking.canSend(SyncCelestiumWallClimbInputPayload.ID)) {
            SyncCelestiumWallClimbInputPayload.send(false, false, false, false, false);
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
        syncedWallClimbVelocity = Vec3.ZERO;
        clearWallClimbSoundProgress();
    }
}
