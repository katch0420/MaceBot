package net.katch0420.macebot.main.networking.packets.c2s;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.katch0420.macebot.main.MaceBot;
import net.katch0420.macebot.main.kits.client.data.KitData;
import net.katch0420.macebot.main.kits.main.KitSyncManager;
import net.katch0420.macebot.main.kits.server.Kit;
import net.katch0420.macebot.main.kits.server.KitRegistry;
import net.katch0420.macebot.main.networking.packets.s2c.UpdateKitDataS2CPacket;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.Objects;

public record UpdateKitDataC2SPacket(String id, String displayName, String icon, boolean custom) implements CustomPayload {
    public static final Identifier RAW_ID = Identifier.of(MaceBot.MOD_ID, "update_kit_data_c2s");
    public static final CustomPayload.Id<UpdateKitDataC2SPacket> ID = new CustomPayload.Id<>(RAW_ID);

    public static final PacketCodec<RegistryByteBuf, UpdateKitDataC2SPacket> CODEC =
            PacketCodec.tuple(
                    PacketCodecs.STRING,
                    UpdateKitDataC2SPacket::id,

                    PacketCodecs.STRING,
                    UpdateKitDataC2SPacket::displayName,

                    PacketCodecs.STRING,
                    UpdateKitDataC2SPacket::icon,

                    PacketCodecs.BOOL,
                    UpdateKitDataC2SPacket::custom,

                    UpdateKitDataC2SPacket::new
            );

    public UpdateKitDataC2SPacket(KitData kitData) {
        this(kitData.getId(), kitData.getDisplayName(), kitData.getIconItem(), kitData.isCustom());
    }

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void receive(UpdateKitDataC2SPacket payload, ServerPlayNetworking.Context context) {
        context.server().execute(() -> {
            if(Objects.equals(payload.icon, UpdateKitDataS2CPacket.KIT_DELETE_COMMAND)){
                KitRegistry.unregister(payload.id());
                KitSyncManager.syncKitDeleteToClients(payload.id);
                return;
            }
            KitRegistry.updateOrElseRegister(new Kit(payload.id(), payload.displayName(), payload.icon(), payload.custom()));
        });
    }
}
