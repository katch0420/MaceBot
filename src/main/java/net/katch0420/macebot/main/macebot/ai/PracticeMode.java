package net.katch0420.macebot.main.macebot.ai;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.serialization.Codec;
import net.katch0420.macebot.main.utils.EnumArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum PracticeMode implements StringRepresentable {
    STUN_SLAM(Component.literal("Stun slam")), STATIC_AIM(Component.literal("Static aim")), GRAPPLING(Component.literal("Grappling"));
    public final Component displayText;

    PracticeMode(Component displayText) {
        this.displayText = displayText;
    }

    @Override
    public @NotNull String getSerializedName() {
        return this.toString().toLowerCase();
    }

    static final Codec<PracticeMode> CODEC = StringRepresentable.fromValues(PracticeMode::values);

    public static class PracticeModeArgumentType extends EnumArgumentType<PracticeMode> {
        public PracticeModeArgumentType() {
            super(CODEC, PracticeMode::values);
        }

        public static PracticeMode getPracticeMode(CommandContext<CommandSourceStack> context, String id) {
            return context.getArgument(id, PracticeMode.class);
        }
    }
}