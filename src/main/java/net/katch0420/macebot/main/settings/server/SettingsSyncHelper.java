package net.katch0420.macebot.main.settings.server;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.katch0420.macebot.main.networking.packets.s2c.UpdateSettingS2CPacket;
import net.katch0420.macebot.main.settings.main.SettingsKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public class SettingsSyncHelper {

    /**
     * Send every setting to one player (call on join).
     */
    public static void sendAllSettings(ServerPlayerEntity player) {
        for (SettingsKey key : SettingsKey.values()) {
            sendToPlayer(key, player);
        }
    }

    /**
     * Apply a value to server Settings, then broadcast the new value to ALL clients.
     * Used by commands and the C2S packet handler for user-facing settings.
     */
    public static void applyAndBroadcast(SettingsKey key, Object value, MinecraftServer server) {
        key.applyToServer(value);
        broadcast(key, server);
    }

    /**
     * Read the current server value and broadcast it to all clients without changing it.
     * Used for status flags (MACEBOT_ONLINE, MACEBOT_PAUSED) that are set by lifecycle
     * code elsewhere and just need to be pushed out after the fact.
     */
    public static void broadcastCurrent(SettingsKey key, MinecraftServer server) {
        broadcast(key, server);
    }

    // ── Internal ──────────────────────────────────────────────────────────────

    private static void broadcast(SettingsKey key, MinecraftServer server) {
        UpdateSettingS2CPacket packet = new UpdateSettingS2CPacket(key, key.getServerValueAsString());
        for (ServerPlayerEntity player : PlayerLookup.all(server)) {
            ServerPlayNetworking.send(player, packet);
        }
    }

    private static void sendToPlayer(SettingsKey key, ServerPlayerEntity player) {
        ServerPlayNetworking.send(player,
                new UpdateSettingS2CPacket(key, key.getServerValueAsString()));
    }
}