package net.katch0420.macebot.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.katch0420.macebot.client.gui.ControlPanelScreen;
import net.katch0420.macebot.client.inputs.MaceBotKeyBinds;
import net.katch0420.macebot.main.MaceBot;
import net.katch0420.macebot.main.kits.client.gui.handled.KitEditorScreen;
import net.katch0420.macebot.main.kits.client.gui.handled.KitViewScreen;
import net.katch0420.macebot.main.networking.MaceBotNetworking;
import net.katch0420.macebot.main.networking.packets.c2s.ConfirmInstallC2SPacket;
import net.katch0420.macebot.main.settings.client.ClientSideSettings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.text.Text;

public class MaceBotClient implements ClientModInitializer{
    public MinecraftClient instance;

    public MinecraftClient getInstance(){
        return instance;
    }

    @Override
    public void onInitializeClient() {

        ClientLifecycleEvents.CLIENT_STARTED.register(minecraftClient -> {
            ClientSideSettings.setConnected(false);
            instance = minecraftClient;
        });
        ClientPlayConnectionEvents.DISCONNECT.register(
                (a,b)-> ClientSideSettings.setConnected(false)
        );
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (ControlPanelScreen.justClosed) {
                ControlPanelScreen.justClosed = false; // reset after one tick
                return;
            }
            if(!ClientSideSettings.isConnected() && client.player != null) client.player.sendMessage(Text.of("§eServer does not have macebot Installed"),false);
            while (MaceBotKeyBinds.openOptionsGui.wasPressed() && client.currentScreen == null) {
                client.setScreen(new ControlPanelScreen(Text.literal("MaceBot")));
            }
        });

        HandledScreens.register(
                KitEditorScreen.KIT_EDITOR_SCREEN_HANDLER,
                KitEditorScreen::new
        );

        HandledScreens.register(
                KitViewScreen.KIT_VIEW_SCREEN_HANDLER,
                KitViewScreen::new
        );

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) ->
                {
                    assert client.player != null;
                    ClientPlayNetworking.send(new ConfirmInstallC2SPacket(MaceBot.VERSION));
                }
        );
        MaceBotNetworking.registerS2CPackets();
        MaceBotKeyBinds.register();
    }
}
