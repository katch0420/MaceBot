package net.katch0420.macebot.client.gui.themes;

public final class ThemeTree {
    private ThemeTree() {}

    public static ThemeGroup build(Theme theme) {
        ThemeGroup root = new ThemeGroup("Theme");

        ThemeGroup screen = root.group("Screen");
        screen.group("General")
                .field("Foreground", theme::screen_foreground, theme::set_screen_foreground)
                .field("Background", theme::screen_background, theme::set_screen_background)
                .field("Panel Separator", theme::panel_separator, theme::set_panel_separator);
        screen.group("Header")
                .field("Foreground", theme::header_foreground, theme::set_header_foreground)
                .field("Background", theme::header_background, theme::set_header_background);
        screen.group("Footer")
                .field("Foreground", theme::footer_foreground, theme::set_footer_foreground)
                .field("Background", theme::footer_background, theme::set_footer_background);
        screen.group("Sidebar")
                .field("Foreground", theme::sidebar_foreground, theme::set_sidebar_foreground)
                .field("Background", theme::sidebar_background, theme::set_sidebar_background);

        ThemeGroup bodys = screen.group("Bodys");
        bodys.group("Panel")
                .field("Background", theme::body_background, theme::set_body_background)
                .field("Foreground", theme::body_foreground, theme::set_body_foreground)
                .field("Border", theme::body_border, theme::set_body_border)
                .field("Display Background", theme::display_background, theme::set_display_background)
                .field("Key", theme::body_key, theme::set_body_key)
                .field("Value", theme::body_value, theme::set_body_value)
                .field("Label", theme::body_label, theme::set_body_label)
                .field("Category Label", theme::body_category_label, theme::set_body_category_label);
        bodys.group("Buttons")
                .field("Background", theme::body_button_background, theme::set_body_button_background)
                .field("Foreground", theme::body_button_foreground, theme::set_body_button_foreground)
                .field("Border", theme::body_button_border, theme::set_body_button_border);

        root.group("Hud"); // placeholder

        ThemeGroup accents = root.group("Accents & Status");
        accents.group("Accent")
                .field("Accent", theme::accent, theme::set_accent)
                .field("Accent (Hover)", theme::accent_hover, theme::set_accent_hover);
        accents.group("Status")
                .field("Success", theme::success, theme::set_success)
                .field("Warning", theme::warning, theme::set_warning)
                .field("Danger", theme::danger, theme::set_danger);
        accents.group("Interaction")
                .field("Disabled Overlay", theme::disabled_overlay, theme::set_disabled_overlay)
                .field("Focus Ring", theme::focus_ring, theme::set_focus_ring);

        return root;
    }
}