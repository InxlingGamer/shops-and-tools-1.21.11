package net.inklinggamer.celestium;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.inklinggamer.celestium.advancement.CelestiumAdvancementHelper;
import net.inklinggamer.celestium.advancement.ModAdvancementActions;
import net.inklinggamer.celestium.advancement.ModAdvancementCriteria;
import net.inklinggamer.celestium.block.ModBlocks;
import net.inklinggamer.celestium.entity.WardenBossBarManager;
import net.inklinggamer.celestium.entity.WardenCombatManager;
import net.inklinggamer.celestium.item.CelestiumChestItem;
import net.inklinggamer.celestium.item.ModItemGroups;
import net.inklinggamer.celestium.item.ModItems;
import net.inklinggamer.celestium.migration.LegacyContentBridge;
import net.inklinggamer.celestium.network.OpenCelestiumCraftingPayload;
import net.inklinggamer.celestium.network.ReturnToInventoryPayload;
import net.inklinggamer.celestium.network.ArmCelestiumShovelSlamPayload;
import net.inklinggamer.celestium.network.SyncCelestiumRagePayload;
import net.inklinggamer.celestium.network.SyncCelestiumSpearStunCooldownPayload;
import net.inklinggamer.celestium.network.SyncCelestiumTrialChamberMarkerPayload;
import net.inklinggamer.celestium.network.SyncCelestiumWallClimbInputPayload;
import net.inklinggamer.celestium.network.SyncCelestiumWallClimbStatePayload;
import net.inklinggamer.celestium.network.SyncCelestiumThrustCooldownPayload;
import net.inklinggamer.celestium.network.ToggleCelestiumPickaxeAreaModePayload;
import net.inklinggamer.celestium.network.ToggleCelestiumPickaxeEnchantModePayload;
import net.inklinggamer.celestium.network.ToggleCelestiumShovelAreaModePayload;
import net.inklinggamer.celestium.player.CelestiumBootsManager;
import net.inklinggamer.celestium.player.CelestiumExperienceManager;
import net.inklinggamer.celestium.player.CelestiumHoeManager;
import net.inklinggamer.celestium.player.CelestiumLeggingsManager;
import net.inklinggamer.celestium.player.CelestiumPickaxeManager;
import net.inklinggamer.celestium.player.CelestiumShovelManager;
import net.inklinggamer.celestium.player.CelestiumSpearManager;
import net.inklinggamer.celestium.player.CelestiumSwordManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Celestium implements ModInitializer {
	public static final String MOD_ID = "celestium";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModAdvancementCriteria.register();
		ModItems.registerModItems();
		ModItemGroups.registerItemGroups();
		ModBlocks.registerModBlocks();
		LegacyContentBridge.register();
		WardenCombatManager.register();
		WardenBossBarManager.register();
		OpenCelestiumCraftingPayload.register();
		ReturnToInventoryPayload.register();
		SyncCelestiumWallClimbInputPayload.register();
		SyncCelestiumWallClimbStatePayload.register();
		SyncCelestiumSpearStunCooldownPayload.register();
		SyncCelestiumThrustCooldownPayload.register();
		SyncCelestiumRagePayload.register();
		SyncCelestiumTrialChamberMarkerPayload.register();
		ToggleCelestiumPickaxeAreaModePayload.register();
		ToggleCelestiumPickaxeEnchantModePayload.register();
		ToggleCelestiumShovelAreaModePayload.register();
		ArmCelestiumShovelSlamPayload.register();
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			LegacyContentBridge.migratePlayer(handler.player);
			ModAdvancementActions.triggerPlayerJoined(handler.player);
		});
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			LegacyContentBridge.tickServer(server);
			CelestiumBootsManager.tickServer(server);
			CelestiumExperienceManager.tickServer(server);
			CelestiumHoeManager.tickServer(server);
			CelestiumLeggingsManager.tickServer(server);
			CelestiumPickaxeManager.tickServer(server);
			CelestiumShovelManager.tickServer(server);
			CelestiumSpearManager.tickServer(server);
			CelestiumSwordManager.tickServer(server);
			WardenBossBarManager.tickServer(server);
			server.getPlayerList().getPlayers().forEach(player -> {
				CelestiumBootsManager.tickPlayer(player);
				CelestiumChestItem.tickPlayer(player);
				CelestiumExperienceManager.tickPlayer(player);
				CelestiumHoeManager.tickPlayer(player);
				CelestiumLeggingsManager.tickPlayer(player);
				CelestiumShovelManager.tickPlayer(player);
				CelestiumSpearManager.tickPlayer(player);
				CelestiumSwordManager.tickPlayer(player);
				if (CelestiumAdvancementHelper.isFullyAscended(player)) {
					ModAdvancementActions.triggerFullyAscended(player);
				}
			});
		});
	}
}
