package net.katch0420.macebot.main.network.packets.c2s;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.katch0420.macebot.main.MaceBot;
import net.katch0420.macebot.main.settings.SettingManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record RequestSettingsC2SPacket() implements CustomPacketPayload {
    public static Identifier REQUEST_SETTINGS_C2S = Identifier.fromNamespaceAndPath(MaceBot.MOD_ID, "request_settings_c2s");
    public static Type<RequestSettingsC2SPacket> ID = new Type<>(REQUEST_SETTINGS_C2S);
    public static final StreamCodec<RegistryFriendlyByteBuf, RequestSettingsC2SPacket> CODEC = StreamCodec.unit(new RequestSettingsC2SPacket());

    public static void receive(RequestSettingsC2SPacket payload, ServerPlayNetworking.Context context){
        context.server().execute(()-> {
            SettingManager.resyncFor(context.player());
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
