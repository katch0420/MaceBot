package net.katch0420.macebot.client.gui;

import net.minecraft.util.Identifier;

/**
 * Central registry of GUI texture identifiers.
 *
 * NOTE: this file reconstructs the fields referenced elsewhere in the GUI
 * (ControllerBody, MainFrame) plus the new quick-setting icons. If your
 * actual project already has additional fields not shown here, merge them
 * in rather than overwriting the file outright.
 */
public class MaceBotTextures {
    private static Identifier id(String path) {
        return Identifier.of("macebot", "textures/gui/" + path);
    }

    // ---- Sidebar navigation icons ----
    public static final Identifier SIDE_PANEL_ICONS_PANEL = id("side_panel_controller.png");
    public static final Identifier SIDE_PANEL_ICONS_KITS = id("side_panel_kits.png");
    public static final Identifier SIDE_PANEL_ICONS_SETTINGS = id("side_panel_settings.png");
    public static final Identifier SIDE_PANEL_ICONS_INFO = id("side_panel_info.png");
    public static final Identifier SIDE_PANEL_ICONS_EXIT = id("side_panel_exit.png");

    // ---- Quick-setting icons (Controller body) ----
    // Generated flat-style 32x32 placeholder icons - see /textures/quick_*.png
    public static final Identifier QUICK_KITS = id("quick_kits.png");
    public static final Identifier QUICK_BUFF = id("quick_buff.png");
    public static final Identifier QUICK_BUFF_DISABLED = id("quick_buff.png");
    public static final Identifier QUICK_REFILL = id("quick_refill.png");
    public static final Identifier QUICK_REFILL_DISABLED = id("quick_refill.png");
    public static final Identifier QUICK_TELEPORT = id("quick_teleport.png");
    public static final Identifier QUICK_SETTINGS = id("quick_settings.png");
}
