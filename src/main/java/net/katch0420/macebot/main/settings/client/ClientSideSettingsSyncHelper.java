package net.katch0420.macebot.main.settings.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.katch0420.macebot.client.MaceBotClient;
import net.katch0420.macebot.client.gui.frames.MainFrame;
import net.katch0420.macebot.main.messenger.ModMessages;
import net.katch0420.macebot.main.networking.packets.c2s.UpdateSettingC2SPacket;
import net.katch0420.macebot.main.settings.main.SettingsKey;
import net.minecraft.client.MinecraftClient;

@Environment(EnvType.CLIENT)
public class ClientSideSettingsSyncHelper {

    /**
     * Apply a setting locally on the client AND send it to the server.
     * The server will then broadcast it back to all clients via UpdateSettingS2CPacket.
     * This is what OptionScreen should call on every setting change.
     */
    public static void setAndSend(SettingsKey key, Object value) {
        if(key.isRestricted() && userDoesNotHaveAccess()) return;
        key.applyToClient(value);
        // 2. Tell the server — it will validate, apply, and rebroadcast to all
        ClientPlayNetworking.send(new UpdateSettingC2SPacket(key, String.valueOf(value)));
    }

    private static boolean userDoesNotHaveAccess() {
        if(MaceBotClient.getClientPlayer().hasPermissionLevel(3)) return false;
        if(MinecraftClient.getInstance().currentScreen instanceof MainFrame m) m.showWarningStatus(ModMessages.CLIENT_WARN_RESTRICTED_ACTION);
        else MaceBotClient.getClientPlayer().sendMessage(ModMessages.WARNING.copy().append(ModMessages.CLIENT_WARN_RESTRICTED_ACTION));
        return true;
    }

    /**
     * Send the current client-side value of a key to the server.
     * Use this if you've already updated ClientSideSettings directly.
     */
    public static void sendCurrent(SettingsKey key) {
        String value = key.getClientValueAsString();
        ClientPlayNetworking.send(new UpdateSettingC2SPacket(key, value));
    }
}