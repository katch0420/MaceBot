package net.katch0420.macebot.Commands;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.katch0420.macebot.ai.Controller;
import net.katch0420.macebot.player.Kits;
import net.katch0420.macebot.playerbot.PlayerBot;
import net.katch0420.macebot.playerbot.PlayerBotSettings;
import net.katch0420.macebot.utils.Colors;
import net.katch0420.macebot.utils.Messenger;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Formatting;

import java.util.Objects;
import java.util.function.Supplier;

public class BotCommands {

    private static ArgumentBuilder<ServerCommandSource, ?> toggle(String name, Supplier<Boolean> toggleAction, String label) {
        return CommandManager.literal(name)
                .executes(context -> {
                    boolean bl = toggleAction.get();
                    Messenger.add(label + ": ", Colors.BaseColor);
                    Messenger.add(bl ? "enabled" : "disabled", bl ? Colors.TrueColor : Colors.FalseColor);
                    Messenger.send(context.getSource().getPlayer(), true, true);
                    return 1;
                });
    }

    private static int giveKit(CommandContext<ServerCommandSource> context, Kits.Kit kit, boolean unbreakable, Formatting color) {
        Kits.giveKit(context.getSource(), kit, unbreakable, "MaceBot");

        Messenger.add("Gave ", Colors.BaseColor);
        if (unbreakable) Messenger.add("unbreakable ", Colors.BaseColor);
        Messenger.add(kit.displayName(), color);
        Messenger.add(" to ", Colors.BaseColor);
        Messenger.add("MaceBot", Colors.BaseColor);

        Messenger.send(context.getSource().getPlayer(), true, true);
        return 1;
    }

    public static void Register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, registrationEnvironment) ->
                dispatcher.register(
                        CommandManager.literal("macebot")
                                .then(
                                        CommandManager.literal("bot")
                                                .then(
                                                        CommandManager.literal("spawn")
                                                                .executes(context -> {
                                                                    PlayerBot.createBot(context.getSource().getServer(), Objects.requireNonNull(context.getSource().getPlayer()).getServerWorld(), context.getSource().getPlayer().getBlockPos(), context.getSource());
                                                                    Messenger.add("Spawning macebot", Colors.BaseColor);
                                                                    Messenger.send(context.getSource().getPlayer(), true, true);
                                                                    return 1;
                                                                })
                                                )
                                                .then(
                                                        CommandManager.literal("pause")
                                                                .executes(context -> {
                                                                    PlayerBot.controller.pauseTheBot();
                                                                    Messenger.add("Paused the bot", Colors.BaseColor);
                                                                    Messenger.send(context.getSource().getPlayer(), true, true);
                                                                    return 1;
                                                                })
                                                )
                                                .then(
                                                        CommandManager.literal("play")
                                                                .executes(context -> {
                                                                    Controller.difficulty = Controller.Difficulty.EASY;
                                                                    Messenger.add("Resumed the bot", Colors.BaseColor);
                                                                    Messenger.send(context.getSource().getPlayer(), true, true);
                                                                    return 1;
                                                                })
                                                )
                                                .then(
                                                        CommandManager.literal("settings")
                                                                .then(toggle("auto-refill", PlayerBotSettings::toggleAutoRefill, "Auto Refill"))
                                                                .then(toggle("elytra", PlayerBotSettings::toggleElytra, "Elytra Ability"))
                                                                .then(toggle("attack", PlayerBotSettings::toggleAttack, "Attack Ability"))
                                                                .then(toggle("ordinary-mace", PlayerBotSettings::toggleMace, "Ordinary Mace Attack"))
                                                                .then(toggle("crits", PlayerBotSettings::toggleCrits, "Crit Hits"))
                                                )
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
                                )
                )
        );
    }
}
