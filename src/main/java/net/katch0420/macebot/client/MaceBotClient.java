package net.katch0420.macebot.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.katch0420.macebot.client.gui.bodies.KitEditorBody;
import net.katch0420.macebot.client.gui.frames.MainFrame;
import net.katch0420.macebot.client.gui.themes.Theme;
import net.katch0420.macebot.client.gui.themes.Themes;
import net.katch0420.macebot.client.inputs.MaceBotKeyBinds;
import net.katch0420.macebot.main.MaceBot;
import net.katch0420.macebot.main.networking.MaceBotNetworking;
import net.katch0420.macebot.main.networking.packets.c2s.ConfirmInstallC2SPacket;
import net.katch0420.macebot.main.settings.client.ClientSideSettings;
import net.katch0420.macebot.main.settings.server.SettingsSyncHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;

public class MaceBotClient implements ClientModInitializer{

    public static MaceBotClient instance;

    public static MaceBotClient getInstance(){
        return instance;
    }

    public static ClientPlayerEntity getClientPlayer(){
        return MinecraftClient.getInstance().player;
    }

    public static MainFrame mainFrame = new MainFrame();

    public static Theme theme = Themes.CURRENT;

    public static String SERVER_SIDE_VERSION = "";

    @Override
    public void onInitializeClient() {

        instance = this;
        ClientPlayConnectionEvents.DISCONNECT.register(
                (a,b)-> ClientSideSettings.setConnected(false)
        );

        ClientTickEvents.START_CLIENT_TICK.register(
                c -> MaceBotKeyBinds.processKeybinds()
        );

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) ->
                ClientPlayNetworking.send(new ConfirmInstallC2SPacket(MaceBot.VERSION))
        );

        MaceBotNetworking.registerS2CPackets();
        MaceBotKeyBinds.register();
    }
}
