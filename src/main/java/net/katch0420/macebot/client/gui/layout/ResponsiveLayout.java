package net.katch0420.macebot.client.gui.layout;

/**
 * Small helper that turns the *actual* available pixel area (which already
 * reflects Minecraft's current GUI scale + the player's real resolution)
 * into a continuous scale factor, instead of bucketing into a few fixed
 * "gui scale 2 / 3 / 4" presets like the old code did.
 *
 * This means margins, button sizes and panel proportions shrink/grow
 * smoothly as the player resizes their window or changes GUI scale,
 * rather than jumping between a few hand-tuned layouts.
 */
public final class ResponsiveLayout {

    /** Width (in already-scaled GUI pixels) the layout was designed at. */
    private static final int REFERENCE_WIDTH = 720;

    /** Never shrink below this factor - text/buttons stay legible. */
    private static final float MIN_SCALE = 0.70f;

    /** Never grow past this factor - avoids cartoonishly large widgets on huge screens. */
    private static final float MAX_SCALE = 1.65f;

    private ResponsiveLayout() {}

    /**
     * @param availableWidthPx the real available width in GUI pixels
     * @return a smooth scale factor clamped to [MIN_SCALE, MAX_SCALE]
     */
    public static float scaleFactor(int availableWidthPx) {
        float raw = availableWidthPx / (float) REFERENCE_WIDTH;
        return clamp(raw, MIN_SCALE, MAX_SCALE);
    }

    public static int scaled(int base, float factor) {
        return Math.round(base * factor);
    }

    public static int scaledMin(int base, float factor, int min) {
        return Math.max(min, scaled(base, factor));
    }

    private static float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
    }

    /** Below this available width, multi-column panels should stack vertically instead. */
    public static boolean shouldStackVertically(int availableWidth, int minSideBySideWidth) {
        return availableWidth < minSideBySideWidth;
    }
}
