package net.katch0420.macebot.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.katch0420.macebot.client.gui.frames.MainFrame;
import net.katch0420.macebot.client.gui.themes.Theme;
import net.katch0420.macebot.client.gui.themes.Themes;
import net.katch0420.macebot.client.inputs.MaceBotKeyBinds;
import net.katch0420.macebot.main.MaceBot;
import net.katch0420.macebot.main.kits.client.data.ClientKitRegistry;
import net.katch0420.macebot.main.kits.client.data.ClientPresetRegistry;
import net.katch0420.macebot.main.messenger.ClientChatMessageManager;
import net.katch0420.macebot.main.messenger.Messages;
import net.katch0420.macebot.main.network.S2CPacketManager;
import net.katch0420.macebot.main.network.packets.c2s.ConfirmInstallC2SPacket;
import net.katch0420.macebot.main.settings.Settings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public class MaceBotClient implements ClientModInitializer {

    public static MaceBotClient instance;

    public static MaceBotClient getInstance(){
        return instance;
    }

    public static LocalPlayer getClientPlayer(){
        return Minecraft.getInstance().player;
    }

    public static MainFrame mainFrame;

    public static Theme theme = Themes.CURRENT;

    public static String SERVER_SIDE_VERSION = "";
    private static int joinCheckDelay = -1;
    static boolean forcePermissionCheck = false;
    @Override
    public void onInitializeClient() {

        instance = this;

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            ClientPlayNetworking.send(new ConfirmInstallC2SPacket(MaceBot.VERSION));
            ClientPresetRegistry.load();
            joinCheckDelay = 5; // wait 5 ticks before trusting permission state
        });

        ClientLifecycleEvents.CLIENT_STARTED.register(
                c -> mainFrame = new MainFrame() //Creates Gui Frame
        );

        ClientPlayConnectionEvents.DISCONNECT.register((listener, client)->{
            forcePermissionCheck = false;
            Settings.CLIENT.update(false);
            mainFrame = new MainFrame();
            joinCheckDelay = -1;

        });

        ClientTickEvents.START_CLIENT_TICK.register(c -> {
            MaceBotKeyBinds.processKeybinds();
            ClientKitRegistry.tick();

            if (joinCheckDelay > 0 || forcePermissionCheck) {
                joinCheckDelay--;
                if (joinCheckDelay == 0 || forcePermissionCheck) {
                    if(Minecraft.getInstance().player != null) {
                        if (!(Settings.hasLevel3Permission(Minecraft.getInstance().player))) {
                            ClientChatMessageManager.info(Messages.I_CLIENT_DISABLED_ON_JOIN_NON_OP);
                            Settings.CLIENT.update(false);
                            forcePermissionCheck = false;
                        }
                    } else forcePermissionCheck = true;
                }
            }
        });

        S2CPacketManager.registerS2CReceivers();
        MaceBotKeyBinds.register();
    }
}
