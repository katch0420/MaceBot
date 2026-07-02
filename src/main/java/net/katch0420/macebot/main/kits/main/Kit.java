package net.katch0420.macebot.main.kits.main;

import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public class Kit {
    private final String id;
    private String displayName;
    private Identifier iconId; // optional for GUI icon
    private final boolean custom;
    private Map<Integer, KitStack> items = new HashMap<>();

    public void setDisplayName(String name) { this.displayName = name; }
    public void setIconId(Identifier iconId) { this.iconId = iconId; }

    public Kit(String id, String displayName, Identifier iconId, boolean custom) {
        this.id = id;
        this.displayName = displayName;
        this.iconId = iconId;
        this.custom = custom;
    }

    public void addItem(int index, KitStack stack) {
        items.put(index, stack);
    }

    public Map<Integer, KitStack> getItems() {
        return items;
    }

    public Kit copy(){
        Kit kit = new Kit(id, displayName, iconId, custom);
        kit.items = this.items;
        return kit;
    }

    public void clear(){
        items.clear();
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public Identifier getIconId() { return iconId; }

    public boolean isCustom() {
        return custom;
    }

    public void setIconItem(Identifier iconItem) {
        this.iconId = iconItem;
    }
}
