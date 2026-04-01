package net.katch0420.macebot.main.kits.server;

import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import net.fabricmc.loader.api.FabricLoader;
import net.katch0420.macebot.main.MaceBot;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.*;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class CustomKitManager {

    private static final Path KITS_DIR = FabricLoader.getInstance()
            .getConfigDir().resolve("macebot").resolve("kits");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static DynamicRegistryManager registryManager;

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
        root.addProperty("iconItem",    kit.getIconItem() != null ? kit.getIconItem() : "");

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
            registryManager = MaceBot.server.getRegistryManager();
            if (registryManager == null) {
                System.err.println("[MaceBot] registryManager not set — cannot serialize");
                return null;
            }

            ItemData data = kitStack.itemData;

            // Build a temporary ItemStack so we can use ItemStack.CODEC
            // This avoids needing a PlayerEntity — registryManager is enough
            Item item = Registries.ITEM.get(data.id);
            ItemStack stack = new ItemStack(item, kitStack.count);

            // Apply stored components (custom name, lore, potion contents, etc.)
            if (data.components != null && !data.components.isEmpty()) {
                stack.applyComponentsFrom(data.components);
            }

            // Apply enchantments using registryManager (no player needed)
            if (data.enchantments != null && !data.enchantments.isEmpty()) {
                Registry<Enchantment> registry = registryManager.getOrThrow(RegistryKeys.ENCHANTMENT);
                ItemEnchantmentsComponent.Builder builder =
                        new ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT);

                for (Map.Entry<RegistryKey<Enchantment>, Integer> entry : data.enchantments.entrySet()) {
                    registry.getEntry(entry.getKey().getValue()).ifPresent(e -> builder.add(e, entry.getValue()));
                }
                stack.set(DataComponentTypes.ENCHANTMENTS, builder.build());
            }

            // Encode the full stack with ItemStack.CODEC — handles everything
            RegistryOps<JsonElement> ops = RegistryOps.of(JsonOps.INSTANCE, registryManager);
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

            Kit kit = new Kit(id, displayName, iconItem, true);

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
            registryManager = MaceBot.server.getRegistryManager();

            if (registryManager == null) {
                System.err.println("[MaceBot] registryManager not set — cannot deserialize");
                return null;
            }

            RegistryOps<JsonElement> ops = RegistryOps.of(JsonOps.INSTANCE, registryManager);

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