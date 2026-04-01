package net.katch0420.macebot.main.kits.client.data;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class ClientKitRegistry {
    private static final Map<String, KitData> KITS = new HashMap<>();

    public static void register(KitData kit) {
        KITS.put(kit.getId(), kit);
    }

    public static KitData get(String id) {
        return KITS.get(id);
    }

    public static void replaceOrElseRegister(KitData kit){
        if(KITS.containsKey(kit.getId())){
            KITS.replace(kit.getId(),kit);
        } else {
            register(kit);
        }
    }

    public static void updateOrElseRegister(KitData kit){
        if(KITS.containsKey(kit.getId())){
            KITS.get(kit.getId()).setIconItem(kit.getIconItem());
            KITS.get(kit.getId()).setDisplayName(kit.getDisplayName());
        } else {
            register(kit);
        }
    }
    public static Collection<KitData> all() {
        return KITS.values();
    }

    public static void unregister(String id) {
        KITS.remove(id);
    }

    public static void deleteAll() {
        KITS.clear();
    }
}
