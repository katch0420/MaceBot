package net.katch0420.macebot.main;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.katch0420.macebot.main.commands.MaceBotCommands;
import net.katch0420.macebot.main.kits.server.CustomKitManager;
import net.katch0420.macebot.main.kits.server.BuiltInKitLoader;
import net.katch0420.macebot.main.kits.server.KitRegistry;
import net.katch0420.macebot.main.macebot.ai.Mode;
import net.katch0420.macebot.main.macebot.ai.PracticeMode;
import net.katch0420.macebot.main.macebot.bot.PlayerBot;
import net.katch0420.macebot.main.network.C2SPacketManager;
import net.katch0420.macebot.main.network.MainPacketManager;
import net.katch0420.macebot.main.settings.Env;
import net.katch0420.macebot.main.settings.SettingManager;
import net.katch0420.macebot.main.utils.PlayerBuffs;
// MOJANG-VERIFY: Yarn's ConstantArgumentSerializer -> guessing Mojang's SingletonArgumentInfo,
// and its .of(supplier) -> .contextFree(supplier). This is a well-known rename pattern for
// argument-type serializers, but double check against your actual mapped sources.
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.client.server.IntegratedServer;

public class MaceBot implements ModInitializer {

    public static boolean debugMode = true;

    public static final String MOD_ID = "macebot";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final ModContainer CONTAINER = FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow(() -> new IllegalStateException("Mod not found!"));
    public static final String VERSION = CONTAINER.getMetadata().getVersion().getFriendlyString();
    private static boolean debug = true;
    public static MinecraftServer server = null;
    @Override
    public void onInitialize() {
        MainPacketManager.registerPackets();
        C2SPacketManager.registerC2SReceivers();

        ServerLifecycleEvents.SERVER_STARTING.register(
                minecraftServer -> {
                    server = minecraftServer;
                    MaceBotCommands.Register(server.getCommands());
                    KitRegistry.registerTickEvent();
                    BuiltInKitLoader.registerAll();
                    CustomKitManager.init();
                    CustomKitManager.loadAll().forEach(KitRegistry::register);
                    if(server instanceof DedicatedServer) Env.updateEnv(Env.SERVER);
                    if(server instanceof IntegratedServer) Env.updateEnv(Env.COMMON);
                }
        );
        ServerTickEvents.END_SERVER_TICK.register(MaceBot::tick
        );
        ServerPlayConnectionEvents.JOIN.register(
                (a,b,c) -> {
                    KitRegistry.syncAllKitsToClients();
                    SettingManager.resyncForAll();
                }
        );

        ServerLifecycleEvents.SERVER_STOPPING.register(
                minecraftServer -> {
                    PlayerBot.kick();
                    Env.updateEnv(Env.CLIENT);
                });
        registerArguments();
    }

    private void registerArguments() {
        ArgumentTypeRegistry.registerArgumentType(
                Identifier.fromNamespaceAndPath("macebot", "mode"),
                Mode.ModeArgumentType.class,
                SingletonArgumentInfo.contextFree(Mode.ModeArgumentType::new));

        ArgumentTypeRegistry.registerArgumentType(
                Identifier.fromNamespaceAndPath("macebot", "practice_mode"),
                PracticeMode.PracticeModeArgumentType.class,
                SingletonArgumentInfo.contextFree(PracticeMode.PracticeModeArgumentType::new));

    }

    public static void debugLog(String str){
        if(debug) LOGGER.warn("Debug: {}", str);
    }

    public static void tick(MinecraftServer s){
        PlayerBuffs.tick(s);
    }
}