package net.katch0420.macebot.main.settings.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.katch0420.macebot.main.networking.packets.c2s.UpdateSettingC2SPacket;
import net.katch0420.macebot.main.settings.main.SettingsKey;

@Environment(EnvType.CLIENT)
public class ClientSideSettingsSyncHelper {

    /**
     * Apply a setting locally on the client AND send it to the server.
     * The server will then broadcast it back to all clients via UpdateSettingS2CPacket.
     * This is what OptionScreen should call on every setting change.
     */
    public static void setAndSend(SettingsKey key, Object value) {
        // 1. Apply immediately on this client so the UI feels responsive
        key.applyToClient(value);
        // 2. Tell the server — it will validate, apply, and rebroadcast to all
        ClientPlayNetworking.send(new UpdateSettingC2SPacket(key, String.valueOf(value)));
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