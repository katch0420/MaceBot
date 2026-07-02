package net.katch0420.macebot.main.kits.server;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.katch0420.macebot.main.MaceBot;
import net.katch0420.macebot.main.kits.main.Kit;
import net.katch0420.macebot.main.networking.packets.s2c.KitSyncS2CPacket;

import java.util.*;

public class KitRegistry {
    private static final Map<String, Kit> KITS = new HashMap<>();

    public static void registerTickEvent() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTicks() % 300 == 0) {
                server.getPlayerManager().getPlayerList().forEach(p -> {
                    ServerPlayNetworking.send(p, KitSyncS2CPacket.reset());
                    KITS.values().forEach(k -> {
                        ServerPlayNetworking.send(p, KitSyncS2CPacket.kitData(k));
                        ServerPlayNetworking.send(p, KitSyncS2CPacket.kitItems(k));
                    });
                });
            }
            if (server.getTicks() % 3000 == 0) {
                KITS.values().forEach(k -> { if (k.isCustom()) CustomKitManager.saveKit(k); });
            }
        });
    }

    public static void register(Kit kit) {
        KITS.put(kit.getId(), kit);
        syncToClients(kit);
        if (kit.isCustom()) CustomKitManager.saveKit(kit);
    }

    public static void replaceOrElseRegister(Kit kit) {
        if (KITS.containsKey(kit.getId())) {
            KITS.replace(kit.getId(), kit);
            syncToClients(KITS.get(kit.getId()));
        } else {
            register(kit);
        }
    }

    public static void updateOrElseRegister(Kit kit) {
        if (KITS.containsKey(kit.getId())) {
            KITS.get(kit.getId()).setIconItem(kit.getIconId());
            KITS.get(kit.getId()).setDisplayName(kit.getDisplayName());
            syncToClients(KITS.get(kit.getId()));
        } else {
            register(kit);
        }
    }

    public static Kit get(String id) { return KITS.get(id); }

    public static Collection<Kit> all() { return KITS.values(); }

    public static void unregister(String id) {
        Kit kit = KITS.get(id);
        if (kit == null) return;
        if (kit.isCustom()) CustomKitManager.deleteKit(id);
        KITS.remove(id);
        // Tell clients to remove it
        MaceBot.server.getPlayerManager().getPlayerList().forEach(p ->
                ServerPlayNetworking.send(p, KitSyncS2CPacket.deleteKit(id)));
    }

    public static Iterable<String> getAllIds() { return new HashSet<>(KITS.keySet()); }

    // ── Internal ──────────────────────────────────────────────────────────────

    private static void syncToClients(Kit kit) {
        if (MaceBot.server == null) return;
        MaceBot.server.getPlayerManager().getPlayerList().forEach(p -> {
            ServerPlayNetworking.send(p, KitSyncS2CPacket.kitData(kit));
            ServerPlayNetworking.send(p, KitSyncS2CPacket.kitItems(kit));
        });
    }

    public static void syncAllKitsToClients(){
        MaceBot.server.getPlayerManager().getPlayerList().forEach(p -> {
            ServerPlayNetworking.send(p, KitSyncS2CPacket.reset());
            KITS.values().forEach(k -> {
                ServerPlayNetworking.send(p, KitSyncS2CPacket.kitData(k));
                ServerPlayNetworking.send(p, KitSyncS2CPacket.kitItems(k));
            });
        });
    }
}