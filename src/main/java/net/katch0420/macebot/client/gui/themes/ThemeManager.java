package net.katch0420.macebot.client.gui.themes;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages the full set of themes: built-in (code-defined, read-only) and
 * custom (one JSON file each under config/macebot/themes/), tracks which one
 * is currently active, and persists that selection across restarts.
 * <p>
 * Editing a built-in theme's colors auto-forks it into a new custom theme
 * first (see {@link #ensureEditableActive()}) so the built-ins themselves
 * never get silently overwritten.
 */
public class ThemeManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve("macebot");
    private static final Path THEMES_DIR = CONFIG_DIR.resolve("themes");
    private static final Path ACTIVE_FILE = CONFIG_DIR.resolve("active_theme.json");
    /** Legacy single-file location from before named theme management existed. */
    private static final Path LEGACY_THEME_FILE = CONFIG_DIR.resolve("theme.json");

    public static String activeName = "Professional Dark";
    public static boolean activeIsBuiltin = true;

    // ── Built-ins ────────────────────────────────────────────────────────────

    public static List<ThemeProfile> builtins() {
        return List.of(
                new ThemeProfile("Professional Dark", true, Themes.PROFESSIONAL_DARK),
                new ThemeProfile("Professional Light", true, Themes.PROFESSIONAL_LIGHT),
                new ThemeProfile("Classic", true, Themes.DEFAULT)
        );
    }

    // ── Custom themes ────────────────────────────────────────────────────────

    public static List<ThemeProfile> customThemes() {
        List<ThemeProfile> result = new ArrayList<>();
        try {
            if (Files.isDirectory(THEMES_DIR)) {
                try (var stream = Files.list(THEMES_DIR)) {
                    for (Path path : stream.filter(p -> p.toString().endsWith(".json")).collect(Collectors.toList())) {
                        try (Reader reader = Files.newBufferedReader(path)) {
                            StoredTheme stored = GSON.fromJson(reader, StoredTheme.class);
                            if (stored != null && stored.name != null && stored.theme != null) {
                                result.add(new ThemeProfile(stored.name, false, stored.theme));
                            }
                        } catch (IOException ignored) {
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        result.sort(Comparator.comparing(p -> p.name()));
        return result;
    }

    /** All themes, built-ins first then custom alphabetically - what the management UI lists. */
    public static List<ThemeProfile> allThemes() {
        List<ThemeProfile> all = new ArrayList<>(builtins());
        all.addAll(customThemes());
        return all;
    }

    public static void saveCustom(ThemeProfile profile) {
        if (profile.builtin()) return;
        try {
            Files.createDirectories(THEMES_DIR);
            Path path = THEMES_DIR.resolve(slug(profile.name()) + ".json");
            StoredTheme stored = new StoredTheme(profile.name(), copy(profile.theme()));
            try (Writer writer = Files.newBufferedWriter(path)) {
                GSON.toJson(stored, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void deleteCustom(String name) {
        try {
            Files.deleteIfExists(THEMES_DIR.resolve(slug(name) + ".json"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        // If we just deleted the active theme, fall back to a built-in so the
        // app isn't left pointing at a now-nonexistent theme.
        if (!activeIsBuiltin && activeName.equalsIgnoreCase(name)) {
            setActive(builtins().getFirst());
        }
    }

    /** Returns a name guaranteed not to collide with any existing theme, e.g. "My Theme" -> "My Theme (2)". */
    public static String uniqueName(String baseName) {
        Set<String> taken = allThemes().stream().map(p -> p.name().toLowerCase(Locale.ROOT)).collect(Collectors.toSet());
        if (!taken.contains(baseName.toLowerCase(Locale.ROOT))) return baseName;
        int i = 2;
        while (taken.contains((baseName + " (" + i + ")").toLowerCase(Locale.ROOT))) i++;
        return baseName + " (" + i + ")";
    }

    private static String slug(String name) {
        String slug = name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "_").replaceAll("^_+|_+$", "");
        return slug.isEmpty() ? "theme" : slug;
    }

    // ── Active theme ─────────────────────────────────────────────────────────

    /** Loads the saved active theme's colors into a fresh Theme instance (called once for Themes.CURRENT's initializer). */
    public static Theme load() {
        migrateLegacyFileIfPresent();

        ActiveMarker marker = readActiveMarker();
        if (marker != null) {
            Optional<ThemeProfile> found = (marker.builtin ? builtins() : customThemes()).stream()
                    .filter(p -> p.name().equals(marker.name))
                    .findFirst();
            if (found.isPresent()) {
                activeName = found.get().name();
                activeIsBuiltin = found.get().builtin();
                return copy(found.get().theme());
            }
        }
        activeName = "Professional Dark";
        activeIsBuiltin = true;
        return copy(Themes.PROFESSIONAL_DARK);
    }

    /** Switches the live active theme and persists the selection. */
    public static void setActive(ThemeProfile profile) {
        Themes.CURRENT = copy(profile.theme());
        activeName = profile.name();
        activeIsBuiltin = profile.builtin();
        writeActiveMarker();
    }

    /**
     * If the active theme is a built-in, forks it into a new editable custom
     * theme with identical colors and switches to that BEFORE any UI binds
     * getter/setter references to Themes.CURRENT. Call this first thing when
     * opening any color-editing UI.
     *
     * @return true if a fork just happened (caller should rebuild anything bound to the old Themes.CURRENT)
     */
    public static boolean ensureEditableActive() {
        if (!activeIsBuiltin) return false;
        ThemeProfile forked = new ThemeProfile(uniqueName(activeName + " (Custom)"), false, copy(Themes.CURRENT));
        saveCustom(forked);
        setActive(forked);
        return true;
    }

    /** Persists current Themes.CURRENT colors into whichever custom theme is active. No-op while a built-in is active. */
    public static void saveActive() {
        if (activeIsBuiltin) return;
        saveCustom(new ThemeProfile(activeName, false, Themes.CURRENT));
    }

    /** Legacy entry point - existing call sites (ColorPickerScreen, etc.) keep working unchanged. */
    public static void save(Theme theme) {
        saveActive();
    }

    private static void writeActiveMarker() {
        try {
            Files.createDirectories(CONFIG_DIR);
            try (Writer writer = Files.newBufferedWriter(ACTIVE_FILE)) {
                GSON.toJson(new ActiveMarker(activeName, activeIsBuiltin), writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static ActiveMarker readActiveMarker() {
        try {
            if (Files.exists(ACTIVE_FILE)) {
                try (Reader reader = Files.newBufferedReader(ACTIVE_FILE)) {
                    return GSON.fromJson(reader, ActiveMarker.class);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** One-time migration: imports the old single theme.json as a custom theme if present. */
    private static void migrateLegacyFileIfPresent() {
        try {
            if (Files.exists(LEGACY_THEME_FILE) && !Files.exists(ACTIVE_FILE)) {
                try (Reader reader = Files.newBufferedReader(LEGACY_THEME_FILE)) {
                    Theme legacy = GSON.fromJson(reader, Theme.class);
                    if (legacy != null) {
                        ThemeProfile profile = new ThemeProfile(uniqueName("My Theme"), false, legacy);
                        saveCustom(profile);
                        setActive(profile);
                    }
                }
                Files.deleteIfExists(LEGACY_THEME_FILE);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Creates an independent copy of a theme, so editing it doesn't mutate the source in place. */
    public static Theme copy(Theme source) {
        return new Theme(
                source.isBuiltIn(),
                source.screen_foreground(), source.screen_background(),
                source.header_foreground(), source.header_background(),
                source.footer_foreground(), source.footer_background(),
                source.sidebar_foreground(), source.sidebar_background(),
                source.panel_separator(),
                source.body_background(), source.body_foreground(), source.body_border(), source.display_background(),
                source.body_key(), source.body_value(), source.body_label(), source.body_category_label(),
                source.body_button_background(), source.body_button_foreground(), source.body_button_border(),
                source.accent(), source.accent_hover(),
                source.success(), source.warning(), source.danger(),
                source.disabled_overlay(), source.focus_ring()
        );
    }

    private record StoredTheme(String name, Theme theme) {}
    private record ActiveMarker(String name, boolean builtin) {}
}