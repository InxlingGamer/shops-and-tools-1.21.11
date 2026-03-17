package net.inklinggamer.shopsandtools;

import net.fabricmc.api.ModInitializer;

import net.inklinggamer.shopsandtools.block.ModBlocks;
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
	}
}