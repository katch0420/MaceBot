package net.katch0420.macebot.main.settings;


import net.katch0420.macebot.main.MaceBot;
import net.minecraft.network.chat.Component;

import java.util.*;

public class SettingsCategories {

    private static final Map<String, SettingCategory> categories = new HashMap<>();

    public static SettingCategory MACEBOT = register(new SettingCategory("bot", "MaceBot"));
    public static SettingCategory STATUS = register(new SettingCategory("status", "Status", Flags.CATEGORY_DO_NOT_DISPLAY));
    public static SettingCategory MOD = register(new SettingCategory("mod", "Mod"));
    public static SettingCategory PLAYER = register(new SettingCategory("player", "Player"));

    private static SettingCategory register(SettingCategory c) {
        if(categories.containsKey(c.getId())){
            MaceBot.debugLog("Setting already exists! : " + c.getId());
            return categories.get(c.getId());
        }
        categories.put(c.getId(), c);
        return c;
    }

    public static Collection<SettingCategory> getCategories() {
        return categories.values();
    }

    public static SettingCategory getCategory(String id){
        var v = categories.get(id);
        if(v == null) MaceBot.debugLog("No category with id : " + id);
        return v;
    }

    public static class SettingCategory{
        private final String id;
        private Component displayName;
        public ArrayList<Setting<?>> settings = new ArrayList<>();
        private final FlagContainer flags = new FlagContainer();

        public SettingCategory(String id, Component displayName) {
            this.id = id;
            this.displayName = displayName;
        }

        public SettingCategory(String id, String displayName) {
            this.id = id;
            this.displayName = Component.nullToEmpty(displayName);
        }

        public SettingCategory(String id, String displayName, Flags.Flag... flags) {
            this.id = id;
            this.displayName = Component.nullToEmpty(displayName);
            this.flags.addFlags(flags);
        }

        public void addSetting(Setting<?> s){
            settings.add(s);
        }

        public String getId() {
            return id;
        }

        public Component getDisplayName() {
            return displayName;
        }

        public void setDisplayName(Component displayName) {
            this.displayName = displayName;
        }

        public boolean hasFlag(Flags.Flag flag){
            return flags.contains(flag);
        }
    }
}