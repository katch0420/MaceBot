package net.katch0420.macebot.client.gui.themes;

/**
 * A named, switchable theme: either a code-defined built-in or a user-saved custom theme.
 */
public record ThemeProfile(String name, boolean builtin, Theme theme) {
}