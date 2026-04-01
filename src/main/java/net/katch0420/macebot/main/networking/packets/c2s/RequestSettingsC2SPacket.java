package net.katch0420.macebot.main.networking.packets.c2s;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.katch0420.macebot.main.MaceBot;
import net.katch0420.macebot.main.settings.server.Settings;
import net.katch0420.macebot.main.settings.server.SettingsSyncHelper;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record RequestSettingsC2SPacket() implements CustomPayload{
    public static Identifier REQUEST_SETTINGS_C2S = Identifier.of(MaceBot.MOD_ID, "request_settings_c2s");
    public static CustomPayload.Id<RequestSettingsC2SPacket> ID = new CustomPayload.Id<>(REQUEST_SETTINGS_C2S);
    public static final PacketCodec<RegistryByteBuf, RequestSettingsC2SPacket> CODEC = PacketCodec.unit(new RequestSettingsC2SPacket());

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void receive(RequestSettingsC2SPacket payload, ServerPlayNetworking.Context context){
        context.server().execute(()-> {
            SettingsSyncHelper.sendAllSettings(context.player());
        });
    }
}
