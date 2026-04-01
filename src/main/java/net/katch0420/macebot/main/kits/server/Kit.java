package net.katch0420.macebot.main.kits.server;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.HashMap;
import java.util.Map;

public class Kit {
    private final String id;
    private String displayName;
    private String iconItem; // optional for GUI icon
    private final boolean custom;
    private Map<Integer, KitStack> items = new HashMap<>();

    public void setDisplayName(String name) { this.displayName = name; }
    public void setIconItem(String iconItem) { this.iconItem = iconItem; }

    public Kit(String id, String displayName, String iconItem, boolean custom) {
        this.id = id;
        this.displayName = displayName;
        this.iconItem = iconItem;
        this.custom = custom;
    }

    public void addItem(int index, KitStack stack) {
        items.put(index, stack);
    }

    public Map<Integer, KitStack> getItems() {
        return items;
    }

    public Kit copy(){
        Kit kit = new Kit(id, displayName,iconItem, custom);
        kit.items = this.items;
        return kit;
    }

    public void clear(){
        items.clear();
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getIconItem() { return iconItem; }

    public boolean isCustom() {
        return custom;
    }
}
