package net.katch0420.macebot.main.network.packets.s2c;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.katch0420.macebot.main.MaceBot;
import net.katch0420.macebot.main.settings.SettingManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record SettingS2CPacket(String id, String val) implements CustomPacketPayload {
    public static final Identifier RAW_ID = Identifier.fromNamespaceAndPath(MaceBot.MOD_ID, "setting_s2c");
    public static final CustomPacketPayload.Type<SettingS2CPacket> ID = new CustomPacketPayload.Type<>(RAW_ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, SettingS2CPacket> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8,
                    SettingS2CPacket::id,
                    ByteBufCodecs.STRING_UTF8,
                    SettingS2CPacket::val,
                    SettingS2CPacket::new
            );

    public static void receive(SettingS2CPacket payload, ClientPlayNetworking.Context context) {
        context.client().execute(() -> {
            var v = SettingManager.getSetting(payload.id());
            v.setValue(v.parse(payload.val()));
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
