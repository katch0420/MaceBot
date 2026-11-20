package net.katch0420.macebot.main.kits.server;

import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import net.fabricmc.loader.api.FabricLoader;
import net.katch0420.macebot.main.MaceBot;
import net.katch0420.macebot.main.kits.main.ItemData;
import net.katch0420.macebot.main.kits.main.Kit;
import net.katch0420.macebot.main.kits.main.KitStack;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class CustomKitManager {

    private static final Path KITS_DIR = FabricLoader.getInstance()
            .getConfigDir().resolve("macebot").resolve("kits");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static RegistryAccess.Frozen registryManager;

    public static void init() {
        try {
            Files.createDirectories(KITS_DIR);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ── Save ──────────────────────────────────────────────────────────────────

    public static void saveKit(Kit kit) {
        JsonObject root = new JsonObject();
        root.addProperty("id",          kit.getId());
        root.addProperty("displayName", kit.getDisplayName());
        root.addProperty("iconItem",    kit.getIconId() != null ? kit.getIconId().getPath() : "");

        JsonObject items = new JsonObject();
        kit.getItems().forEach((slot, kitStack) -> {
            JsonObject stackObj = serializeKitStack(kitStack);
            if (stackObj != null) items.add(String.valueOf(slot), stackObj);
        });
        root.add("items", items);

        Path file = KITS_DIR.resolve(kit.getId() + ".json");
        try (Writer writer = Files.newBufferedWriter(file)) {
            GSON.toJson(root, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static JsonObject serializeKitStack(KitStack kitStack) {
        try {
            registryManager = MaceBot.server.registryAccess();
            if (registryManager == null) {
                System.err.println("[MaceBot] registryManager not set — cannot serialize");
                return null;
            }

            ItemStack stack = kitStack.toStack();

            RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, registryManager);
            JsonElement stackJson = ItemStack.CODEC
                    .encodeStart(ops, stack)
                    .resultOrPartial(err -> System.err.println("[MaceBot] encode error: " + err))
                    .orElse(null);

            if (stackJson == null) return null;

            JsonObject obj = new JsonObject();
            obj.addProperty("slot", (int) kitStack.slot);
            obj.add("stack", stackJson);
            return obj;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ── Load ──────────────────────────────────────────────────────────────────

    public static List<Kit> loadAll() {
        List<Kit> loaded = new ArrayList<>();
        if (!Files.exists(KITS_DIR)) return loaded;

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(KITS_DIR, "*.json")) {
            for (Path file : stream) {
                Kit kit = loadFromFile(file);
                if (kit != null) loaded.add(kit);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return loaded;
    }

    private static Kit loadFromFile(Path file) {
        try (Reader reader = Files.newBufferedReader(file)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();

            String id          = root.get("id").getAsString();
            String displayName = root.get("displayName").getAsString();
            String iconItem    = root.has("iconItem") ? root.get("iconItem").getAsString() : "";

            Kit kit = new Kit(id, displayName, Identifier.parse(iconItem), true);

            if (root.has("items")) {
                root.getAsJsonObject("items").entrySet().forEach(entry -> {
                    try {
                        KitStack ks = deserializeKitStack(entry.getValue().getAsJsonObject());
                        if (ks != null) kit.addItem((int) ks.slot, ks);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }

            return kit;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static KitStack deserializeKitStack(JsonObject obj) {
        try {

            int slot = obj.get("slot").getAsInt();
            registryManager = MaceBot.server.registryAccess();

            if (registryManager == null) {
                System.err.println("[MaceBot] registryManager not set — cannot deserialize");
                return null;
            }

            RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, registryManager);

            // New format — full ItemStack via codec
            if (obj.has("stack")) {
                ItemStack stack = ItemStack.CODEC
                        .parse(ops, obj.get("stack"))
                        .resultOrPartial(err -> System.err.println("[MaceBot] decode error: " + err))
                        .orElse(null);

                if (stack == null || stack.isEmpty()) return null;
                return KitStack.fromStack(stack, slot); // uses existing fromStack
            }

            // Legacy format fallback — old saves with "id"/"count"/"components" fields
            if (obj.has("id")) {
                JsonObject stackJson = new JsonObject();
                stackJson.addProperty("id", "minecraft:" + obj.get("id").getAsString());
                if (obj.has("count"))      stackJson.addProperty("count", obj.get("count").getAsInt());
                if (obj.has("components")) stackJson.add("components", obj.get("components"));

                ItemStack stack = ItemStack.CODEC
                        .parse(ops, stackJson)
                        .resultOrPartial(err -> System.err.println("[MaceBot] legacy decode error: " + err))
                        .orElse(null);

                if (stack == null || stack.isEmpty()) return null;
                return KitStack.fromStack(stack, slot);
            }

            return null;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    public static String generateId(String baseName) {
        String base      = baseName.toLowerCase().replaceAll("[^a-z0-9_]", "_");
        String candidate = base;
        int i = 1;
        while (Files.exists(KITS_DIR.resolve(candidate + ".json"))) {
            candidate = base + "_" + i++;
        }
        return candidate;
    }

    public static void deleteKit(String kitId) {
        try {
            Files.deleteIfExists(KITS_DIR.resolve(kitId + ".json"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}