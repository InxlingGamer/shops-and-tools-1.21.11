package net.inklinggamer.celestium;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.inklinggamer.celestium.client.CelestiumBootsClient;
import net.inklinggamer.celestium.client.CelestiumClientNetworking;
import net.inklinggamer.celestium.client.CelestiumFroglightIrisCompat;
import net.inklinggamer.celestium.client.CelestiumHoeClient;
import net.inklinggamer.celestium.client.CelestiumLeggingsClient;
import net.inklinggamer.celestium.client.CelestiumPickaxeClient;
import net.inklinggamer.celestium.client.CelestiumRageHud;
import net.inklinggamer.celestium.client.CelestiumShovelClient;
import net.inklinggamer.celestium.client.CelestiumSpearStunCooldownHud;
import net.inklinggamer.celestium.client.CelestiumThrustCooldownHud;
import net.inklinggamer.celestium.client.xray.CelestiumXrayController;

public class CelestiumClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        if (FabricLoader.getInstance().isModLoaded("iris")) {
            CelestiumFroglightIrisCompat.register();
        }

        CelestiumBootsClient.initialize();
        CelestiumLeggingsClient.initialize();
        CelestiumPickaxeClient.initialize();
        CelestiumShovelClient.initialize();
        CelestiumClientNetworking.registerReceivers();
        CelestiumXrayController.initialize();
        ClientTickEvents.END_CLIENT_TICK.register(CelestiumRageHud::tick);
        ClientTickEvents.END_CLIENT_TICK.register(CelestiumSpearStunCooldownHud::tick);
        ClientTickEvents.END_CLIENT_TICK.register(CelestiumThrustCooldownHud::tick);
        ClientTickEvents.END_CLIENT_TICK.register(CelestiumHoeClient::tick);
        ClientTickEvents.END_CLIENT_TICK.register(CelestiumPickaxeClient::tick);
        ClientTickEvents.END_CLIENT_TICK.register(CelestiumXrayController::tick);
        WorldRenderEvents.AFTER_ENTITIES.register(CelestiumHoeClient::render);
        WorldRenderEvents.AFTER_ENTITIES.register(CelestiumPickaxeClient::render);
        WorldRenderEvents.AFTER_ENTITIES.register(CelestiumShovelClient::render);
        WorldRenderEvents.AFTER_ENTITIES.register(CelestiumXrayController::render);
    }
}
