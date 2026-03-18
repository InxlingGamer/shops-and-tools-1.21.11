package net.inklinggamer.shopsandtools;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.inklinggamer.shopsandtools.block.ModBlocks;
import net.inklinggamer.shopsandtools.item.CelestiumChestItem;
import net.inklinggamer.shopsandtools.item.ModItemGroups;
import net.inklinggamer.shopsandtools.item.ModItems;
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
		ServerTickEvents.END_SERVER_TICK.register(server ->
				server.getPlayerManager().getPlayerList().forEach(CelestiumChestItem::tickPlayer)
		);
	}
}
