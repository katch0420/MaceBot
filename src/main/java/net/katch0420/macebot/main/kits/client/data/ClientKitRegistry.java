package net.katch0420.macebot.main.kits.client.data;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.katch0420.macebot.client.gui.bodies.Bodies;
import net.katch0420.macebot.client.gui.bodies.KitsBody;
import net.katch0420.macebot.client.gui.frames.MainFrame;
import net.katch0420.macebot.main.kits.main.Kit;
import net.katch0420.macebot.main.networking.packets.c2s.KitSyncC2SPacket;
import net.katch0420.macebot.main.networking.packets.s2c.KitSyncS2CPacket;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Client-side kit registry. Handles incoming KitSyncC2SPacket S2C packets
 * (KIT_DATA and RESET) and notifies any registered listeners so open
 * screens can refresh without polling.
 */
@Environment(EnvType.CLIENT)
public class ClientKitRegistry {

    private static final Map<String, Kit> KITS = new LinkedHashMap<>();
    private static final List<Runnable> listeners = new CopyOnWriteArrayList<>();

    // ── Data access ───────────────────────────────────────────────────────────

    public static void register(Kit kit) {
        KITS.put(kit.getId(), kit);
        notifyListeners();
    }

    public static Kit get(String id) { return KITS.get(id); }

    public static Collection<Kit> all() { return Collections.unmodifiableCollection(KITS.values()); }

    public static Collection<String> allKitIds() {return Collections.unmodifiableCollection(KITS.keySet());}

    public static void unregister(String id) {
        KITS.remove(id);
        notifyListeners();
    }

    public static void clear() {
        KITS.clear();
        notifyListeners();
    }

    /** Returns all kits split: built-ins first, then custom alphabetically. */
    public static List<Kit> allSorted() {
        List<Kit> builtIn = new ArrayList<>();
        List<Kit> custom  = new ArrayList<>();
        for (Kit k : KITS.values()) {
            (k.isCustom() ? custom : builtIn).add(k);
        }
        custom.sort(Comparator.comparing(Kit::getDisplayName));
        List<Kit> result = new ArrayList<>(builtIn);
        result.addAll(custom);
        return result;
    }


    // ── Change listeners ──────────────────────────────────────────────────────

    /** Register a callback fired on the main thread when any kit changes. */
    public static void addListener(Runnable r) { listeners.add(r); }

    public static void removeListener(Runnable r) { listeners.remove(r); }

    public static void notifyListeners() {
        if (!listeners.isEmpty()) {
            for (Runnable r : new ArrayList<>(listeners)) {
                r.run();
            }
        }
    }

    public static void handleClient(KitSyncS2CPacket p, ClientPlayNetworking.Context c) {
        MinecraftClient mc = c.client();
        mc.execute(() -> {
            switch (p.command()) {
                case KitSyncC2SPacket.CMD_RESET -> clear();
                case KitSyncC2SPacket.CMD_KIT_DATA -> {
                    if (p.kitId().isEmpty()) return;
                    Kit existing = KITS.get(p.kitId());
                    if (existing != null) {
                        existing.setDisplayName(p.displayName());
                        existing.setIconItem(Identifier.of(p.iconItem()));
                    } else {
                        KITS.put(p.kitId(), new Kit(p.kitId(), p.displayName(),
                                Identifier.of(p.iconItem()), p.isCustom()));
                    }
                    notifyListeners();
                }
                case KitSyncS2CPacket.CMD_DELETE_KIT -> {
                    ClientKitRegistry.unregister(p.kitId());
                    ((KitsBody)Bodies.KITS).rebuildKitList();
                }
                case KitSyncS2CPacket.CMD_KIT_ITEMS -> {
                    if (p.kitId().isEmpty()) return;
                    Kit kit = KITS.get(p.kitId());
                    if (kit == null) return;
                    kit.clear();
                    if (!p.payload().equals("{}"))
                        KitSyncC2SPacket.deserializeItems(kit, p.payload(),
                                Objects.requireNonNull(c.client().world).getRegistryManager());
                    notifyListeners();
                }
            }
        });
    }
}