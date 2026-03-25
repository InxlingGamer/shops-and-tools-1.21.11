package net.inklinggamer.shopsandtools;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.inklinggamer.shopsandtools.block.ModBlocks;
import net.inklinggamer.shopsandtools.item.CelestiumChestItem;
import net.inklinggamer.shopsandtools.item.ModItemGroups;
import net.inklinggamer.shopsandtools.item.ModItems;
import net.inklinggamer.shopsandtools.network.OpenCelestiumCraftingPayload;
import net.inklinggamer.shopsandtools.network.ReturnToInventoryPayload;
import net.inklinggamer.shopsandtools.network.ArmCelestiumShovelSlamPayload;
import net.inklinggamer.shopsandtools.network.SyncCelestiumRagePayload;
import net.inklinggamer.shopsandtools.network.SyncCelestiumTrialChamberMarkerPayload;
import net.inklinggamer.shopsandtools.network.SyncCelestiumWallClimbInputPayload;
import net.inklinggamer.shopsandtools.network.SyncCelestiumThrustCooldownPayload;
import net.inklinggamer.shopsandtools.network.ToggleCelestiumPickaxeAreaModePayload;
import net.inklinggamer.shopsandtools.network.ToggleCelestiumPickaxeEnchantModePayload;
import net.inklinggamer.shopsandtools.network.ToggleCelestiumShovelAreaModePayload;
import net.inklinggamer.shopsandtools.player.CelestiumBootsManager;
import net.inklinggamer.shopsandtools.player.CelestiumExperienceManager;
import net.inklinggamer.shopsandtools.player.CelestiumLeggingsManager;
import net.inklinggamer.shopsandtools.player.CelestiumPickaxeManager;
import net.inklinggamer.shopsandtools.player.CelestiumShovelManager;
import net.inklinggamer.shopsandtools.player.CelestiumSwordManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShopsAndTools implements ModInitializer {
	public static final String MOD_ID = "shopsandtools";
	
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModItems.registerModItems();
		ModItemGroups.registerItemGroups();
		ModBlocks.registerModBlocks();
		OpenCelestiumCraftingPayload.register();
		ReturnToInventoryPayload.register();
		SyncCelestiumWallClimbInputPayload.register();
		SyncCelestiumThrustCooldownPayload.register();
		SyncCelestiumRagePayload.register();
		SyncCelestiumTrialChamberMarkerPayload.register();
		ToggleCelestiumPickaxeAreaModePayload.register();
		ToggleCelestiumPickaxeEnchantModePayload.register();
		ToggleCelestiumShovelAreaModePayload.register();
		ArmCelestiumShovelSlamPayload.register();
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			CelestiumBootsManager.tickServer(server);
			CelestiumExperienceManager.tickServer(server);
			CelestiumLeggingsManager.tickServer(server);
			CelestiumPickaxeManager.tickServer(server);
			CelestiumShovelManager.tickServer(server);
			CelestiumSwordManager.tickServer(server);
			server.getPlayerManager().getPlayerList().forEach(player -> {
				CelestiumBootsManager.tickPlayer(player);
				CelestiumChestItem.tickPlayer(player);
				CelestiumExperienceManager.tickPlayer(player);
				CelestiumLeggingsManager.tickPlayer(player);
				CelestiumShovelManager.tickPlayer(player);
				CelestiumSwordManager.tickPlayer(player);
			});
		});
	}
}
