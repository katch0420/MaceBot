package net.katch0420.macebot.main.networking.packets.s2c;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.*;
import net.katch0420.macebot.client.MaceBotClient;
import net.katch0420.macebot.main.MaceBot;
import net.katch0420.macebot.main.messenger.ModMessages;
import net.katch0420.macebot.main.networking.packets.c2s.RequestSettingsC2SPacket;
import net.katch0420.macebot.main.settings.client.ClientSideSettings;
import net.katch0420.macebot.main.settings.client.ClientSideSettingsSyncHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.Objects;

public record ConfirmInstallS2CPacket(String version) implements CustomPayload {
    public static Identifier CONFIRM_INSTALL_S2C = Identifier.of(MaceBot.MOD_ID, "confirm_install_s2c");
    public static Id<ConfirmInstallS2CPacket> ID = new Id<>(CONFIRM_INSTALL_S2C);
    public static final PacketCodec<RegistryByteBuf, ConfirmInstallS2CPacket> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING,
            ConfirmInstallS2CPacket::version,
            ConfirmInstallS2CPacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void receive(ConfirmInstallS2CPacket payload, Context context){
        context.client().execute(() -> {
            String serverVersion = payload.version();
            MaceBotClient.SERVER_SIDE_VERSION = serverVersion;
            if (Objects.equals(serverVersion, MaceBot.VERSION)) {
                ModMessages.send(context.player(), ModMessages.SUCCESS.copy().append(ModMessages.CLIENT_INFO_CHAT_NETWORK_DETECTION_SUCCESSFUL));
            } else {
                ModMessages.send(context.player(), ModMessages.WARNING.copy().append(ModMessages.CLIENT_WARN_CHAT_NETWORK_DETECTION_SUCCESSFUL_MISMATCH));
            }
            MaceBot.LOGGER.info("Detected MaceBot in server environment.");
            MaceBot.LOGGER.info("Sending Setting Sync Request");
            ClientSideSettings.setConnected(true);
            ClientPlayNetworking.send(new RequestSettingsC2SPacket());
        });
    }
}
