package net.katch0420.macebot.client.gui.themes;

public final class ThemeTree {
    private ThemeTree() {}

    public static ThemeGroup build(Theme theme) {
        ThemeGroup root = new ThemeGroup("Theme");
        for (ThemeSettings.ColorSetting setting : ThemeSettings.all()) {
            ThemeGroup group = root;
            for (String name : setting.groupPath().split("/")) {
                ThemeGroup parent = group;
                group = parent.children.stream()
                        .filter(child -> child.name.equals(name))
                        .findFirst()
                        .orElseGet(() -> parent.group(name));
            }
            group.field(setting.name(), () -> theme.getColor(setting.id()), color -> theme.setColor(setting.id(), color));
        }
        return root;
    }
}
