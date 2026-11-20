package net.katch0420.macebot.main.settings;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.katch0420.macebot.main.MaceBot;
import net.katch0420.macebot.main.network.packets.s2c.SettingS2CPacket;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SettingManager {

    public static final Map<String, Setting<?>> settings = new HashMap<>();

    @SuppressWarnings("unchecked")
    public static <T> Setting<T> register(Setting<T> s){
        if(settings.containsKey(s.getId())){
            MaceBot.debugLog("Setting already exists! : " + s.getId());
            return (Setting<T>) settings.get(s.getId());
        }
        settings.put(s.getId(), s);
        s.getCategory().addSetting(s);
        return s;
    }

    @SuppressWarnings("unchecked")
    public static <T> Setting<T> getSetting(String id){
        var v = settings.get(id);
        if(v == null) MaceBot.debugLog("No setting with id : " + id);
        return (Setting<T>) v;
    }

    public static Collection<Setting<?>> getSettings(){
        return settings.values();
    }

    public static List<Setting<?>> getSettings(SettingsCategories.SettingCategory category) {
        return category.settings;
    }

    public static Object getValue(String id){
        return getSetting(id).getValue();
    }

    public static Object getValue(Setting<?> s){
        return s.getValue();
    }

    public static void resyncForAll(){
        getSettings().stream().filter(s -> !s.hasFlag(Flags.SETTING_SYNC_NOT_NECESSARY)).forEach(Setting::updateToClients);
    }

    public static void resyncFor(ServerPlayer player) {
        getSettings().stream().filter(s -> !s.hasFlag(Flags.SETTING_SYNC_NOT_NECESSARY)).forEach(
                s -> ServerPlayNetworking.send(player, new SettingS2CPacket(s.getId(), s.serialize()))
        );
    }
}
