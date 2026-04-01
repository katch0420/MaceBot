package net.katch0420.macebot.main.networking;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.katch0420.macebot.main.networking.packets.c2s.*;
import net.katch0420.macebot.main.networking.packets.s2c.ConfirmInstallS2CPacket;
import net.katch0420.macebot.main.networking.packets.s2c.UpdateKitDataS2CPacket;
import net.katch0420.macebot.main.networking.packets.s2c.UpdateSettingS2CPacket;

public class MaceBotNetworking {

    public static void registerPackets(){
        //S2C
        PayloadTypeRegistry.playS2C().register(ConfirmInstallS2CPacket.ID, ConfirmInstallS2CPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(UpdateSettingS2CPacket.ID, UpdateSettingS2CPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(UpdateKitDataS2CPacket.ID, UpdateKitDataS2CPacket.CODEC);

        //C2S
        PayloadTypeRegistry.playC2S().register(UpdateCursorStackC2SPacket.ID,UpdateCursorStackC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(ConfirmInstallC2SPacket.ID, ConfirmInstallC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(UpdateSettingC2SPacket.ID, UpdateSettingC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(RequestSettingsC2SPacket.ID,RequestSettingsC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(ControlCommandC2SPacket.ID, ControlCommandC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(UpdateKitSlotC2SPacket.ID, UpdateKitSlotC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(OpenKitEditorC2SPacket.ID,OpenKitEditorC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(OpenKitViewerC2SPacket.ID,OpenKitViewerC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(UpdateKitDataC2SPacket.ID, UpdateKitDataC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(CreateNewKitC2SPacket.ID, CreateNewKitC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(KitLoadRequestC2SPacket.ID, KitLoadRequestC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(RequestKitDuplicationC2SPacket.ID, RequestKitDuplicationC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(SaveKitC2SPacket.ID, SaveKitC2SPacket.CODEC);
    }

    //Called in client side
    public static void registerS2CPackets(){
        ClientPlayNetworking.registerGlobalReceiver(ConfirmInstallS2CPacket.ID, ConfirmInstallS2CPacket::receive);
        ClientPlayNetworking.registerGlobalReceiver(UpdateSettingS2CPacket.ID, UpdateSettingS2CPacket::receive);
        ClientPlayNetworking.registerGlobalReceiver(UpdateKitDataS2CPacket.ID, UpdateKitDataS2CPacket::receive);
    }

    //Called in server side
    public static void registerC2SPackets(){
        ServerPlayNetworking.registerGlobalReceiver(ConfirmInstallC2SPacket.ID, ConfirmInstallC2SPacket::receive);
        ServerPlayNetworking.registerGlobalReceiver(UpdateSettingC2SPacket.ID, UpdateSettingC2SPacket::receive);
        ServerPlayNetworking.registerGlobalReceiver(RequestSettingsC2SPacket.ID, RequestSettingsC2SPacket::receive);
        ServerPlayNetworking.registerGlobalReceiver(ControlCommandC2SPacket.ID, ControlCommandC2SPacket::receive);
        ServerPlayNetworking.registerGlobalReceiver(UpdateCursorStackC2SPacket.ID,UpdateCursorStackC2SPacket::receive);
        ServerPlayNetworking.registerGlobalReceiver(UpdateKitSlotC2SPacket.ID,UpdateKitSlotC2SPacket::receive);
        ServerPlayNetworking.registerGlobalReceiver(OpenKitEditorC2SPacket.ID, OpenKitEditorC2SPacket::receive);
        ServerPlayNetworking.registerGlobalReceiver(OpenKitViewerC2SPacket.ID, OpenKitViewerC2SPacket::receive);
        ServerPlayNetworking.registerGlobalReceiver(UpdateKitDataC2SPacket.ID, UpdateKitDataC2SPacket::receive);
        ServerPlayNetworking.registerGlobalReceiver(CreateNewKitC2SPacket.ID, CreateNewKitC2SPacket::receive);
        ServerPlayNetworking.registerGlobalReceiver(KitLoadRequestC2SPacket.ID, KitLoadRequestC2SPacket::receive);
        ServerPlayNetworking.registerGlobalReceiver(RequestKitDuplicationC2SPacket.ID, RequestKitDuplicationC2SPacket::receive);
        ServerPlayNetworking.registerGlobalReceiver(SaveKitC2SPacket.ID, SaveKitC2SPacket::receive);
    }
}
