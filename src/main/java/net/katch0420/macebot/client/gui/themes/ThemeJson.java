package net.katch0420.macebot.client.gui.themes;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

/** Gson configuration that reads both map-based and legacy field-based themes. */
public final class ThemeJson {
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(Theme.class, new ThemeAdapter())
            .create();

    private ThemeJson() {}

    public static Gson gson() {
        return GSON;
    }

    private static final class ThemeAdapter implements JsonSerializer<Theme>, JsonDeserializer<Theme> {
        @Override
        public JsonElement serialize(Theme theme, Type type, JsonSerializationContext context) {
            JsonObject object = new JsonObject();
            object.addProperty("builtIn", theme.isBuiltIn());
            object.add("colors", context.serialize(theme.getColors()));
            return object;
        }

        @Override
        public Theme deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
            JsonObject object = element.getAsJsonObject();
            boolean builtIn = object.has("builtIn") && object.get("builtIn").getAsBoolean();
            JsonObject mappedColors = object.has("colors") && object.get("colors").isJsonObject()
                    ? object.getAsJsonObject("colors") : null;
            int[] values = new int[ThemeSettings.all().size()];
            for (int i = 0; i < values.length; i++) {
                ThemeSettings.ColorSetting setting = ThemeSettings.all().get(i);
                String id = setting.id();
                JsonElement color = mappedColors == null ? object.get(id) : mappedColors.get(id);
                values[i] = color == null || color.isJsonNull() ? setting.defaultValue() : color.getAsInt();
            }
            return new Theme(builtIn, values);
        }
    }
}
