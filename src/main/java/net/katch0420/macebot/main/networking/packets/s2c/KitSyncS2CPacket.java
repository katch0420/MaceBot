package net.katch0420.macebot.main.networking.packets.s2c;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.katch0420.macebot.main.MaceBot;
import net.katch0420.macebot.main.kits.client.data.ClientKitRegistry;
import net.katch0420.macebot.main.kits.main.Kit;
import net.katch0420.macebot.main.kits.main.KitStack;
import net.katch0420.macebot.main.kits.server.*;
import net.katch0420.macebot.main.networking.packets.c2s.KitSyncC2SPacket;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryOps;
import net.minecraft.util.Identifier;
import com.mojang.serialization.JsonOps;
import java.util.Map;

/**
 * Single unified payload replacing all previous kit C2S/S2C packets.
 * C2S commands: sync_kit, delete_kit, duplicate_kit, open_editor,
 *               open_viewer, save_kit, update_slot, load_kit
 * S2C commands: kit_data, reset
 */
public record KitSyncS2CPacket(
        String command,
        String kitId,
        String displayName,
        String iconItem,
        String payload,
        boolean isCustom
) implements CustomPayload {

    public static final CustomPayload.Id<KitSyncS2CPacket> ID =
            new CustomPayload.Id<>(Identifier.of("macebot", "kit_sync_s2c"));

    public static final PacketCodec<PacketByteBuf, KitSyncS2CPacket> CODEC = PacketCodec.tuple(
            PacketCodecs.string(32), KitSyncS2CPacket::command,
            PacketCodecs.string(64), KitSyncS2CPacket::kitId,
            PacketCodecs.string(256), KitSyncS2CPacket::displayName,
            PacketCodecs.string(256), KitSyncS2CPacket::iconItem,
            PacketCodecs.STRING, KitSyncS2CPacket::payload,
            PacketCodecs.BOOL, KitSyncS2CPacket::isCustom,
            KitSyncS2CPacket::new
    );

    public static final String CMD_KIT_DATA      = "kit_data";
    public static final String CMD_RESET         = "reset";
    public static final String CMD_KIT_ITEMS = "kit_items";
    public static final String CMD_DELETE_KIT    = "delete_kit";


    private static final Gson GSON = new Gson();

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }

    // ── S2C factories ─────────────────────────────────────────────────────────

    public static KitSyncS2CPacket kitItems(Kit kit) {
        return new KitSyncS2CPacket(CMD_KIT_ITEMS, kit.getId(), "", "",
                serializeItems(kit), kit.isCustom());
    }
    public static KitSyncS2CPacket kitData(Kit kit) {
        return new KitSyncS2CPacket(CMD_KIT_DATA, kit.getId(), kit.getDisplayName(),
                orEmpty(kit.getIconId().getPath()), serializeItems(kit), kit.isCustom());
    }

    public static KitSyncS2CPacket deleteKit(String kitId) {
        return new KitSyncS2CPacket(CMD_DELETE_KIT, kitId, "", "", "{}", false);
    }

    public static KitSyncS2CPacket reset() {
        return new KitSyncS2CPacket(CMD_RESET, "", "", "", "{}", false);
    }

    public static void receive(KitSyncS2CPacket p, ClientPlayNetworking.Context c){
        ClientKitRegistry.handleClient(p,c);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static String serializeItems(Kit kit) {
        if (MaceBot.server == null) return "{}";
        try {
            JsonObject root = new JsonObject();
            RegistryOps<JsonElement> ops = RegistryOps.of(JsonOps.INSTANCE, MaceBot.server.getRegistryManager());
            for (Map.Entry<Integer, KitStack> e : kit.getItems().entrySet()) {
                ItemStack stack = e.getValue().toStack();
                if (stack == null || stack.isEmpty()) continue;
                JsonElement el = ItemStack.CODEC.encodeStart(ops, stack).resultOrPartial(err -> {}).orElse(null);
                if (el != null) root.add(String.valueOf(e.getKey()), el);
            }
            return root.toString();
        } catch (Exception e) { return "{}"; }
    }

    private static void deserializeItems(Kit kit, String json, DynamicRegistryManager rm) {
        try {
            JsonObject root = GSON.fromJson(json, JsonObject.class);
            RegistryOps<JsonElement> ops = RegistryOps.of(JsonOps.INSTANCE, rm);
            root.entrySet().forEach(e -> {
                try {
                    int slot = Integer.parseInt(e.getKey());
                    ItemStack stack = ItemStack.CODEC.parse(ops, e.getValue())
                            .resultOrPartial(err -> {}).orElse(null);
                    if (stack != null && !stack.isEmpty())
                        kit.addItem(slot, KitStack.fromStack(stack, slot));
                } catch (Exception ex) { ex.printStackTrace(); }
            });
        } catch (Exception e) { e.printStackTrace(); }
    }

    private static String orEmpty(String s) { return s != null ? s : "minecraft:wooden_sword"; }
}