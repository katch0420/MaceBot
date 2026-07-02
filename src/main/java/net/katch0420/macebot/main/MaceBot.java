package net.katch0420.macebot.main;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.katch0420.macebot.client.gui.bodies.KitEditorBody;
import net.katch0420.macebot.main.kits.server.CustomKitManager;
import net.katch0420.macebot.main.kits.server.BuiltInKitLoader;
import net.katch0420.macebot.main.kits.server.KitRegistry;
import net.katch0420.macebot.main.macebot.bot.PlayerBot;
import net.katch0420.macebot.main.networking.MaceBotNetworking;
import net.katch0420.macebot.main.commands.BotCommands;
import net.katch0420.macebot.main.commands.PlayerCommands;
import net.katch0420.macebot.main.settings.server.SettingsSyncHelper;
import net.katch0420.macebot.main.utils.PlayerBuffs;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MaceBot implements ModInitializer {

    public static boolean debugMode = true;

	public static final String MOD_ID = "macebot";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final ModContainer CONTAINER = FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow(() -> new IllegalStateException("Mod not found!"));
    public static final String VERSION = CONTAINER.getMetadata().getVersion().getFriendlyString();

    public static MinecraftServer server = null;
	@Override
	public void onInitialize() {
        MaceBotNetworking.registerPackets();
        MaceBotNetworking.registerC2SPackets();

        ServerLifecycleEvents.SERVER_STARTING.register(
                minecraftServer -> {
                    server = minecraftServer;
                    KitRegistry.registerTickEvent();
                    BuiltInKitLoader.registerAll();
                    CustomKitManager.init();
                    CustomKitManager.loadAll().forEach(KitRegistry::register);
                }
        );

        ServerTickEvents.END_SERVER_TICK.register(MaceBot::tick
        );

        ServerPlayConnectionEvents.JOIN.register(
                (a,b,c) -> {
                    KitRegistry.syncAllKitsToClients();
                    SettingsSyncHelper.resyncSettingsForAll();
                }
        );

        ServerLifecycleEvents.SERVER_STOPPING.register(
                minecraftServer -> {
                    PlayerBot.disconnect();
                });

        LOGGER.info("Initializing Mod");

        LOGGER.info("Initialized Mod Utilities");

        BotCommands.Register();
        PlayerCommands.Register();
        LOGGER.info("Registered Commands");
	}
    public static void tick(MinecraftServer s){
        PlayerBuffs.tick(s);
    }
}
