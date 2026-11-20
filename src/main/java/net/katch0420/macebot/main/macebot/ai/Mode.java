package net.katch0420.macebot.main.macebot.ai;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.serialization.Codec;
import net.katch0420.macebot.main.utils.EnumArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

public enum Mode implements StringRepresentable {
    NPC(Component.literal("Npc").withStyle(ChatFormatting.GREEN)),
    FIGHT(Component.literal("Fight").withStyle(ChatFormatting.GOLD)),
    PRACTICE(Component.literal("Practice").withStyle(ChatFormatting.YELLOW));

    public final Component displayText;

    Mode(Component displayText) {
        this.displayText = displayText;
    }


    static final Codec<Mode> CODEC = StringRepresentable.fromValues(Mode::values);

    @Override
    public @NotNull String getSerializedName() {
        return this.displayText.getString().toLowerCase();
    }

    public static class ModeArgumentType extends EnumArgumentType<Mode> {

        public ModeArgumentType() {
            super(CODEC, Mode::values);
        }

        private static final DynamicCommandExceptionType INVALID_MODE =
                new DynamicCommandExceptionType(id -> Component.literal("Unknown mode: " + id));

        public static Mode getMode(@UnknownNullability CommandContext<CommandSourceStack> context, String id) {
            return context.getArgument(id, Mode.class);
        }

        @Override
        public Mode parse(StringReader reader) throws CommandSyntaxException {
            String modeString = reader.readUnquotedString();
            for (Mode mode : Mode.values()) {
                if (mode.getSerializedName().equalsIgnoreCase(modeString)) {
                    return mode;
                }
            }
            throw INVALID_MODE.create(modeString);
        }
    }
}