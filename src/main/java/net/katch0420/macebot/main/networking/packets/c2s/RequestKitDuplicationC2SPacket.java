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

public record RequestKitDuplicationC2SPacket(String id) implements CustomPayload {
    public static Identifier REQUEST_KIT_DUPLICATION_C2S = Identifier.of(MaceBot.MOD_ID, "request_kit_duplication_c2s");
    public static CustomPayload.Id<RequestKitDuplicationC2SPacket> ID = new CustomPayload.Id<>(REQUEST_KIT_DUPLICATION_C2S);
    public static final PacketCodec<RegistryByteBuf, RequestKitDuplicationC2SPacket> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING,
            RequestKitDuplicationC2SPacket::id,
            RequestKitDuplicationC2SPacket::new
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void receive(RequestKitDuplicationC2SPacket payload, ServerPlayNetworking.Context context){
        context.server().execute(()->{
            String newID = CustomKitManager.generateId(payload.id + "_copy");
            Kit kit = KitRegistry.get(payload.id);
            Kit copy = new Kit(newID, kit.getDisplayName() + " (Copy)", kit.getIconItem(), true);
            kit.getItems().forEach(copy::addItem);
            KitRegistry.register(copy);
            CustomKitManager.saveKit(copy);
        });
    }
}

