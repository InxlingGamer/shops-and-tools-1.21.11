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
    private static final int WALL_CLIMB_SOUND_INTERVAL_TICKS = 4;
    private static final float WALL_CLIMB_SOUND_VOLUME_MULTIPLIER = 1.15F;
    private static boolean hasObservedState;
    private static boolean observedSneakKeyHeld;
    private static boolean observedForwardKeyHeld;
    private static boolean observedBackwardKeyHeld;
    private static boolean observedLeftKeyHeld;
    private static boolean observedRightKeyHeld;
    private static boolean observedBootsEquipped;
    private static boolean pendingSync = true;
    private static long lastWallClimbSoundTick = Long.MIN_VALUE;
    private static Direction lastWallClimbSoundDirection;

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

        tickWallClimbSound(client, bootsEquipped, sneakKeyHeld, forwardKeyHeld, backwardKeyHeld, leftKeyHeld, rightKeyHeld);
    }

    private static void tickWallClimbSound(
            MinecraftClient client,
            boolean bootsEquipped,
            boolean sneakKeyHeld,
            boolean forwardKeyHeld,
            boolean backwardKeyHeld,
            boolean leftKeyHeld,
            boolean rightKeyHeld
    ) {
        if (!bootsEquipped
                || !sneakKeyHeld
                || !CelestiumBootsManager.hasWallClimbMovementInput(forwardKeyHeld, backwardKeyHeld, leftKeyHeld, rightKeyHeld)
                || !(client.player.isClimbing() || CelestiumBootsManager.shouldWallClimb(client.player))) {
            resetWallClimbSoundState();
            return;
        }

        Direction wallDirection = CelestiumBootsManager.resolveWallClimbDirection(
                client.player,
                lastWallClimbSoundDirection,
                lastWallClimbSoundDirection != null && sneakKeyHeld
        );
        if (wallDirection == null) {
            resetWallClimbSoundState();
            return;
        }

        lastWallClimbSoundDirection = wallDirection;

        long worldTime = client.world.getTime();
        if (worldTime - lastWallClimbSoundTick < WALL_CLIMB_SOUND_INTERVAL_TICKS) {
            return;
        }

        playLocalWallClimbStepSound(client, wallDirection);
        lastWallClimbSoundTick = worldTime;
    }

    private static void playLocalWallClimbStepSound(MinecraftClient client, Direction wallDirection) {
        BlockPos soundPos = CelestiumBootsManager.resolveWallClimbSoundPos(client.player, wallDirection);
        if (soundPos == null) {
            soundPos = client.player.getBlockPos();
        }

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
        resetWallClimbSoundState();
    }

    private static void resetWallClimbSoundState() {
        lastWallClimbSoundTick = Long.MIN_VALUE;
        lastWallClimbSoundDirection = null;
    }
}
