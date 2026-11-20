package net.katch0420.macebot.client.utils;

public class ColorHelper {
    public static int darken(int color, float factor) {
        int a = (color >> 24) & 0xFF;
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        r = (int)(r * factor);
        g = (int)(g * factor);
        b = (int)(b * factor);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    // 1. Convert ARGB hex (0xAARRGGBB) into RGBA floats
    public static float[] toRGBAf(int argbColor) {
        float a = ((argbColor >> 24) & 0xFF) / 255.0f;
        float r = ((argbColor >> 16) & 0xFF) / 255.0f;
        float g = ((argbColor >> 8) & 0xFF) / 255.0f;
        float b = (argbColor & 0xFF) / 255.0f;
        return new float[]{r, g, b, a};
    }
    public static int getRed(int argbColor) {
        return (argbColor >> 16) & 0xFF;
    }
    public static int getGreen(int argbColor) {
        return (argbColor >> 8) & 0xFF;
    }
    public static int getBlue(int argbColor) {
        return argbColor & 0xFF;
    }

    public static float getAlpha(int argbColor) {
        return (argbColor >> 24) & 0xFF;
    }

    public static float getRedF(int argbColor) {
        return ((argbColor >> 16) & 0xFF) / 255F;
    }
    public static float getGreenF(int argbColor) {
        return ((argbColor >> 8) & 0xFF) / 255F;
    }
    public static float getBlueF(int argbColor) {
        return (argbColor & 0xFF) / 255F;
    }
    public static float getAlphaF(int argbColor) {
        return ((argbColor >> 24) & 0xFF) / 255.0f;
    }
    public static int fromRGBAf(float r, float g, float b, float a) {
        int ai = (int)(a * 255.0f) & 0xFF;
        int ri = (int)(r * 255.0f) & 0xFF;
        int gi = (int)(g * 255.0f) & 0xFF;
        int bi = (int)(b * 255.0f) & 0xFF;
        return (ai << 24) | (ri << 16) | (gi << 8) | bi;
    }
}
