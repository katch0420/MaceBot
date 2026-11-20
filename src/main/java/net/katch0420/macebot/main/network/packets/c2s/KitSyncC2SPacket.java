package net.katch0420.macebot.main.network.packets.c2s;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.katch0420.macebot.main.MaceBot;
import net.katch0420.macebot.main.kits.main.Kit;
import net.katch0420.macebot.main.kits.main.KitStack;
import net.katch0420.macebot.main.kits.server.*;
import net.katch0420.macebot.main.macebot.bot.PlayerBot;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import com.mojang.serialization.JsonOps;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

/**
 * Single unified payload replacing all previous kit C2S/S2C packets.
 * C2S commands: sync_kit, delete_kit, duplicate_kit, open_editor,
 *               open_viewer, save_kit, update_slot, load_kit
 * S2C commands: kit_data, reset
 */
public record KitSyncC2SPacket(
        String command,
        String kitId,
        String displayName,
        String iconItem,
        String payload,
        boolean isCustom
) implements CustomPacketPayload {

    public static final Type<KitSyncC2SPacket> ID =
            new Type<>(Identifier.fromNamespaceAndPath("bot", "kit_sync_c2s"));

    public static final StreamCodec<RegistryFriendlyByteBuf, KitSyncC2SPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(32), KitSyncC2SPacket::command,
            ByteBufCodecs.stringUtf8(64), KitSyncC2SPacket::kitId,
            ByteBufCodecs.stringUtf8(256), KitSyncC2SPacket::displayName,
            ByteBufCodecs.stringUtf8(256), KitSyncC2SPacket::iconItem,
            ByteBufCodecs.STRING_UTF8, KitSyncC2SPacket::payload,
            ByteBufCodecs.BOOL, KitSyncC2SPacket::isCustom,
            KitSyncC2SPacket::new
    );

    public static final String CMD_RESYNC_KITS = "resync_all_kits";
    public static final String CMD_SYNC_KIT      = "sync_kit";
    public static final String CMD_NEW_KIT      = "new_kit";
    public static final String CMD_DELETE_KIT    = "delete_kit";
    public static final String CMD_DUPLICATE_KIT = "duplicate_kit";
    public static final String CMD_OPEN_EDITOR   = "open_editor";
    public static final String CMD_OPEN_VIEWER   = "open_viewer";
    public static final String CMD_SAVE_KIT      = "save_kit";
    public static final String CMD_UPDATE_SLOT   = "update_slot";
    public static final String CMD_LOAD_KIT      = "load_kit";
    public static final String CMD_KIT_DATA      = "kit_data";
    public static final String CMD_RESET         = "reset";

    private static final Gson GSON = new Gson();

    // ── Server handler ────────────────────────────────────────────────────────

    public static void receive(KitSyncC2SPacket p, ServerPlayNetworking.Context c) {
        MinecraftServer server = c.player()
                //? if >=1.21.9 {
                .level().getServer();
                //?} else
                //.getServer();
        if (server == null) return;

        switch (p.command) {
            case CMD_RESYNC_KITS -> {
                KitRegistry.syncAllKitsToClients();
            }
            case CMD_NEW_KIT -> {
                Kit kit = new Kit(p.kitId,p.displayName,Identifier.parse(p.iconItem),p.isCustom);
                KitRegistry.register(kit);
            }
            case CMD_SYNC_KIT -> {
                Kit kit = KitRegistry.get(p.kitId);
                if (kit == null || !kit.isCustom()) return;
                kit.setDisplayName(p.displayName);
                kit.setIconItem(Identifier.parse(p.iconItem));
                if (!p.payload.equals("{}"))
                    deserializeItems(kit, p.payload, server.registryAccess());
                KitRegistry.register(kit);
            }
            case CMD_DELETE_KIT -> {
                Kit kit = KitRegistry.get(p.kitId);
                if (kit != null && kit.isCustom()) KitRegistry.unregister(p.kitId);
            }
            case CMD_DUPLICATE_KIT -> {
                Kit src = KitRegistry.get(p.kitId);
                if (src == null) return;
                String newId = CustomKitManager.generateId(src.getDisplayName());
                Kit dup = new Kit(newId, src.getDisplayName() + " (Copy)", src.getIconId(), true);
                src.getItems().forEach(dup::addItem);
                KitRegistry.register(dup);
            }
            case CMD_SAVE_KIT -> {
                Kit kit = KitRegistry.get(p.kitId);
                if (kit != null && kit.isCustom()) CustomKitManager.saveKit(kit);
            }
            case CMD_UPDATE_SLOT -> {
                Kit kit = KitRegistry.get(p.kitId);
                if (kit == null || !kit.isCustom()) return;
                try {
                    JsonObject obj = GSON.fromJson(p.payload, JsonObject.class);
                    int slot = obj.get("slot").getAsInt();
                    RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, server.registryAccess());
                    ItemStack stack = ItemStack.CODEC.parse(ops, obj.get("stack"))
                            .resultOrPartial(e -> {}).orElse(null);
                    if (stack != null && !stack.isEmpty())
                        kit.addItem(slot, KitStack.fromStack(stack, slot));
                    else
                        kit.getItems().remove(slot);
                    KitRegistry.register(kit);
                } catch (Exception e) { e.printStackTrace(); }
            }
            case CMD_LOAD_KIT -> {
                try {
                    JsonObject opts = GSON.fromJson(p.payload, JsonObject.class);
                    String  target      = opts.get("target").getAsString();
                    boolean unbreaking  = opts.get("unbreaking").getAsBoolean();
                    boolean mending     = opts.get("mending").getAsBoolean();
                    boolean unbreakable = opts.get("unbreakable").getAsBoolean();
                    switch (target) {
                        case "MYSELF"      -> KitGiver.giveKit(c.player(), p.kitId, unbreakable, unbreaking, mending);
                        case "ALL_PLAYERS" -> server.getPlayerList().getPlayers().forEach(
                                pl -> {if(!p.isCustom && pl != PlayerBot.playerBot) KitGiver.giveKit(pl, p.kitId, unbreakable, unbreaking,  mending);});
                        case "MACEBOT" -> {
                            if(PlayerBot.playerBot != null) KitGiver.giveKit(PlayerBot.playerBot, p.kitId, unbreakable, unbreaking, mending);
                        }
                    }
                } catch (Exception e) { e.printStackTrace(); }
            }
        }
    }

    // ── C2S factories ─────────────────────────────────────────────────────────

    public static KitSyncC2SPacket requestResync() {
        return new KitSyncC2SPacket(CMD_RESYNC_KITS, "","","","{}",true);
    }

    public static KitSyncC2SPacket syncKit(Kit kit) {
        return new KitSyncC2SPacket(CMD_SYNC_KIT, kit.getId(), kit.getDisplayName(),
                orEmpty(kit.getIconId().getPath()), serializeItemsInClientSide(kit), kit.isCustom());
    }

    public static KitSyncC2SPacket deleteKit(String kitId) {
        return new KitSyncC2SPacket(CMD_DELETE_KIT, kitId, "", "", "{}", false);
    }

    public static KitSyncC2SPacket duplicateKit(String kitId) {
        return new KitSyncC2SPacket(CMD_DUPLICATE_KIT, kitId, "", "", "{}", false);
    }

    public static KitSyncC2SPacket openEditor(String kitId) {
        return new KitSyncC2SPacket(CMD_OPEN_EDITOR, kitId, "", "", "{}", false);
    }

    public static KitSyncC2SPacket openViewer(String kitId) {
        return new KitSyncC2SPacket(CMD_OPEN_VIEWER, kitId, "", "", "{}", false);
    }

    public static KitSyncC2SPacket saveKit(String kitId) {
        return new KitSyncC2SPacket(CMD_SAVE_KIT, kitId, "", "", "{}", false);
    }

    public static KitSyncC2SPacket updateSlot(String kitId, int slot, ItemStack stack) {
        JsonObject obj = new JsonObject();
        obj.addProperty("slot", slot);
        try {
            RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, MaceBot.server.registryAccess());
            JsonElement el = ItemStack.CODEC.encodeStart(ops, stack).resultOrPartial(e -> {}).orElse(null);
            if (el != null) obj.add("stack", el);
        } catch (Exception e) { e.printStackTrace(); }
        return new KitSyncC2SPacket(CMD_UPDATE_SLOT, kitId, "", "", obj.toString(), false);
    }

    public static KitSyncC2SPacket loadKit(String kitId, String target,
                                           boolean unbreaking, boolean mending, boolean unbreakable) {
        JsonObject opts = new JsonObject();
        opts.addProperty("target", target);
        opts.addProperty("unbreaking", unbreaking);
        opts.addProperty("mending", mending);
        opts.addProperty("unbreakable", unbreakable);
        return new KitSyncC2SPacket(CMD_LOAD_KIT, kitId, "", "", opts.toString(), false);
    }

    // ── S2C factories ─────────────────────────────────────────────────────────

    public static KitSyncC2SPacket kitData(Kit kit) {
        return new KitSyncC2SPacket(CMD_KIT_DATA, kit.getId(), kit.getDisplayName(),
                orEmpty(kit.getIconId().getNamespace()), serializeItems(kit), kit.isCustom());
    }


    public static KitSyncC2SPacket reset() {
        return new KitSyncC2SPacket(CMD_RESET, "", "", "", "{}", false);
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
                JsonElement el = ItemStack.CODEC.encodeStart(ops, stack).getOrThrow();
                if (el != null) root.add(String.valueOf(e.getKey()), el);
            }
            return root.toString();
        } catch (Exception e) { return "{}"; }
    }

    private static String serializeItemsInClientSide(Kit kit) {
        if (MaceBot.server == null) return "{}";
        try {
            JsonObject root = new JsonObject();
            RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, Minecraft.getInstance().level.registryAccess());
            for (Map.Entry<Integer, KitStack> e : kit.getItems().entrySet()) {
                ItemStack stack = e.getValue().toStack();
                if (stack == null || stack.isEmpty()) continue;
                JsonElement el = ItemStack.CODEC.encodeStart(ops, stack)
                        .resultOrPartial(err -> MaceBot.LOGGER.warn("[KitSync] slot " + e.getKey() + " encode error: " + err))
                        .orElse(null);                if (el != null) root.add(String.valueOf(e.getKey()), el);
            }
            return root.toString();
        } catch (Exception e) { return "{}"; }
    }

    public static void deserializeItems(Kit kit, String json, RegistryAccess rm) {
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

    private static String orEmpty(String s) { return s != null ? s : ""; }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}