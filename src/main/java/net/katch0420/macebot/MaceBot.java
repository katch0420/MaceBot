package net.katch0420.macebot;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MaceBot implements ModInitializer {
	public static final String MOD_ID = "MaceBot";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	@Override
	public void onInitialize() {
        LOGGER.info("[MaceBot] Initializing Mod:");
        MaceBotCommands.Register();
        LOGGER.info("[MaceBot] Registered Commands");
	}
}
