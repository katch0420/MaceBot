package net.katch0420.macebot.main.networking.packets.c2s;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.katch0420.macebot.main.MaceBot;
import net.katch0420.macebot.main.kits.client.gui.handled.KitViewScreen;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public record OpenKitViewerC2SPacket(String kitId) implements CustomPayload {
    public static Identifier OPEN_KIT_VIEWER_C2S = Identifier.of(MaceBot.MOD_ID, "open_kit_viewer_c2s");
    public static CustomPayload.Id<OpenKitViewerC2SPacket> ID = new CustomPayload.Id<>(OPEN_KIT_VIEWER_C2S);
    public static final PacketCodec<RegistryByteBuf, OpenKitViewerC2SPacket> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING,
            OpenKitViewerC2SPacket::kitId,
            OpenKitViewerC2SPacket::new
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void receive(OpenKitViewerC2SPacket payload, ServerPlayNetworking.Context context) {
        context.server().execute(() -> context.player().openHandledScreen(
                        new SimpleNamedScreenHandlerFactory(
                                (syncId, playerInventory, player1) -> new KitViewScreen.KitViewScreenHandler(syncId, playerInventory, payload.kitId),
                                Text.of("Kit Viewer")
                        )
                )
        );
    }
}
