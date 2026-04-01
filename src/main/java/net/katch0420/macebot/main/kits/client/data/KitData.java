package net.katch0420.macebot.main.kits.client.data;

public class KitData {
    private final String id;
    private String displayName;
    private String iconItem; // optional for GUI icon
    private final boolean custom;
    private int count;
    public KitData(String id, String displayName, String iconItem, boolean custom, int count) {
        this.id = id;
        this.custom = custom;
        this.iconItem = iconItem;
        this.displayName = displayName;
        this.count = count;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIconItem() {
        return iconItem;
    }

    public boolean isCustom() {
        return custom;
    }

    public String getId() {
        return id;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setIconItem(String iconItem) {
        this.iconItem = iconItem;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
