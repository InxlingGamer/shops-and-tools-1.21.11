package net.inklinggamer.shopsandtools;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.inklinggamer.shopsandtools.client.xray.CelestiumXrayController;

public class ShopsAndToolsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        CelestiumXrayController.initialize();
        ClientTickEvents.END_CLIENT_TICK.register(CelestiumXrayController::tick);
        WorldRenderEvents.END_MAIN.register(CelestiumXrayController::render);
    }
}
