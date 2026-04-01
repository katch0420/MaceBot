package net.katch0420.macebot.main.networking.packets.c2s;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.katch0420.macebot.main.MaceBot;
import net.katch0420.macebot.main.kits.client.gui.handled.KitEditorScreen;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.Identifier;

public record SaveKitC2SPacket() implements CustomPayload {
    public static Identifier SAVE_KIT_C2S = Identifier.of(MaceBot.MOD_ID, "save_kit_c2s");
    public static CustomPayload.Id<SaveKitC2SPacket> ID = new CustomPayload.Id<>(SAVE_KIT_C2S);
    public static final PacketCodec<RegistryByteBuf, SaveKitC2SPacket> CODEC = PacketCodec.unit(new SaveKitC2SPacket());

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void receive(SaveKitC2SPacket payload, ServerPlayNetworking.Context context){
        context.server().execute(()->{
            ScreenHandler screenHandler = context.player().currentScreenHandler;
            if(screenHandler instanceof KitEditorScreen.KitEditorScreenHandler handler){
                handler.saveKit();
            }
        });
    }
}