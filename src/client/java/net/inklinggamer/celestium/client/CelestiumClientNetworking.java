package net.inklinggamer.celestium.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.inklinggamer.celestium.network.SyncCelestiumRagePayload;
import net.inklinggamer.celestium.network.SyncCelestiumSpearStunCooldownPayload;
import net.inklinggamer.celestium.network.SyncCelestiumThrustCooldownPayload;
import net.inklinggamer.celestium.network.SyncCelestiumTrialChamberMarkerPayload;
import net.inklinggamer.celestium.network.SyncCelestiumWallClimbStatePayload;
import net.minecraft.network.packet.CustomPayload;

public final class CelestiumClientNetworking {
    private CelestiumClientNetworking() {
    }

    public static void registerReceivers() {
        ClientPlayNetworking.registerGlobalReceiver(SyncCelestiumRagePayload.ID, (payload, context) ->
                context.client().execute(() -> CelestiumRageHud.syncStacks(payload.stacks())));
        ClientPlayNetworking.registerGlobalReceiver(SyncCelestiumSpearStunCooldownPayload.ID, (payload, context) ->
                context.client().execute(() -> CelestiumSpearStunCooldownHud.syncCooldown(payload.remainingTicks())));
        ClientPlayNetworking.registerGlobalReceiver(SyncCelestiumThrustCooldownPayload.ID, (payload, context) ->
                context.client().execute(() -> CelestiumThrustCooldownHud.syncCooldown(payload.remainingTicks())));
        ClientPlayNetworking.registerGlobalReceiver(SyncCelestiumTrialChamberMarkerPayload.ID, (payload, context) ->
                context.client().execute(() -> CelestiumShovelClient.syncTrialChamberMarker(
                        payload.pos(), payload.dimensionId(), payload.durationTicks())));
        ClientPlayNetworking.registerGlobalReceiver(SyncCelestiumWallClimbStatePayload.ID, (payload, context) ->
                context.client().execute(() -> CelestiumBootsClient.syncWallClimbState(
                        payload.active(), payload.wallDirection(), payload.velocity())));
    }

    public static void send(CustomPayload payload) {
        ClientPlayNetworking.send(payload);
    }
}
