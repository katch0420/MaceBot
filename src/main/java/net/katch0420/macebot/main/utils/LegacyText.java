package net.katch0420.macebot.main.utils;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public class LegacyText {
    public static MutableComponent parse(String input) {
        MutableComponent root = Component.empty();
        Style current = Style.EMPTY;

        StringBuilder buffer = new StringBuilder();

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (c == '&' && i + 1 < input.length()) {
                // flush buffer before applying new style
                if (!buffer.isEmpty()) {
                    root = root.append(Component.literal(buffer.toString()).setStyle(current));
                    buffer.setLength(0);
                }

                char code = input.charAt(++i);
                ChatFormatting fmt = ChatFormatting.getByCode(code);
                if (fmt != null) {
                    //? if <26.2 {
                    if(fmt.isColor()){
                        current = current.withColor(fmt);
                        continue;
                    }
                    //?}
                    switch (fmt) {
                        case BOLD -> current = current.withBold(true);
                        case ITALIC -> current = current.withItalic(true);
                        case UNDERLINE -> current = current.withUnderlined(true);
                        case STRIKETHROUGH -> current = current.withStrikethrough(true);
                        case OBFUSCATED -> current = current.withObfuscated(true);
                        case RESET -> current = Style.EMPTY;
                        //? if >=26.2
                        //default -> current = current.withColor(fmt);
                    }
                }
                continue;
            }

            buffer.append(c);
        }

        // flush remaining text
        if (!buffer.isEmpty()) {
            root = root.append(Component.literal(buffer.toString()).setStyle(current));
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
