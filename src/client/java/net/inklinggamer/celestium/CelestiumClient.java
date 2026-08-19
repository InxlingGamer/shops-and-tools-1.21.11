package net.inklinggamer.celestium;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
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
import net.minecraft.resources.Identifier;

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
        HudElementRegistry.attachElementAfter(
                VanillaHudElements.HOTBAR,
                Identifier.fromNamespaceAndPath(Celestium.MOD_ID, "combat_status_bars"),
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
