package net.katch0420.macebot.main.kits.client.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryOps;
import net.minecraft.client.MinecraftClient;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Client-side user preset items — ItemStacks the player has saved for quick
 * reuse in the Kit Editor browser. Persisted locally as JSON; no server sync.
 */
public class ClientPresetRegistry {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance()
            .getConfigDir().resolve("macebot").resolve("item_presets.json");

    private static final List<ItemStack> PRESETS = new ArrayList<>();
    private static final List<Runnable>  LISTENERS = new ArrayList<>();

    // ── Access ────────────────────────────────────────────────────────────────

    public static List<ItemStack> all() {
        return Collections.unmodifiableList(PRESETS);
    }

    public static void add(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return;
        // Prevent exact duplicates (same item + same components)
        for (ItemStack existing : PRESETS) {
            if (ItemStack.areItemsAndComponentsEqual(existing, stack)) return;
        }
        PRESETS.add(stack.copy());
        save();
        notify_();
    }

    public static void remove(int index) {
        if (index < 0 || index >= PRESETS.size()) return;
        PRESETS.remove(index);
        save();
        notify_();
    }

    public static boolean contains(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        for (ItemStack existing : PRESETS) {
            if (ItemStack.areItemsAndComponentsEqual(existing, stack)) return true;
        }
        return false;
    }

    public static int indexOf(ItemStack stack) {
        for (int i = 0; i < PRESETS.size(); i++) {
            if (ItemStack.areItemsAndComponentsEqual(PRESETS.get(i), stack)) return i;
        }
        return -1;
    }

    // ── Persistence ───────────────────────────────────────────────────────────

    public static void load() {
        PRESETS.clear();
        if (!Files.exists(FILE)) return;

        try (Reader reader = Files.newBufferedReader(FILE)) {
            JsonArray arr = GSON.fromJson(reader, JsonArray.class);
            if (arr == null) return;

            RegistryOps<JsonElement> ops = RegistryOps.of(
                    com.mojang.serialization.JsonOps.INSTANCE,
                    MinecraftClient.getInstance().world.getRegistryManager());

            for (JsonElement el : arr) {
                ItemStack stack = ItemStack.CODEC.parse(ops, el)
                        .resultOrPartial(e -> System.err.println("[MaceBot] preset load error: " + e))
                        .orElse(null);
                if (stack != null && !stack.isEmpty()) PRESETS.add(stack);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void save() {
        if (MinecraftClient.getInstance().world == null) return;
        try {
            Files.createDirectories(FILE.getParent());
            RegistryOps<JsonElement> ops = RegistryOps.of(
                    com.mojang.serialization.JsonOps.INSTANCE,
                    MinecraftClient.getInstance().world.getRegistryManager());

            JsonArray arr = new JsonArray();
            for (ItemStack stack : PRESETS) {
                JsonElement el = ItemStack.CODEC.encodeStart(ops, stack)
                        .resultOrPartial(e -> System.err.println("[MaceBot] preset save error: " + e))
                        .orElse(null);
                if (el != null) arr.add(el);
            }
            try (Writer writer = Files.newBufferedWriter(FILE)) {
                GSON.toJson(arr, writer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ── Listeners ─────────────────────────────────────────────────────────────

    public static void addListener(Runnable r) { LISTENERS.add(r); }
    public static void removeListener(Runnable r) { LISTENERS.remove(r); }
    private static void notify_() { LISTENERS.forEach(Runnable::run); }
}