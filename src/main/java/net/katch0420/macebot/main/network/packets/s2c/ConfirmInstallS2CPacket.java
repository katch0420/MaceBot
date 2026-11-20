package net.katch0420.macebot.main.network.packets.s2c;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.*;
import net.katch0420.macebot.client.MaceBotClient;
import net.katch0420.macebot.main.MaceBot;
import net.katch0420.macebot.main.messenger.ClientChatMessageManager;
import net.katch0420.macebot.main.messenger.Messages;
import net.katch0420.macebot.main.network.packets.c2s.RequestSettingsC2SPacket;
import net.katch0420.macebot.main.settings.Settings;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.Objects;

public record ConfirmInstallS2CPacket(String version) implements CustomPacketPayload {
    public static Identifier CONFIRM_INSTALL_S2C = Identifier.fromNamespaceAndPath(MaceBot.MOD_ID, "confirm_install_s2c");
    public static Type<ConfirmInstallS2CPacket> ID = new Type<>(CONFIRM_INSTALL_S2C);
    public static final StreamCodec<RegistryFriendlyByteBuf, ConfirmInstallS2CPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            ConfirmInstallS2CPacket::version,
            ConfirmInstallS2CPacket::new
    );

    public static void receive(ConfirmInstallS2CPacket payload, Context context){
        context.client().execute(() -> {
            String serverVersion = payload.version();
            MaceBotClient.SERVER_SIDE_VERSION = serverVersion;
            if (Objects.equals(serverVersion, MaceBot.VERSION)) {
                ClientChatMessageManager.info(Messages.I_CONNECTION_DETECTED);
            } else {
                ClientChatMessageManager.info(Messages.W_CONNECTION_DETECTION_MISMATCH);
            }
            Settings.CLIENT.update(true);
            ClientPlayNetworking.send(new RequestSettingsC2SPacket());
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
