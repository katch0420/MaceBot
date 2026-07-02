package net.katch0420.macebot.client.gui.themes;

import com.google.gson.Gson;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Encodes a {@link ThemeProfile} into a compact text string players can
 * paste to each other (Discord, chat, a text file, etc) and decodes it back.
 * <p>
 * Format: {@code "MBT1:" + base64(gzip(json({name, theme})))}.
 * The "MBT1" prefix is a format tag so future versions can change the
 * payload shape without silently mis-parsing old codes.
 */
public final class ThemeCodec {

    private static final String PREFIX = "MBT1:";
    private static final Gson GSON = new Gson();

    private ThemeCodec() {}

    public static String encode(String name, Theme theme) {
        try {
            StoredTheme stored = new StoredTheme(name, ThemeManager.copy(theme));
            String json = GSON.toJson(stored);

            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            try (GZIPOutputStream gzip = new GZIPOutputStream(byteStream)) {
                gzip.write(json.getBytes(StandardCharsets.UTF_8));
            }
            return PREFIX + Base64.getEncoder().encodeToString(byteStream.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /** Returns empty if the code is malformed, foreign, or corrupted - fails closed rather than guessing. */
    public static Optional<ThemeProfile> decode(String code) {
        if (code == null) return Optional.empty();
        String trimmed = code.trim();
        if (!trimmed.startsWith(PREFIX)) return Optional.empty();

        try {
            byte[] compressed = Base64.getDecoder().decode(trimmed.substring(PREFIX.length()));
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try (GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(compressed))) {
                gzip.transferTo(out);
            }
            String json = out.toString(StandardCharsets.UTF_8);
            StoredTheme stored = GSON.fromJson(json, StoredTheme.class);
            if (stored == null || stored.theme == null) return Optional.empty();
            String name = (stored.name != null && !stored.name.isBlank()) ? stored.name : "Imported Theme";
            return Optional.of(new ThemeProfile(name, false, stored.theme));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private record StoredTheme(String name, Theme theme) {}
}