package net.katch0420.macebot.main.networking;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.katch0420.macebot.main.networking.packets.c2s.*;
import net.katch0420.macebot.main.networking.packets.s2c.ConfirmInstallS2CPacket;
import net.katch0420.macebot.main.networking.packets.s2c.KitSyncS2CPacket;
import net.katch0420.macebot.main.networking.packets.s2c.UpdateSettingS2CPacket;

public class MaceBotNetworking {

    public static void registerPackets(){
        //S2C
        PayloadTypeRegistry.playS2C().register(ConfirmInstallS2CPacket.ID, ConfirmInstallS2CPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(UpdateSettingS2CPacket.ID, UpdateSettingS2CPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(KitSyncS2CPacket.ID, KitSyncS2CPacket.CODEC);

        //C2S
        PayloadTypeRegistry.playC2S().register(UpdateCursorStackC2SPacket.ID,UpdateCursorStackC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(ConfirmInstallC2SPacket.ID, ConfirmInstallC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(UpdateSettingC2SPacket.ID, UpdateSettingC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(RequestSettingsC2SPacket.ID,RequestSettingsC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(ControlCommandC2SPacket.ID, ControlCommandC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(KitSyncC2SPacket.ID, KitSyncC2SPacket.CODEC);
    }

    //Called in client side
    public static void registerS2CPackets(){
        ClientPlayNetworking.registerGlobalReceiver(ConfirmInstallS2CPacket.ID, ConfirmInstallS2CPacket::receive);
        ClientPlayNetworking.registerGlobalReceiver(UpdateSettingS2CPacket.ID, UpdateSettingS2CPacket::receive);
        ClientPlayNetworking.registerGlobalReceiver(KitSyncS2CPacket.ID, KitSyncS2CPacket::receive);
    }

    //Called in server side
    public static void registerC2SPackets(){
        ServerPlayNetworking.registerGlobalReceiver(ConfirmInstallC2SPacket.ID, ConfirmInstallC2SPacket::receive);
        ServerPlayNetworking.registerGlobalReceiver(UpdateSettingC2SPacket.ID, UpdateSettingC2SPacket::receive);
        ServerPlayNetworking.registerGlobalReceiver(RequestSettingsC2SPacket.ID, RequestSettingsC2SPacket::receive);
        ServerPlayNetworking.registerGlobalReceiver(ControlCommandC2SPacket.ID, ControlCommandC2SPacket::receive);
        ServerPlayNetworking.registerGlobalReceiver(UpdateCursorStackC2SPacket.ID,UpdateCursorStackC2SPacket::receive);
        ServerPlayNetworking.registerGlobalReceiver(KitSyncC2SPacket.ID, KitSyncC2SPacket::receive);
    }
}
