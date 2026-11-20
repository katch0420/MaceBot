package net.katch0420.macebot.client.gui.themes;

import java.util.LinkedHashMap;
import java.util.Map;

/** A theme palette whose colors are addressed by registered setting IDs. */
public class Theme {
    private boolean builtIn;
    private final Map<String, Integer> colors = new LinkedHashMap<>();

    public Theme(boolean builtIn, int... values) {
        if (values.length > ThemeSettings.all().size()) {
            throw new IllegalArgumentException("Received more theme colors than are registered");
        }
        this.builtIn = builtIn;
        for (int i = 0; i < values.length; i++) {
            ThemeSettings.ColorSetting setting = ThemeSettings.all().get(i);
            colors.put(setting.id(), i < values.length ? values[i] : setting.defaultValue());
        }
    }

    public boolean isBuiltIn() { return builtIn; }
    public void setBuiltIn(boolean builtIn) { this.builtIn = builtIn; }

    public int getColor(String id) {
        Integer color = colors.get(id);
        if (color != null) return color;
        return ThemeSettings.all().stream()
                .filter(setting -> setting.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown theme color: " + id))
                .defaultValue();
    }

    public void setColor(String id, int color) {
        if (ThemeSettings.all().stream().noneMatch(setting -> setting.id().equals(id))) {
            throw new IllegalArgumentException("Unknown theme color: " + id);
        }
        colors.put(id, color);
    }

    public Map<String, Integer> getColors() {
        Map<String, Integer> result = new LinkedHashMap<>();
        for (ThemeSettings.ColorSetting setting : ThemeSettings.all()) {
            result.put(setting.id(), getColor(setting.id()));
        }
        return Map.copyOf(result);
    }

    public Theme copy() {
        int[] values = ThemeSettings.all().stream().mapToInt(setting -> getColor(setting.id())).toArray();
        return new Theme(builtIn, values);
    }

    public int screen_foreground() { return getColor("screen_foreground"); }
    public void set_screen_foreground(int v) { setColor("screen_foreground", v); }
    public int screen_background() { return getColor("screen_background"); }
    public void set_screen_background(int v) { setColor("screen_background", v); }
    public int header_foreground() { return getColor("header_foreground"); }
    public void set_header_foreground(int v) { setColor("header_foreground", v); }
    public int header_background() { return getColor("header_background"); }
    public void set_header_background(int v) { setColor("header_background", v); }
    public int footer_foreground() { return getColor("footer_foreground"); }
    public void set_footer_foreground(int v) { setColor("footer_foreground", v); }
    public int footer_background() { return getColor("footer_background"); }
    public void set_footer_background(int v) { setColor("footer_background", v); }
    public int sidebar_foreground() { return getColor("sidebar_foreground"); }
    public void set_sidebar_foreground(int v) { setColor("sidebar_foreground", v); }
    public int sidebar_background() { return getColor("sidebar_background"); }
    public void set_sidebar_background(int v) { setColor("sidebar_background", v); }
    public int panel_separator() { return getColor("panel_separator"); }
    public void set_panel_separator(int v) { setColor("panel_separator", v); }
    public int body_background() { return getColor("body_background"); }
    public void set_body_background(int v) { setColor("body_background", v); }
    public int body_foreground() { return getColor("body_foreground"); }
    public void set_body_foreground(int v) { setColor("body_foreground", v); }
    public int body_border() { return getColor("body_border"); }
    public void set_body_border(int v) { setColor("body_border", v); }
    public int display_background() { return getColor("display_background"); }
    public void set_display_background(int v) { setColor("display_background", v); }
    public int body_key() { return getColor("body_key"); }
    public void set_body_key(int v) { setColor("body_key", v); }
    public int body_value() { return getColor("body_value"); }
    public void set_body_value(int v) { setColor("body_value", v); }
    public int body_label() { return getColor("body_label"); }
    public void set_body_label(int v) { setColor("body_label", v); }
    public int body_category_label() { return getColor("body_category_label"); }
    public void set_body_category_label(int v) { setColor("body_category_label", v); }
    public int body_button_background() { return getColor("body_button_background"); }
    public void set_body_button_background(int v) { setColor("body_button_background", v); }
    public int body_button_foreground() { return getColor("body_button_foreground"); }
    public void set_body_button_foreground(int v) { setColor("body_button_foreground", v); }
    public int body_button_border() { return getColor("body_button_border"); }
    public void set_body_button_border(int v) { setColor("body_button_border", v); }
    public int accent() { return getColor("accent"); }
    public void set_accent(int v) { setColor("accent", v); }
    public int accent_hover() { return getColor("accent_hover"); }
    public void set_accent_hover(int v) { setColor("accent_hover", v); }
    public int success() { return getColor("success"); }
    public void set_success(int v) { setColor("success", v); }
    public int warning() { return getColor("warning"); }
    public void set_warning(int v) { setColor("warning", v); }
    public int danger() { return getColor("danger"); }
    public void set_danger(int v) { setColor("danger", v); }
    public int disabled_overlay() { return getColor("disabled_overlay"); }
    public void set_disabled_overlay(int v) { setColor("disabled_overlay", v); }
    public int focus_ring() { return getColor("focus_ring"); }
    public void set_focus_ring(int v) { setColor("focus_ring", v); }
    public int custom_hitbox() { return getColor("custom_hitbox"); }
    public void set_custom_hitbox(int v) { setColor("custom_hitbox", v); }
}
