package net.katch0420.macebot.main.commands;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.katch0420.macebot.main.kits.server.KitGiver;
import net.katch0420.macebot.main.kits.server.KitRegistry;
import net.katch0420.macebot.main.macebot.control.Controller;
import net.katch0420.macebot.main.macebot.bot.PlayerBot;
import net.katch0420.macebot.main.settings.main.SettingsKey;
import net.katch0420.macebot.main.settings.server.SettingsSyncHelper;
import net.katch0420.macebot.main.utils.Colors;
import net.katch0420.macebot.main.utils.Messenger;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Objects;

public class BotCommands {

    /**
     * Generic boolean toggle backed by SettingsKey.
     * Flips the server value, broadcasts to all clients, sends chat confirmation.
     */
    private static ArgumentBuilder<ServerCommandSource, ?> toggle(
            String name, SettingsKey key, String label) {
        return CommandManager.literal(name)
                .executes(ctx -> {
                    boolean next = !((Boolean) key.getServerValue());
                    SettingsSyncHelper.applyAndBroadcast(key, next, ctx.getSource().getServer());
                    Messenger.add(label + ": ", Colors.BaseColor);
                    Messenger.add(next ? "enabled" : "disabled",
                            next ? Colors.TrueColor : Colors.FalseColor);
                    Messenger.send(ctx.getSource().getPlayer(), true, true);
                    return 1;
                });
    }

    private static int giveBotKit(CommandContext<ServerCommandSource> ctx,
                                  String kitId, boolean unbreakable) {
        ServerPlayerEntity bot = ctx.getSource().getServer()
                .getPlayerManager().getPlayer("MaceBot");

        if (bot == null) {
            Messenger.add("MaceBot is not online", Colors.FalseColor);
            Messenger.send(ctx.getSource().getPlayer(), true, true);
            return 0;
        }

        KitGiver.giveBuiltInKit(bot, KitRegistry.get(kitId), unbreakable);
        Messenger.add("Gave ", Colors.BaseColor);
        if (unbreakable) Messenger.add("unbreakable ", Colors.BaseColor);
        Messenger.add(kitId.replace("_", " "), Colors.BaseColor);
        Messenger.add(" to MaceBot", Colors.BaseColor);
        Messenger.send(ctx.getSource().getPlayer(), true, true);
        return 1;
    }

    private static ArgumentBuilder<ServerCommandSource, ?> kitLiteral(String name, String kitId) {
        return CommandManager.literal(name)
                .executes(ctx -> giveBotKit(ctx, kitId, true))
                .then(CommandManager.argument("unbreakable", BoolArgumentType.bool())
                        .executes(ctx -> giveBotKit(ctx, kitId,
                                BoolArgumentType.getBool(ctx, "unbreakable"))));
    }

    public static void Register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, env) ->
                dispatcher.register(
                        CommandManager.literal("macebot")
                                .then(CommandManager.literal("bot")

                                        .then(CommandManager.literal("spawn")
                                                .executes(ctx -> {
                                                    PlayerBot.spawnMaceBot(ctx.getSource().getPlayer());
                                                    Messenger.add("Spawning MaceBot", Colors.BaseColor);
                                                    Messenger.send(ctx.getSource().getPlayer(), true, true);
                                                    return 1;
                                                }))

                                        .then(CommandManager.literal("pause")
                                                .executes(ctx -> {
                                                    PlayerBot.controller.pauseTheBot();
                                                    Messenger.add("Paused the bot", Colors.BaseColor);
                                                    Messenger.send(ctx.getSource().getPlayer(), true, true);
                                                    return 1;
                                                }))

                                        .then(CommandManager.literal("play")
                                                .executes(ctx -> {
                                                    SettingsSyncHelper.applyAndBroadcast(
                                                            SettingsKey.MODE,
                                                            Controller.Mode.FIGHT,
                                                            ctx.getSource().getServer()
                                                    );
                                                    Messenger.add("Resumed the bot", Colors.BaseColor);
                                                    Messenger.send(ctx.getSource().getPlayer(), true, true);
                                                    return 1;
                                                }))

                                        .then(CommandManager.literal("settings")
                                                .then(toggle("auto-refill",   SettingsKey.MACEBOT_AUTO_REFILL_ENABLED, "Auto Refill"))
                                                .then(toggle("elytra",        SettingsKey.MACEBOT_CAN_USE_ELYTRA,      "Elytra Ability"))
                                                .then(toggle("attack",        SettingsKey.MACEBOT_CAN_DO_ATTACK,       "Attack Ability"))
                                                .then(toggle("ordinary-mace", SettingsKey.MACEBOT_CAN_DO_MACE_ATTACK,  "Ordinary Mace Attack"))
                                                .then(toggle("crits",         SettingsKey.MACEBOT_CAN_DO_CRIT_HIT,     "Crit Hits"))
                                                .then(toggle("kb-hit",        SettingsKey.MACEBOT_CAN_DO_KB_HIT,       "KnockBack Hits"))
                                                .then(toggle("buffs",         SettingsKey.MACEBOT_BUFFS_ENABLED,       "Buffs"))
                                                .then(toggle("shield",        SettingsKey.MACEBOT_CAN_USE_SHIELD,      "Shield"))
                                                .then(toggle("tracking",      SettingsKey.MACEBOT_CAN_DO_TRACKING,     "Tracking")))

                                        .then(CommandManager.literal("mace-kit")
                                                .then(kitLiteral("diamond",   "diamond_mace"))
                                                .then(kitLiteral("netherite", "netherite_mace")))
                                )
                )
        );
    }
}