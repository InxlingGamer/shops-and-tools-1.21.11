package net.inklinggamer.shopsandtools;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.loader.api.FabricLoader;
import net.inklinggamer.shopsandtools.client.CelestiumBootsClient;
import net.inklinggamer.shopsandtools.client.CelestiumFroglightIrisCompat;
import net.inklinggamer.shopsandtools.client.CelestiumHoeClient;
import net.inklinggamer.shopsandtools.client.CelestiumLeggingsClient;
import net.inklinggamer.shopsandtools.client.CelestiumPickaxeClient;
import net.inklinggamer.shopsandtools.client.CelestiumRageHud;
import net.inklinggamer.shopsandtools.client.CelestiumShovelClient;
import net.inklinggamer.shopsandtools.client.CelestiumSpearStunCooldownHud;
import net.inklinggamer.shopsandtools.client.CelestiumThrustCooldownHud;
import net.inklinggamer.shopsandtools.client.xray.CelestiumXrayController;
import net.inklinggamer.shopsandtools.network.SyncCelestiumRagePayload;
import net.inklinggamer.shopsandtools.network.SyncCelestiumSpearStunCooldownPayload;
import net.inklinggamer.shopsandtools.network.SyncCelestiumTrialChamberMarkerPayload;
import net.inklinggamer.shopsandtools.network.SyncCelestiumThrustCooldownPayload;
import net.inklinggamer.shopsandtools.network.SyncCelestiumWallClimbStatePayload;
import net.minecraft.resources.Identifier;

public class ShopsAndToolsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        if (FabricLoader.getInstance().isModLoaded("iris")) {
            CelestiumFroglightIrisCompat.register();
        }

        CelestiumBootsClient.initialize();
        CelestiumLeggingsClient.initialize();
        CelestiumPickaxeClient.initialize();
        CelestiumShovelClient.initialize();
        SyncCelestiumRagePayload.registerClient();
        SyncCelestiumSpearStunCooldownPayload.registerClient();
        SyncCelestiumTrialChamberMarkerPayload.registerClient();
        SyncCelestiumThrustCooldownPayload.registerClient();
        SyncCelestiumWallClimbStatePayload.registerClient();
        CelestiumXrayController.initialize();
        ClientTickEvents.END_CLIENT_TICK.register(CelestiumRageHud::tick);
        ClientTickEvents.END_CLIENT_TICK.register(CelestiumSpearStunCooldownHud::tick);
        ClientTickEvents.END_CLIENT_TICK.register(CelestiumThrustCooldownHud::tick);
        ClientTickEvents.END_CLIENT_TICK.register(CelestiumHoeClient::tick);
        ClientTickEvents.END_CLIENT_TICK.register(CelestiumPickaxeClient::tick);
        ClientTickEvents.END_CLIENT_TICK.register(CelestiumXrayController::tick);
        HudElementRegistry.attachElementAfter(
                VanillaHudElements.HOTBAR,
                Identifier.fromNamespaceAndPath(ShopsAndTools.MOD_ID, "combat_status_bars"),
                (graphics, deltaTracker) -> {
                    if (net.minecraft.client.Minecraft.getInstance().player == null) {
                        return;
                    }

                    var player = net.minecraft.client.Minecraft.getInstance().player;
                    if (CelestiumThrustCooldownHud.isActive()) {
                        CelestiumThrustCooldownHud.renderNearHotbar(graphics, player);
                    }
                    if (CelestiumSpearStunCooldownHud.isActive()) {
                        CelestiumSpearStunCooldownHud.renderNearHotbar(graphics, player);
                    }
                    CelestiumRageHud.renderNearHotbar(graphics, player);
                }
        );
        LevelRenderEvents.END_MAIN.register(CelestiumHoeClient::render);
        LevelRenderEvents.END_MAIN.register(CelestiumPickaxeClient::render);
        LevelRenderEvents.END_MAIN.register(CelestiumShovelClient::render);
        LevelRenderEvents.END_MAIN.register(CelestiumXrayController::render);
    }
}
