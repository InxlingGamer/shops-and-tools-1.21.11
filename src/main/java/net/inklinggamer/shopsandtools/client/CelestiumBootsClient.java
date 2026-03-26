package net.inklinggamer.shopsandtools.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.inklinggamer.shopsandtools.network.SyncCelestiumWallClimbInputPayload;
import net.inklinggamer.shopsandtools.player.CelestiumBootsManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

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
    private static Direction lastWallClimbDirection;
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
            SyncCelestiumWallClimbInputPayload.send(sneakKeyHeld, forwardKeyHeld, backwardKeyHeld, leftKeyHeld, rightKeyHeld);
            pendingSync = false;
        }

        Direction wallDirection = resolveWallClimbDirection(client, bootsEquipped, sneakKeyHeld);
        applyLocalWallClimbMovement(client, wallDirection);
        tickWallClimbSound(client, wallDirection, forwardKeyHeld, backwardKeyHeld, leftKeyHeld, rightKeyHeld);
    }

    private static Direction resolveWallClimbDirection(MinecraftClient client, boolean bootsEquipped, boolean sneakKeyHeld) {
        if (!bootsEquipped || !sneakKeyHeld) {
            resetWallClimbState();
            return null;
        }

        Direction wallDirection = CelestiumBootsManager.resolveWallClimbDirection(
                client.player,
                lastWallClimbDirection,
                lastWallClimbDirection != null
        );
        if (wallDirection == null) {
            resetWallClimbState();
            return null;
        }

        lastWallClimbDirection = wallDirection;
        return wallDirection;
    }

    private static void applyLocalWallClimbMovement(MinecraftClient client, Direction wallDirection) {
        if (wallDirection == null) {
            return;
        }

        client.player.setVelocity(CelestiumBootsManager.getWallClimbVelocity(client.player, wallDirection));
        client.player.fallDistance = 0.0F;
    }

    private static void tickWallClimbSound(
            MinecraftClient client,
            Direction wallDirection,
            boolean forwardKeyHeld,
            boolean backwardKeyHeld,
            boolean leftKeyHeld,
            boolean rightKeyHeld
    ) {
        if (wallDirection == null) {
            return;
        }

        if (!CelestiumBootsManager.hasWallClimbMovementInput(forwardKeyHeld, backwardKeyHeld, leftKeyHeld, rightKeyHeld)) {
            clearWallClimbSoundProgress();
            return;
        }

        BlockPos soundPos = CelestiumBootsManager.resolveWallClimbSoundPos(client.player, wallDirection);
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
        lastWallClimbDirection = null;
        clearWallClimbSoundProgress();
    }
}
