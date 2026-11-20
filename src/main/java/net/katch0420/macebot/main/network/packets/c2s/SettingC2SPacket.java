package net.katch0420.macebot.main.network.packets.c2s;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.katch0420.macebot.main.MaceBot;
import net.katch0420.macebot.main.settings.Flags;
import net.katch0420.macebot.main.settings.SettingManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record SettingC2SPacket(String id, String val) implements CustomPacketPayload {
    public static final Identifier RAW_ID = Identifier.fromNamespaceAndPath(MaceBot.MOD_ID, "setting_s2c");
    public static final CustomPacketPayload.Type<SettingC2SPacket> ID = new CustomPacketPayload.Type<>(RAW_ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, SettingC2SPacket> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8,
                    SettingC2SPacket::id,
                    ByteBufCodecs.STRING_UTF8,
                    SettingC2SPacket::val,
                    SettingC2SPacket::new
            );

    public static void receive(SettingC2SPacket payload, ServerPlayNetworking.Context context) {
        context.server().execute(() -> {
            var v = SettingManager.getSetting(payload.id());
            v.setValue(v.parse(payload.val()));
            if(!v.hasFlag(Flags.SETTING_SERVER_ONLY))v.updateToClients();
            else MaceBot.debugLog("Tried to send Server Only Setting: " + v.getId());
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
