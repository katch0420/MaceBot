package net.katch0420.macebot.main.networking.packets.c2s;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.katch0420.macebot.main.MaceBot;
import net.katch0420.macebot.main.kits.server.CustomKitManager;
import net.katch0420.macebot.main.kits.server.Kit;
import net.katch0420.macebot.main.kits.server.KitRegistry;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record CreateNewKitC2SPacket(String base) implements CustomPayload{
    public static Identifier REQUEST_NEW_KIT_ID_C2S = Identifier.of(MaceBot.MOD_ID, "request_new_kit_id_c2s");
    public static CustomPayload.Id<CreateNewKitC2SPacket> ID = new CustomPayload.Id<>(REQUEST_NEW_KIT_ID_C2S);
    public static final PacketCodec<RegistryByteBuf, CreateNewKitC2SPacket> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING,
            CreateNewKitC2SPacket::base,
            CreateNewKitC2SPacket::new
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void receive(CreateNewKitC2SPacket payload, ServerPlayNetworking.Context context){
        context.server().execute(() -> KitRegistry.register(new Kit(
                CustomKitManager.generateId(payload.base),
                "New Kit",
                "stone_sword",
                true
        )));
    }
}
