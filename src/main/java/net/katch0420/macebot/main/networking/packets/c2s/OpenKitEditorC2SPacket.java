package net.katch0420.macebot.main.networking.packets.c2s;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.katch0420.macebot.main.MaceBot;
import net.katch0420.macebot.main.kits.server.KitRegistry;
import net.katch0420.macebot.main.kits.client.gui.handled.KitEditorScreen;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public record OpenKitEditorC2SPacket(String kitId) implements CustomPayload {
    public static Identifier OPEN_KIT_EDITOR_C2S = Identifier.of(MaceBot.MOD_ID, "open_kit_editor_c2s");
    public static CustomPayload.Id<OpenKitEditorC2SPacket> ID = new CustomPayload.Id<>(OPEN_KIT_EDITOR_C2S);
    public static final PacketCodec<RegistryByteBuf, OpenKitEditorC2SPacket> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING,
            OpenKitEditorC2SPacket::kitId,
            OpenKitEditorC2SPacket::new
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void receive(OpenKitEditorC2SPacket payload, ServerPlayNetworking.Context context){
        context.server().execute(() -> context.player().openHandledScreen(
                new SimpleNamedScreenHandlerFactory(
                        (syncId, playerInventory, player1) -> new KitEditorScreen.KitEditorScreenHandler(syncId, player1, KitRegistry.get(payload.kitId())),
                        Text.of("Kit Editor")
                )
        ));
    }
}
