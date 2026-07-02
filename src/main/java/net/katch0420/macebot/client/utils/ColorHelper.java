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

}
