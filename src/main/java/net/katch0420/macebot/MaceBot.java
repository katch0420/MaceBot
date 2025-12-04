package net.katch0420.macebot;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.katch0420.macebot.Commands.BotCommands;
import net.katch0420.macebot.Commands.PlayerCommands;
import net.katch0420.macebot.Commands.SkinCommands;
import net.katch0420.macebot.playerbot.PlayerBot;
import net.katch0420.macebot.utils.SkinManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MaceBot implements ModInitializer {
	public static final String MOD_ID = "MaceBot";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	@Override
	public void onInitialize() {
        ServerLifecycleEvents.SERVER_STOPPING.register(
                minecraftServer -> {
                    PlayerBot.botOnline = false;
        });
        LOGGER.info("Initializing Mod");

        SkinManager.init();
        LOGGER.info("Initialized Mod Utilities");

        BotCommands.Register();
        PlayerCommands.Register();
        SkinCommands.Register();
        LOGGER.info("Registered Commands");
	}

}
