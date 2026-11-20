package net.katch0420.macebot.main.network.packets.s2c;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.katch0420.macebot.main.MaceBot;
import net.katch0420.macebot.main.kits.client.data.ClientKitRegistry;
import net.katch0420.macebot.main.kits.main.Kit;
import net.katch0420.macebot.main.kits.main.KitStack;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

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
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<KitSyncS2CPacket> ID =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("bot", "kit_sync_s2c"));

    public static final StreamCodec<RegistryFriendlyByteBuf, KitSyncS2CPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(32), KitSyncS2CPacket::command,
            ByteBufCodecs.stringUtf8(64), KitSyncS2CPacket::kitId,
            ByteBufCodecs.stringUtf8(256), KitSyncS2CPacket::displayName,
            ByteBufCodecs.stringUtf8(256), KitSyncS2CPacket::iconItem,
            ByteBufCodecs.STRING_UTF8, KitSyncS2CPacket::payload,
            ByteBufCodecs.BOOL, KitSyncS2CPacket::isCustom,
            KitSyncS2CPacket::new
    );

    public static final String CMD_KIT_DATA      = "kit_data";
    public static final String CMD_RESET         = "reset";
    public static final String CMD_KIT_ITEMS = "kit_items";
    public static final String CMD_DELETE_KIT    = "delete_kit";


    private static final Gson GSON = new Gson();

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return ID; }

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
            RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, MaceBot.server.registryAccess());
            for (Map.Entry<Integer, KitStack> e : kit.getItems().entrySet()) {
                ItemStack stack = e.getValue().toStack();
                if (stack == null || stack.isEmpty()) continue;
                JsonElement el = ItemStack.CODEC.encodeStart(ops, stack).resultOrPartial(err -> {}).orElse(null);
                if (el != null) root.add(String.valueOf(e.getKey()), el);
            }
            return root.toString();
        } catch (Exception e) { return "{}"; }
    }

    private static void deserializeItems(Kit kit, String json, RegistryAccess.Frozen rm) {
        try {
            JsonObject root = GSON.fromJson(json, JsonObject.class);
            RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, rm);
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