package net.katch0420.macebot.main.networking.packets.c2s;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.katch0420.macebot.main.MaceBot;
import net.katch0420.macebot.main.networking.packets.s2c.ConfirmInstallS2CPacket;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.Objects;

public record ConfirmInstallC2SPacket(String version) implements CustomPayload {
    public static Identifier CONFIRM_INSTALL_C2S = Identifier.of(MaceBot.MOD_ID, "confirm_install_c2s");
    public static Id<ConfirmInstallC2SPacket> ID = new Id<>(CONFIRM_INSTALL_C2S);
    public static final PacketCodec<RegistryByteBuf, ConfirmInstallC2SPacket> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING,
            ConfirmInstallC2SPacket::version,
            ConfirmInstallC2SPacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void receive(ConfirmInstallC2SPacket payload, ServerPlayNetworking.Context context){
        context.server().execute(()-> {
            String clientVersion = payload.version();
            if (Objects.equals(clientVersion, MaceBot.VERSION)) {
                MaceBot.LOGGER.info("Client with MaceBot installed logged in. Name: {}", context.player().getName().toString());
            } else {
                MaceBot.LOGGER.info("Client with Mismatched version of MaceBot installed logged in. Name: {}", context.player().getName().toString());
            }
            ServerPlayNetworking.send(context.player(), new ConfirmInstallS2CPacket(MaceBot.VERSION));
        });
    }
}
