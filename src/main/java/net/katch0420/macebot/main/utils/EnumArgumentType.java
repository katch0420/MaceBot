package net.katch0420.macebot.main.utils;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Generic Brigadier argument type for enums that implement StringRepresentable
 * (i.e. have a getSerializedName()). Matches values case-insensitively against
 * their serialized name and offers tab-completion for all values.
 * <p>
 * Usage: extend this with a no-arg constructor that calls
 * super(MyEnum.CODEC, MyEnum::values) - see Difficulty.DifficultyArgumentType
 * and Controller.PracticeMode.PracticeModeArgumentType for examples.
 */
public abstract class EnumArgumentType<T extends Enum<T> & StringRepresentable> implements ArgumentType<T> {

    private static final DynamicCommandExceptionType INVALID_VALUE =
            new DynamicCommandExceptionType(id -> Component.literal("Unknown value: " + id));

    protected final Codec<T> codec;
    protected final Supplier<T[]> valuesSupplier;

    protected EnumArgumentType(Codec<T> codec, Supplier<T[]> valuesSupplier) {
        this.codec = codec;
        this.valuesSupplier = valuesSupplier;
    }

    @Override
    public T parse(StringReader reader) throws CommandSyntaxException {
        String input = reader.readUnquotedString();
        for (T value : valuesSupplier.get()) {
            if (value.getSerializedName().equalsIgnoreCase(input)) {
                return value;
            }
        }
        throw INVALID_VALUE.create(input);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);
        for (T value : valuesSupplier.get()) {
            String name = value.getSerializedName();
            if (name.toLowerCase(Locale.ROOT).startsWith(remaining)) {
                builder.suggest(name);
            }
        }
        return builder.buildFuture();
    }
}