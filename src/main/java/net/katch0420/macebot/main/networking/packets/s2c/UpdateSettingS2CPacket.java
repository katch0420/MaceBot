package net.katch0420.macebot.main.networking.packets.s2c;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.katch0420.macebot.main.MaceBot;
import net.katch0420.macebot.main.settings.main.SettingsKey;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record UpdateSettingS2CPacket(SettingsKey key, String value) implements CustomPayload {

    public static final Identifier RAW_ID = Identifier.of(MaceBot.MOD_ID, "update_setting_s2c");
    public static final CustomPayload.Id<UpdateSettingS2CPacket> ID = new CustomPayload.Id<>(RAW_ID);

    public static final PacketCodec<RegistryByteBuf, UpdateSettingS2CPacket> CODEC =
            PacketCodec.tuple(
                    PacketCodecs.indexed(i -> SettingsKey.values()[i], SettingsKey::ordinal),
                    UpdateSettingS2CPacket::key,
                    PacketCodecs.STRING,
                    UpdateSettingS2CPacket::value,
                    UpdateSettingS2CPacket::new
            );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }

    public static void receive(UpdateSettingS2CPacket payload, ClientPlayNetworking.Context context) {
        context.client().execute(() -> {
            SettingsKey key   = payload.key();
            Object      value = key.parse(payload.value()); // shared parse logic, no duplication
            key.applyToClient(value);
        });
    }
}