package net.katch0420.macebot.main.networking.packets.c2s;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.katch0420.macebot.main.kits.client.gui.handled.KitEditorScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.Identifier;

public record UpdateKitSlotC2SPacket(int slotIndex, ItemStack stack) implements CustomPayload {
    public static Identifier UPDATE_KIT_SLOT_C2S = Identifier.of("macebot", "update_kit_slot_c2s");
    public static CustomPayload.Id<UpdateKitSlotC2SPacket> ID = new Id<>(UPDATE_KIT_SLOT_C2S);
    public static final PacketCodec<RegistryByteBuf, UpdateKitSlotC2SPacket> CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER,
            UpdateKitSlotC2SPacket::slotIndex,

            ItemStack.PACKET_CODEC,
            UpdateKitSlotC2SPacket::stack,

            UpdateKitSlotC2SPacket::new
    );

    public static void updateKitSlot(int slotIndex, ItemStack stack) {
        ClientPlayNetworking.send(new UpdateKitSlotC2SPacket(slotIndex, stack));
    }


    public static void receive(UpdateKitSlotC2SPacket payload, ServerPlayNetworking.Context context) {
        context.server().execute(() -> {
            ScreenHandler screenHandler = context.player().currentScreenHandler;

            // Verify the player has the kit editor screen open
            if (screenHandler == null) {
                return;
            }

            // Verify slot index is valid (0-40 for player inventory + armor + offhand)
            if (payload.slotIndex() < 0 || payload.slotIndex() > 40) {
                return;
            }

            if(screenHandler instanceof KitEditorScreen.KitEditorScreenHandler handler){
                handler.getInventoryWrapper().setStack(payload.slotIndex(), payload.stack());
            }
        });
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}