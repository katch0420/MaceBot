package net.katch0420.macebot.main.kits.server;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.katch0420.macebot.main.kits.main.KitSyncManager;

import java.util.*;

public class KitRegistry {
    private static final Map<String, Kit> KITS = new HashMap<>();

    public static void registerTickEvent(){
        ServerTickEvents.END_SERVER_TICK.register(
                server -> {
                    if(server.getTicks() % 300 == 0){
                        KitSyncManager.resyncAllKitsToClients(KITS.values());
                    }
                    if(server.getTicks() % 3000 == 0){
                        KITS.values().forEach(
                                k -> {if(k.isCustom()) CustomKitManager.saveKit(k);});
                    }
                }
        );
    }

    public static void register(Kit kit) {
        KITS.put(kit.getId(), kit);
        KitSyncManager.syncKitToClients(kit);
        if(kit.isCustom())CustomKitManager.saveKit(kit);
    }

    public static void replaceOrElseRegister(Kit kit){
        if(KITS.containsKey(kit.getId())){
            KITS.replace(kit.getId(),kit);
            KitSyncManager.syncKitToClients(KITS.get(kit.getId()));
        } else {
            register(kit);
        }
    }

    public static void updateOrElseRegister(Kit kit){
        if(KITS.containsKey(kit.getId())){
            KITS.get(kit.getId()).setIconItem(kit.getIconItem());
            KITS.get(kit.getId()).setDisplayName(kit.getDisplayName());
            KitSyncManager.syncKitToClients(KITS.get(kit.getId()));
        } else {
            register(kit);
        }
    }

    public static Kit get(String id) {
        return KITS.get(id);
    }

    public static Collection<Kit> all() {
        return KITS.values();
    }

    public static void unregister(String id) {
        if(KITS.get(id).isCustom()){
            CustomKitManager.deleteKit(id);
        }
        KITS.remove(id);
    }

    public static Iterable<String> getAllIds() {
        return new HashSet<>(KITS.keySet());
    }
}
