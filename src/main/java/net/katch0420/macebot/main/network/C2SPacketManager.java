package net.katch0420.macebot.main.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.katch0420.macebot.main.network.packets.c2s.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public class C2SPacketManager {
    public static void registerC2SReceivers(){
        ServerPlayNetworking.registerGlobalReceiver(ConfirmInstallC2SPacket.ID, ConfirmInstallC2SPacket::receive);
        ServerPlayNetworking.registerGlobalReceiver(SettingC2SPacket.ID, SettingC2SPacket::receive);
        ServerPlayNetworking.registerGlobalReceiver(RequestSettingsC2SPacket.ID, RequestSettingsC2SPacket::receive);
        ServerPlayNetworking.registerGlobalReceiver(PlayerBotC2SPacket.ID, PlayerBotC2SPacket::receive);
        ServerPlayNetworking.registerGlobalReceiver(KitSyncC2SPacket.ID, KitSyncC2SPacket::receive);
        ServerPlayNetworking.registerGlobalReceiver(ChangeNameC2SPacket.Type, ChangeNameC2SPacket::receive);
    }

    public static void send(CustomPacketPayload packet){
        ClientPlayNetworking.send(packet);
    }
}
