package net.katch0420.macebot.main.utils;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;

public class LegacyText {
    public static MutableText parse(String input) {
        MutableText root = Text.empty();
        Style current = Style.EMPTY;

        StringBuilder buffer = new StringBuilder();

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (c == '&' && i + 1 < input.length()) {
                // flush buffer before applying new style
                if (!buffer.isEmpty()) {
                    root = root.append(Text.literal(buffer.toString()).setStyle(current));
                    buffer.setLength(0);
                }

                char code = input.charAt(++i);
                Formatting fmt = Formatting.byCode(code);
                if (fmt != null) {
                    if (fmt.isColor()) {
                        current = current.withColor(fmt);
                    } else {
                        // bold, italic, reset, etc.
                        switch (fmt) {
                            case BOLD -> current = current.withBold(true);
                            case ITALIC -> current = current.withItalic(true);
                            case UNDERLINE -> current = current.withUnderline(true);
                            case STRIKETHROUGH -> current = current.withStrikethrough(true);
                            case OBFUSCATED -> current = current.withObfuscated(true);
                            case RESET -> current = Style.EMPTY;
                        }
                    }
                }
                continue;
            }

            buffer.append(c);
        }

        // flush remaining text
        if (!buffer.isEmpty()) {
            root = root.append(Text.literal(buffer.toString()).setStyle(current));
        }

        return root;
    }

    public static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }

    public static String getEnumValueAsString(String s) {
        return capitalize(s).replace("_"," ");
    }
}
