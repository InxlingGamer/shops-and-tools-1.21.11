package net.inklinggamer.shopsandtools;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.inklinggamer.shopsandtools.client.CelestiumLeggingsClient;
import net.inklinggamer.shopsandtools.client.xray.CelestiumXrayController;
import net.inklinggamer.shopsandtools.client.xray.CelestiumXrayIrisCompat;

public class ShopsAndToolsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        if (FabricLoader.getInstance().isModLoaded("iris")) {
            CelestiumXrayIrisCompat.register();
        }

        CelestiumLeggingsClient.initialize();
        CelestiumXrayController.initialize();
        ClientTickEvents.END_CLIENT_TICK.register(CelestiumXrayController::tick);
        WorldRenderEvents.END_MAIN.register(CelestiumXrayController::render);
    }
}
