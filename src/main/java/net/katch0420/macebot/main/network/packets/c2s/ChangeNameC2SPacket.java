package net.katch0420.macebot.main.network.packets.c2s;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.katch0420.macebot.main.MaceBot;
import net.katch0420.macebot.main.macebot.bot.PlayerBot;
import net.katch0420.macebot.main.utils.LegacyText;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ChangeNameC2SPacket(String json) implements CustomPacketPayload {
    public static Identifier CHANGE_NAME_C2S = Identifier.fromNamespaceAndPath(MaceBot.MOD_ID, "change_name_c2s");
    public static CustomPacketPayload.Type<ChangeNameC2SPacket> Type = new Type<>(CHANGE_NAME_C2S);
    public static final StreamCodec<RegistryFriendlyByteBuf, ChangeNameC2SPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ChangeNameC2SPacket::json,
            ChangeNameC2SPacket::new
    );

    public static void send(String text){
        ClientPlayNetworking.send(new ChangeNameC2SPacket(
                text
        ));
    }

    public static void receive(ChangeNameC2SPacket payload, ServerPlayNetworking.Context context){
        PlayerBot.applyNewIdentity(LegacyText.parse(payload.json()));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return Type;
    }
}
