package net.katch0420.macebot.main.networking.packets.s2c;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.katch0420.macebot.main.MaceBot;
import net.katch0420.macebot.main.kits.client.data.ClientKitRegistry;
import net.katch0420.macebot.main.kits.client.data.KitData;
import net.katch0420.macebot.main.kits.server.Kit;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.Objects;

public record UpdateKitDataS2CPacket(String id, String displayName, String icon, boolean custom, int count) implements CustomPayload {
    public static String RESET_COMMAND = "macebot_reset_kits";
    public static String KIT_DELETE_COMMAND = "macebot_delete_kit";
    public static final Identifier RAW_ID = Identifier.of(MaceBot.MOD_ID, "update_kit_data_s2c");
    public static final CustomPayload.Id<UpdateKitDataS2CPacket> ID = new CustomPayload.Id<>(RAW_ID);

    // Codec: encodes key as enum index, value as string
    public static final PacketCodec<RegistryByteBuf, UpdateKitDataS2CPacket> CODEC =
            PacketCodec.tuple(
                    PacketCodecs.STRING,
                    UpdateKitDataS2CPacket::id,

                    PacketCodecs.STRING,
                    UpdateKitDataS2CPacket::displayName,

                    PacketCodecs.STRING,
                    UpdateKitDataS2CPacket::icon,

                    PacketCodecs.BOOL,
                    UpdateKitDataS2CPacket::custom,

                    PacketCodecs.INTEGER,
                    UpdateKitDataS2CPacket::count,

                    UpdateKitDataS2CPacket::new
            );

    public UpdateKitDataS2CPacket(Kit k) {
        this(k.getId(), k.getDisplayName(), k.getIconItem(), k.isCustom(), k.getItems().size());
    }

    public UpdateKitDataS2CPacket(String command) {
        this("","",command,true,0);
    }

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }

    // Client-side receive handler
    public static void receive(UpdateKitDataS2CPacket payload, ClientPlayNetworking.Context context) {
        context.client().execute(() -> {
            if(Objects.equals(payload.icon, RESET_COMMAND)){
                ClientKitRegistry.deleteAll();
                return;
            }
            if(Objects.equals(payload.icon, KIT_DELETE_COMMAND)){
                ClientKitRegistry.unregister(payload.id);
                return;
            }
            KitData kitData = new KitData(
                    payload.id,
                    payload.displayName,
                    payload.icon,
                    payload.custom,
                    payload.count
            );

            ClientKitRegistry.updateOrElseRegister(kitData);
        });
    }
}
