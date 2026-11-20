package net.katch0420.macebot.main.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.katch0420.macebot.main.MaceBot;
import net.katch0420.macebot.main.network.packets.s2c.ConfirmInstallS2CPacket;
import net.katch0420.macebot.main.network.packets.s2c.KitSyncS2CPacket;
import net.katch0420.macebot.main.network.packets.s2c.SettingS2CPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public class S2CPacketManager {
    public static void registerS2CReceivers() {
        ClientPlayNetworking.registerGlobalReceiver(ConfirmInstallS2CPacket.ID, ConfirmInstallS2CPacket::receive);
        ClientPlayNetworking.registerGlobalReceiver(SettingS2CPacket.ID, SettingS2CPacket::receive);
        ClientPlayNetworking.registerGlobalReceiver(KitSyncS2CPacket.ID, KitSyncS2CPacket::receive);
    }

    public static void sendToAll(CustomPacketPayload c) {
        MaceBot.server.getPlayerList().getPlayers().forEach(player -> send(player, c));
    }

    private static void send(ServerPlayer player, CustomPacketPayload c) {
        ServerPlayNetworking.send(player, c);
    }
}
