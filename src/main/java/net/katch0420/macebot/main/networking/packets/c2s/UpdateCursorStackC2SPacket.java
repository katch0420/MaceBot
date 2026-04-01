package net.katch0420.macebot.main.networking.packets.c2s;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.katch0420.macebot.main.MaceBot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public record UpdateCursorStackC2SPacket(ItemStack stack, boolean clear) implements CustomPayload {
    public static Identifier UPDATE_CURSOR_STACK_C2S = Identifier.of(MaceBot.MOD_ID, "update_cursor_stack_c2s");
    public static CustomPayload.Id<UpdateCursorStackC2SPacket> ID = new CustomPayload.Id<>(UPDATE_CURSOR_STACK_C2S);
    public static final PacketCodec<RegistryByteBuf, UpdateCursorStackC2SPacket> CODEC = PacketCodec.tuple(
            ItemStack.PACKET_CODEC,
            UpdateCursorStackC2SPacket::stack,

            PacketCodecs.BOOL,
            UpdateCursorStackC2SPacket::clear,
            UpdateCursorStackC2SPacket::new
    );

    public static void updateCursorStack(ItemStack stack) {
        if(stack.isEmpty()){
            ClientPlayNetworking.send(new UpdateCursorStackC2SPacket(new ItemStack(Items.DIRT), true));
        } else {
            ClientPlayNetworking.send(new UpdateCursorStackC2SPacket(stack, false));
        }
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void receive(UpdateCursorStackC2SPacket payload, ServerPlayNetworking.Context context){
        context.server().execute(()-> {
            if(payload.clear) {
                context.player().currentScreenHandler.setCursorStack(ItemStack.EMPTY);
                return;
            }
            context.player().currentScreenHandler.setCursorStack(payload.stack);
        });
    }
}
