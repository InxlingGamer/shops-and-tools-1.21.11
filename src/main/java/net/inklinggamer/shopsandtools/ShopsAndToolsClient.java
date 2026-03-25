package net.inklinggamer.shopsandtools;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.inklinggamer.shopsandtools.client.CelestiumBootsClient;
import net.inklinggamer.shopsandtools.client.CelestiumHoeClient;
import net.inklinggamer.shopsandtools.client.CelestiumLeggingsClient;
import net.inklinggamer.shopsandtools.client.CelestiumPickaxeClient;
import net.inklinggamer.shopsandtools.client.CelestiumPickaxeOutlineIrisCompat;
import net.inklinggamer.shopsandtools.client.CelestiumRageHud;
import net.inklinggamer.shopsandtools.client.CelestiumShovelClient;
import net.inklinggamer.shopsandtools.client.CelestiumThrustCooldownHud;
import net.inklinggamer.shopsandtools.client.CelestiumTrialChamberMarkerIrisCompat;
import net.inklinggamer.shopsandtools.client.xray.CelestiumXrayController;
import net.inklinggamer.shopsandtools.client.xray.CelestiumXrayIrisCompat;
import net.inklinggamer.shopsandtools.network.SyncCelestiumRagePayload;
import net.inklinggamer.shopsandtools.network.SyncCelestiumTrialChamberMarkerPayload;
import net.inklinggamer.shopsandtools.network.SyncCelestiumThrustCooldownPayload;

public class ShopsAndToolsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        if (FabricLoader.getInstance().isModLoaded("iris")) {
            CelestiumPickaxeOutlineIrisCompat.register();
            CelestiumTrialChamberMarkerIrisCompat.register();
            CelestiumXrayIrisCompat.register();
        }

        CelestiumBootsClient.initialize();
        CelestiumLeggingsClient.initialize();
        CelestiumPickaxeClient.initialize();
        CelestiumShovelClient.initialize();
        SyncCelestiumRagePayload.registerClient();
        SyncCelestiumTrialChamberMarkerPayload.registerClient();
        SyncCelestiumThrustCooldownPayload.registerClient();
        CelestiumXrayController.initialize();
        ClientTickEvents.END_CLIENT_TICK.register(CelestiumRageHud::tick);
        ClientTickEvents.END_CLIENT_TICK.register(CelestiumThrustCooldownHud::tick);
        ClientTickEvents.END_CLIENT_TICK.register(CelestiumHoeClient::tick);
        ClientTickEvents.END_CLIENT_TICK.register(CelestiumPickaxeClient::tick);
        ClientTickEvents.END_CLIENT_TICK.register(CelestiumXrayController::tick);
        WorldRenderEvents.END_MAIN.register(CelestiumHoeClient::render);
        WorldRenderEvents.END_MAIN.register(CelestiumPickaxeClient::render);
        WorldRenderEvents.END_MAIN.register(CelestiumShovelClient::render);
        WorldRenderEvents.END_MAIN.register(CelestiumXrayController::render);
    }
}
