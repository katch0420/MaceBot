package net.katch0420.macebot.main.networking.packets.c2s;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.katch0420.macebot.main.MaceBot;
import net.katch0420.macebot.main.settings.main.SettingsKey;
import net.katch0420.macebot.main.settings.server.SettingsSyncHelper;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record UpdateSettingC2SPacket(SettingsKey key, String value) implements CustomPayload {

    public static final Identifier RAW_ID = Identifier.of(MaceBot.MOD_ID, "update_setting_c2s");
    public static final CustomPayload.Id<UpdateSettingC2SPacket> ID = new CustomPayload.Id<>(RAW_ID);

    public static final PacketCodec<RegistryByteBuf, UpdateSettingC2SPacket> CODEC =
            PacketCodec.tuple(
                    PacketCodecs.indexed(i -> SettingsKey.values()[i], SettingsKey::ordinal),
                    UpdateSettingC2SPacket::key,
                    PacketCodecs.STRING,
                    UpdateSettingC2SPacket::value,
                    UpdateSettingC2SPacket::new
            );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }

    public static void receive(UpdateSettingC2SPacket payload, ServerPlayNetworking.Context context) {
        context.server().execute(() -> {
            SettingsKey key   = payload.key();
            Object      value = key.parse(payload.value()); // no more inline if-chain

            // Apply to server Settings AND broadcast new value to every client
            SettingsSyncHelper.applyAndBroadcast(key, value, context.server());
        });
    }
}