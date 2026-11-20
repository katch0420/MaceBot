package net.katch0420.macebot.client.gui.themes;

import java.util.ArrayList;
import java.util.List;

/** Registry of editable theme colors, their stable IDs, and editor groups. */
public final class ThemeSettings {
    private static final List<ColorSetting> SETTINGS = new ArrayList<>();

    static {
        register("screen_foreground", "Foreground", "Screen/General");
        register("screen_background", "Background", "Screen/General");
        register("header_foreground", "Foreground", "Screen/Header");
        register("header_background", "Background", "Screen/Header");
        register("footer_foreground", "Foreground", "Screen/Footer");
        register("footer_background", "Background", "Screen/Footer");
        register("sidebar_foreground", "Foreground", "Screen/Sidebar");
        register("sidebar_background", "Background", "Screen/Sidebar");
        register("panel_separator", "Panel Separator", "Screen/General");
        register("body_background", "Background", "Screen/Bodies/Panel");
        register("body_foreground", "Foreground", "Screen/Bodies/Panel");
        register("body_border", "Border", "Screen/Bodies/Panel");
        register("display_background", "Display Background", "Screen/Bodies/Panel");
        register("body_key", "Key", "Screen/Bodies/Panel");
        register("body_value", "Value", "Screen/Bodies/Panel");
        register("body_label", "Label", "Screen/Bodies/Panel");
        register("body_category_label", "Category Label", "Screen/Bodies/Panel");
        register("body_button_background", "Background", "Screen/Bodies/Buttons");
        register("body_button_foreground", "Foreground", "Screen/Bodies/Buttons");
        register("body_button_border", "Border", "Screen/Bodies/Buttons");
        register("accent", "Accent", "Accents & Status/Accent");
        register("accent_hover", "Accent (Hover)", "Accents & Status/Accent");
        register("success", "Success", "Accents & Status/Status");
        register("warning", "Warning", "Accents & Status/Status");
        register("danger", "Danger", "Accents & Status/Status");
        register("disabled_overlay", "Disabled Overlay", "Accents & Status/Interaction");
        register("focus_ring", "Focus Ring", "Accents & Status/Interaction");
        register("custom_hitbox", "Custom HitBox", "World Render/Entity/Target");
    }

    private ThemeSettings() {}

    public static List<ColorSetting> all() {
        return List.copyOf(SETTINGS);
    }

    public static void register(String id, String name, String groupPath) {
        register(id, name, groupPath, 0xFFFFFFFF);
    }

    public static void register(String id, String name, String groupPath, int defaultValue) {
        if (SETTINGS.stream().anyMatch(setting -> setting.id().equals(id))) {
            throw new IllegalArgumentException("Theme setting already registered: " + id);
        }
        SETTINGS.add(new ColorSetting(id, name, groupPath, defaultValue));
    }

    public record ColorSetting(String id, String name, String groupPath, int defaultValue) {}
}
