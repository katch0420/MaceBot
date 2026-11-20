package net.katch0420.macebot.main.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.katch0420.macebot.main.network.packets.c2s.*;
import net.katch0420.macebot.main.network.packets.s2c.*;

public class MainPacketManager {

    public static void registerPackets(){
        //S2C
        //? if <=1.21.11 {
        PayloadTypeRegistry.playS2C().register(ConfirmInstallS2CPacket.ID, ConfirmInstallS2CPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(SettingS2CPacket.ID, SettingS2CPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(KitSyncS2CPacket.ID, KitSyncS2CPacket.CODEC);

        PayloadTypeRegistry.playC2S().register(ConfirmInstallC2SPacket.ID, ConfirmInstallC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(SettingC2SPacket.ID, SettingC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(RequestSettingsC2SPacket.ID,RequestSettingsC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(PlayerBotC2SPacket.ID, PlayerBotC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(KitSyncC2SPacket.ID, KitSyncC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(ChangeNameC2SPacket.Type, ChangeNameC2SPacket.CODEC);
        //?} else {
        /*PayloadTypeRegistry.clientboundPlay().register(ConfirmInstallS2CPacket.ID, ConfirmInstallS2CPacket.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(SettingS2CPacket.ID,SettingS2CPacket.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(KitSyncS2CPacket.ID, KitSyncS2CPacket.CODEC);

        //C2S
        PayloadTypeRegistry.serverboundPlay().register(ConfirmInstallC2SPacket.ID, ConfirmInstallC2SPacket.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(SettingC2SPacket.ID, SettingC2SPacket.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(RequestSettingsC2SPacket.ID,RequestSettingsC2SPacket.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(PlayerBotC2SPacket.ID, PlayerBotC2SPacket.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(KitSyncC2SPacket.ID, KitSyncC2SPacket.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ChangeNameC2SPacket.Type, ChangeNameC2SPacket.CODEC);
        *///?}
    }
}
