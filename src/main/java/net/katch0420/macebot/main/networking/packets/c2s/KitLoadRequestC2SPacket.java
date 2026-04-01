package net.katch0420.macebot.main.networking.packets.c2s;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.katch0420.macebot.main.MaceBot;
import net.katch0420.macebot.main.kits.server.KitGiver;
import net.katch0420.macebot.main.kits.server.KitRegistry;
import net.katch0420.macebot.main.macebot.bot.PlayerBot;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public record KitLoadRequestC2SPacket(Target target, String kitId, boolean unbreaking, boolean mending, boolean unbreakable) implements CustomPayload {
    public static Identifier KIT_LOAD_REQUEST_C2S = Identifier.of(MaceBot.MOD_ID, "kit_load_request_c2s");
    public static CustomPayload.Id<KitLoadRequestC2SPacket> ID = new CustomPayload.Id<>(KIT_LOAD_REQUEST_C2S);
    public static final PacketCodec<RegistryByteBuf, KitLoadRequestC2SPacket> CODEC = PacketCodec.tuple(
            PacketCodecs.indexed(i -> Target.values()[i], Target::ordinal),
            KitLoadRequestC2SPacket::target,

            PacketCodecs.STRING,
            KitLoadRequestC2SPacket::kitId,

            PacketCodecs.BOOL,
            KitLoadRequestC2SPacket::unbreaking,

            PacketCodecs.BOOL,
            KitLoadRequestC2SPacket::mending,

            PacketCodecs.BOOL,
            KitLoadRequestC2SPacket::unbreakable,

            KitLoadRequestC2SPacket::new
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void receive(KitLoadRequestC2SPacket payload, ServerPlayNetworking.Context context){
        context.server().execute(()-> {
            switch (payload.target){
                case MYSELF -> KitGiver.giveKit(context.player(), payload.kitId(), payload.unbreaking, payload.unbreakable(), payload.mending);
                case MACEBOT -> {
                    if(PlayerBot.playerBot != null && !KitRegistry.get(payload.kitId()).isCustom()){
                        KitGiver.giveKit(PlayerBot.playerBot, payload.kitId(), payload.unbreaking, payload.unbreakable(), payload.mending);
                    } else if(PlayerBot.playerBot != null){
                        context.player().sendMessage(Text.of("Only builtin kits can be given to Macebot."));
                    }
                }
                case ALL_PLAYERS -> {
                    MaceBot.server.getPlayerManager().getPlayerList().forEach(
                            p -> {
                                if(!(p instanceof PlayerBot)){
                                    KitGiver.giveKit(p, payload.kitId(), payload.unbreaking, payload.unbreakable(), payload.mending);
                                }
                            }
                    );
                }
            }
        });
    }

    public enum Target { MACEBOT, MYSELF, ALL_PLAYERS }
}
