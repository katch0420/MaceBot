package net.katch0420.macebot.client.gui.themes;

public class Theme {
    public Theme(boolean builtIn,
                 int screen_foreground, int screen_background,
                 int header_foreground, int header_background,
                 int footer_foreground, int footer_background,
                 int sidebar_foreground, int sidebar_background,
                 int panel_separator,
                 int body_background, int body_foreground, int body_border, int display_background,
                 int body_key, int body_value, int body_label, int body_category_label,
                 int body_button_background, int body_button_foreground, int body_button_border,
                 int accent, int accent_hover,
                 int success, int warning, int danger,
                 int disabled_overlay, int focus_ring) {
        this.builtIn = builtIn;
        this.screen_foreground = screen_foreground;
        this.screen_background = screen_background;
        this.header_foreground = header_foreground;
        this.header_background = header_background;
        this.footer_foreground = footer_foreground;
        this.footer_background = footer_background;
        this.sidebar_foreground = sidebar_foreground;
        this.sidebar_background = sidebar_background;
        this.panel_separator = panel_separator;
        this.body_background = body_background;
        this.body_foreground = body_foreground;
        this.body_border = body_border;
        this.display_background = display_background;
        this.body_key = body_key;
        this.body_value = body_value;
        this.body_label = body_label;
        this.body_category_label = body_category_label;
        this.body_button_background = body_button_background;
        this.body_button_foreground = body_button_foreground;
        this.body_button_border = body_button_border;
        this.accent = accent;
        this.accent_hover = accent_hover;
        this.success = success;
        this.warning = warning;
        this.danger = danger;
        this.disabled_overlay = disabled_overlay;
        this.focus_ring = focus_ring;
    }

    boolean builtIn = true;

    // ── Screen chrome (frame-level, structurally distinct from bodies) ────────
    int screen_foreground;
    int screen_background;
    int header_foreground;
    int header_background;
    int footer_foreground;
    int footer_background;
    int sidebar_foreground;
    int sidebar_background;
    int panel_separator;

    // ── Body (shared by EVERY body panel - Controller's panels, Settings
    //    columns, ThemeEditor, any future body. One palette, used everywhere,
    //    so they're always visually consistent without re-editing N times) ──
    int body_background;
    int body_foreground;
    int body_border;
    /** Special-purpose: the embedded 3D entity preview box, visually distinct from a normal panel. */
    int display_background;
    int body_key;
    int body_value;
    int body_label;
    int body_category_label;
    int body_button_background;
    int body_button_foreground;
    int body_button_border;

    // ── Semantic accents (cross-cutting, not tied to any one panel) ───────────
    int accent;
    int accent_hover;
    int success;
    int warning;
    int danger;
    int disabled_overlay;
    int focus_ring;

    public boolean isBuiltIn() { return builtIn; }
    public void setBuiltIn(boolean v) { this.builtIn = v; }

    public int screen_foreground() { return screen_foreground; }
    public void set_screen_foreground(int v) { this.screen_foreground = v; }
    public int screen_background() { return screen_background; }
    public void set_screen_background(int v) { this.screen_background = v; }

    public int header_foreground() { return header_foreground; }
    public void set_header_foreground(int v) { this.header_foreground = v; }
    public int header_background() { return header_background; }
    public void set_header_background(int v) { this.header_background = v; }

    public int footer_foreground() { return footer_foreground; }
    public void set_footer_foreground(int v) { this.footer_foreground = v; }
    public int footer_background() { return footer_background; }
    public void set_footer_background(int v) { this.footer_background = v; }

    public int sidebar_foreground() { return sidebar_foreground; }
    public void set_sidebar_foreground(int v) { this.sidebar_foreground = v; }
    public int sidebar_background() { return sidebar_background; }
    public void set_sidebar_background(int v) { this.sidebar_background = v; }

    public int panel_separator() { return panel_separator; }
    public void set_panel_separator(int v) { this.panel_separator = v; }

    public int body_background() { return body_background; }
    public void set_body_background(int v) { this.body_background = v; }
    public int body_foreground() { return body_foreground; }
    public void set_body_foreground(int v) { this.body_foreground = v; }
    public int body_border() { return body_border; }
    public void set_body_border(int v) { this.body_border = v; }
    public int display_background() { return display_background; }
    public void set_display_background(int v) { this.display_background = v; }
    public int body_key() { return body_key; }
    public void set_body_key(int v) { this.body_key = v; }
    public int body_value() { return body_value; }
    public void set_body_value(int v) { this.body_value = v; }
    public int body_label() { return body_label; }
    public void set_body_label(int v) { this.body_label = v; }
    public int body_category_label() { return body_category_label; }
    public void set_body_category_label(int v) { this.body_category_label = v; }
    public int body_button_background() { return body_button_background; }
    public void set_body_button_background(int v) { this.body_button_background = v; }
    public int body_button_foreground() { return body_button_foreground; }
    public void set_body_button_foreground(int v) { this.body_button_foreground = v; }
    public int body_button_border() { return body_button_border; }
    public void set_body_button_border(int v) { this.body_button_border = v; }

    public int accent() { return accent; }
    public void set_accent(int v) { this.accent = v; }
    public int accent_hover() { return accent_hover; }
    public void set_accent_hover(int v) { this.accent_hover = v; }
    public int success() { return success; }
    public void set_success(int v) { this.success = v; }
    public int warning() { return warning; }
    public void set_warning(int v) { this.warning = v; }
    public int danger() { return danger; }
    public void set_danger(int v) { this.danger = v; }
    public int disabled_overlay() { return disabled_overlay; }
    public void set_disabled_overlay(int v) { this.disabled_overlay = v; }
    public int focus_ring() { return focus_ring; }
    public void set_focus_ring(int v) { this.focus_ring = v; }
}