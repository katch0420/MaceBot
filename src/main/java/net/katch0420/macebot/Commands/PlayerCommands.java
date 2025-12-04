package net.katch0420.macebot.Commands;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.katch0420.macebot.player.Kits;
import net.katch0420.macebot.player.PlayerSettings;
import net.katch0420.macebot.utils.Colors;
import net.katch0420.macebot.utils.Messenger;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Formatting;

import java.util.function.Supplier;

public class PlayerCommands {

    private static ArgumentBuilder<ServerCommandSource, ?> toggle(String name, Supplier<Boolean> toggleAction, String label) {
        return CommandManager.literal(name)
                .executes(context -> {
                    boolean bl = toggleAction.get();
                    Messenger.add(label + ": ", Colors.ComponentColor);
                    Messenger.add(bl ? "enabled" : "disabled", bl ? Colors.TrueColor : Colors.FalseColor);
                    Messenger.send(context.getSource().getPlayer(), true, true);
                    return 1;
                });
    }

    private static int giveKit(CommandContext<ServerCommandSource> context, Kits.Kit kit, boolean unbreakable, Formatting color) {
        String s = context.getSource().getName();
        Kits.giveKit(context.getSource(), kit, unbreakable, s);
        Messenger.add("Gave ", Colors.BaseColor);
        if (unbreakable) Messenger.add("unbreakable ", Colors.BaseColor);
        Messenger.add(kit.displayName(), color);
        Messenger.add(" to ", Colors.BaseColor);
        Messenger.add(s, Colors.BaseColor);
        Messenger.send(context.getSource().getPlayer(), true, true);
        return 1;
    }

    public static void Register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, registrationEnvironment) ->
                dispatcher.register(
                        CommandManager.literal("macebot")
                                .then(
                                        CommandManager.literal("player")
                                                .then(
                                                        CommandManager.literal("mace-kit")
                                                                .then(
                                                                        CommandManager.literal("netherite")
                                                                                .executes(ctx -> giveKit(ctx, Kits.Kit.MACE_NETHERITE, true, Formatting.DARK_PURPLE))
                                                                                .then(
                                                                                        CommandManager.argument("unbreakable", BoolArgumentType.bool())
                                                                                                .executes(ctx -> giveKit(ctx, Kits.Kit.MACE_NETHERITE, BoolArgumentType.getBool(ctx, "unbreakable"), Formatting.DARK_PURPLE))
                                                                                )
                                                                )
                                                                .then(
                                                                        CommandManager.literal("diamond")
                                                                                .executes(ctx -> giveKit(ctx, Kits.Kit.MACE_DIAMOND, true, Formatting.AQUA))
                                                                                .then(
                                                                                        CommandManager.argument("unbreakable", BoolArgumentType.bool())
                                                                                                .executes(ctx -> giveKit(ctx, Kits.Kit.MACE_DIAMOND, BoolArgumentType.getBool(ctx, "unbreakable"), Formatting.AQUA))
                                                                                )
                                                                )
                                                )
                                                .then(
                                                        CommandManager.literal("settings")
                                                                .then(toggle("auto-refill", PlayerSettings::toggleAutoRefill, "Auto Refill"))
                                                )
                                )
                )
        );
    }
}
