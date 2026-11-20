package net.katch0420.macebot.main.network.packets.c2s;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.katch0420.macebot.main.MaceBot;
import net.katch0420.macebot.main.network.packets.s2c.ConfirmInstallS2CPacket;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record ConfirmInstallC2SPacket(String version) implements CustomPacketPayload {
    public static Identifier CONFIRM_INSTALL_C2S = Identifier.fromNamespaceAndPath(MaceBot.MOD_ID, "confirm_install_c2s");
    public static Type<ConfirmInstallC2SPacket> ID = new Type<>(CONFIRM_INSTALL_C2S);
    public static final StreamCodec<RegistryFriendlyByteBuf, ConfirmInstallC2SPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            ConfirmInstallC2SPacket::version,
            ConfirmInstallC2SPacket::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    public static void receive(ConfirmInstallC2SPacket payload, ServerPlayNetworking.Context context){
        context.server().execute(()-> {
            String clientVersion = payload.version();
            if (Objects.equals(clientVersion, MaceBot.VERSION)) {
                MaceBot.LOGGER.info("Client with MaceBot installed logged in. Name: {}", context.player().getName().getString());
            } else {
                MaceBot.LOGGER.info("Client with Mismatched version of MaceBot installed logged in. Name: {}", context.player().getName().getString());
            }
            ServerPlayNetworking.send(context.player(), new ConfirmInstallS2CPacket(MaceBot.VERSION));
        });
    }
}
