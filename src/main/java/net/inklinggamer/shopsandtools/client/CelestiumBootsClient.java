package net.inklinggamer.shopsandtools.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.inklinggamer.shopsandtools.network.SyncCelestiumSprintKeyPayload;
import net.inklinggamer.shopsandtools.player.CelestiumBootsManager;
import net.minecraft.client.MinecraftClient;

public final class CelestiumBootsClient {
    private static boolean hasObservedState;
    private static boolean observedSprintKeyHeld;
    private static boolean observedBootsEquipped;
    private static boolean pendingSync = true;

    private CelestiumBootsClient() {
    }

    public static void initialize() {
        ClientTickEvents.END_CLIENT_TICK.register(CelestiumBootsClient::tick);
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> resetTracking());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> resetTracking());
    }

    private static void tick(MinecraftClient client) {
        if (client.player == null || client.world == null) {
            clearServerSprintState();
            resetTracking();
            return;
        }

        boolean sprintKeyHeld = client.options.sprintKey.isPressed();
        boolean bootsEquipped = CelestiumBootsManager.isCelestiumBootsEquipped(client.player);
        if (!hasObservedState || sprintKeyHeld != observedSprintKeyHeld || bootsEquipped != observedBootsEquipped) {
            hasObservedState = true;
            observedSprintKeyHeld = sprintKeyHeld;
            observedBootsEquipped = bootsEquipped;
            pendingSync = true;
        }

        if (pendingSync && ClientPlayNetworking.canSend(SyncCelestiumSprintKeyPayload.ID)) {
            SyncCelestiumSprintKeyPayload.send(sprintKeyHeld);
            pendingSync = false;
        }
    }

    private static void clearServerSprintState() {
        if (hasObservedState && observedSprintKeyHeld && ClientPlayNetworking.canSend(SyncCelestiumSprintKeyPayload.ID)) {
            SyncCelestiumSprintKeyPayload.send(false);
        }
    }

    private static void resetTracking() {
        hasObservedState = false;
        observedSprintKeyHeld = false;
        observedBootsEquipped = false;
        pendingSync = true;
    }
}
