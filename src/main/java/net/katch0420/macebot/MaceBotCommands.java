package net.katch0420.macebot;

import com.mojang.brigadier.arguments.BoolArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.katch0420.macebot.player.Kits;
import net.katch0420.macebot.player.PlayerSettings;
import net.katch0420.macebot.playerbot.PlayerBot;
import net.katch0420.macebot.playerbot.PlayerBotSettings;
import net.katch0420.macebot.utils.Colors;
import net.katch0420.macebot.utils.Messenger;
import net.minecraft.server.command.CommandManager;
import net.minecraft.util.Formatting;

import java.util.Objects;

public class MaceBotCommands {
    public static void Register(){
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, registrationEnvironment) -> dispatcher.register(
                CommandManager.literal("macebot")
                        .then(
                                CommandManager.literal("bot")
                                        .then(
                                                CommandManager.literal("spawn")
                                                        .executes(
                                                                context -> {
                                                                    PlayerBot.createBot(context.getSource().getServer(), Objects.requireNonNull(context.getSource().getPlayer()).getServerWorld(), context.getSource().getPlayer().getBlockPos(), context.getSource());
                                                                    Messenger.add("Spawned macebot",Colors.BaseColor);
                                                                    Messenger.send(context.getSource().getPlayer(), true, true);
                                                                    return 1;
                                                                }
                                                        )
                                        )
                                        .then(
                                                CommandManager.literal("pause")
                                                        .executes(
                                                                context -> {
                                                                    PlayerBot.BotAI.idle = true;
                                                                    Messenger.add("Paused the bot",Colors.BaseColor);
                                                                    Messenger.send(context.getSource().getPlayer(), true,true);
                                                                    return 1;
                                                                }
                                                        )
                                        )
                                        .then(
                                                CommandManager.literal("play")
                                                        .executes(
                                                                context -> {
                                                                    PlayerBot.BotAI.idle = false;
                                                                    Messenger.add("Resumed the bot",Colors.BaseColor);
                                                                    Messenger.send(context.getSource().getPlayer(), true,true);
                                                                    return 1;
                                                                }
                                                        )
                                        )
                                        .then(
                                                CommandManager.literal("settings")
                                                        .then(
                                                                CommandManager.literal("auto-refill")
                                                                        .executes(
                                                                                context -> {
                                                                                    boolean bl = PlayerBotSettings.toggleAutoRefill();
                                                                                    Messenger.add("Auto Refill: ",Colors.BaseColor);
                                                                                    Messenger.add(bl?"enabled":"disabled",bl?Colors.TrueColor:Colors.FalseColor);
                                                                                    Messenger.send(context.getSource().getPlayer(), true,true);
                                                                                    return 1;
                                                                                }
                                                                        )
                                                        )
                                                        .then(
                                                                CommandManager.literal("elytra")
                                                                        .executes(
                                                                                context -> {
                                                                                    boolean bl = PlayerBotSettings.toggleElytra();
                                                                                    Messenger.add("Elytra Usage:  ",Colors.BaseColor);
                                                                                    Messenger.add(bl?"enabled":"disabled",bl?Colors.TrueColor:Colors.FalseColor);
                                                                                    Messenger.send(context.getSource().getPlayer(), true,true);
                                                                                    return 1;
                                                                                }
                                                                        )
                                                        )
                                        )
                                        .then(
                                                CommandManager.literal("mace-kit")
                                                        .then(
                                                                CommandManager.literal("netherite")
                                                                        .executes(
                                                                                context -> {
                                                                                    Kits.giveKit(context.getSource(), Kits.Kit.MACE_NETHERITE,true,"MaceBot");
                                                                                    Messenger.add("Gave ",Colors.BaseColor);
                                                                                    Messenger.add("unbreakable ",Colors.BaseColor);
                                                                                    Messenger.add("Netherite Kit ", Formatting.DARK_PURPLE);
                                                                                    Messenger.add("to ",Colors.BaseColor);
                                                                                    Messenger.add("MaceBot",Colors.BaseColor);
                                                                                    Messenger.send(context.getSource().getPlayer(),true,true);
                                                                                    return 1;
                                                                                }
                                                                        )
                                                                        .then(
                                                                                CommandManager.argument("unbreakable", BoolArgumentType.bool())
                                                                                        .executes(
                                                                                                context -> {
                                                                                                    boolean bl = BoolArgumentType.getBool(context, "unbreakable");
                                                                                                    Kits.giveKit(context.getSource(),Kits.Kit.MACE_NETHERITE,bl ,"MaceBot");
                                                                                                    Messenger.add("Gave ",Colors.BaseColor);
                                                                                                    if(bl) Messenger.add("unbreakable ",Colors.BaseColor);
                                                                                                    Messenger.add("Netherite Kit ", Formatting.DARK_PURPLE);
                                                                                                    Messenger.add("to ",Colors.BaseColor);
                                                                                                    Messenger.add("MaceBot",Colors.BaseColor);
                                                                                                    Messenger.send(context.getSource().getPlayer(),true,true);
                                                                                                    return 1;
                                                                                                }
                                                                                        )
                                                                        )
                                                        )
                                                        .then(
                                                                CommandManager.literal("diamond")
                                                                        .executes(
                                                                                context -> {
                                                                                    Kits.giveKit(context.getSource(), Kits.Kit.MACE_DIAMOND,true,"MaceBot");
                                                                                    Messenger.add("Gave ",Colors.BaseColor);
                                                                                    Messenger.add("unbreakable ",Colors.BaseColor);
                                                                                    Messenger.add("Diamond Kit ", Formatting.AQUA);
                                                                                    Messenger.add("to ",Colors.BaseColor);
                                                                                    Messenger.add("MaceBot",Colors.BaseColor);
                                                                                    Messenger.send(context.getSource().getPlayer(),true,true);
                                                                                    return 1;
                                                                                }
                                                                        )
                                                                        .then(
                                                                                CommandManager.argument("unbreakable",BoolArgumentType.bool())
                                                                                        .executes(
                                                                                                context -> {
                                                                                                    boolean bl = BoolArgumentType.getBool(context, "unbreakable");
                                                                                                    Kits.giveKit(context.getSource(),Kits.Kit.MACE_DIAMOND,bl ,"MaceBot");
                                                                                                    Messenger.add("Gave ",Colors.BaseColor);
                                                                                                    if(bl) Messenger.add("unbreakable ",Colors.BaseColor);
                                                                                                    Messenger.add("Diamond Kit ", Formatting.AQUA);
                                                                                                    Messenger.add("to ",Colors.BaseColor);
                                                                                                    Messenger.add("MaceBot",Colors.BaseColor);
                                                                                                    Messenger.send(context.getSource().getPlayer(),true,true);
                                                                                                    return 1;
                                                                                                }
                                                                                        )
                                                                        )
                                                        )
                                        )
                        )
                        .then(
                                CommandManager.literal("player")
                                        .then(
                                                CommandManager.literal("mace-kit")
                                                        .then(
                                                                CommandManager.literal("netherite")
                                                                        .executes(
                                                                                context -> {
                                                                                    String s = context.getSource().getName();
                                                                                    Kits.giveKit(context.getSource(), Kits.Kit.MACE_NETHERITE,true,s);
                                                                                    Messenger.add("Gave ",Colors.BaseColor);
                                                                                    Messenger.add("unbreakable ",Colors.BaseColor);
                                                                                    Messenger.add("Netherite Kit ", Formatting.DARK_PURPLE);
                                                                                    Messenger.add("to ",Colors.BaseColor);
                                                                                    Messenger.add(s,Colors.BaseColor);
                                                                                    Messenger.send(context.getSource().getPlayer(),true,true);
                                                                                    return 1;
                                                                                }
                                                                        )
                                                                        .then(
                                                                                CommandManager.argument("unbreakable", BoolArgumentType.bool())
                                                                                        .executes(
                                                                                                context -> {
                                                                                                    boolean bl = BoolArgumentType.getBool(context, "unbreakable");
                                                                                                    String s = context.getSource().getName();
                                                                                                    Kits.giveKit(context.getSource(), Kits.Kit.MACE_NETHERITE,bl,s);
                                                                                                    Messenger.add("Gave ",Colors.BaseColor);
                                                                                                    if(bl) Messenger.add("unbreakable ",Colors.BaseColor);
                                                                                                    Messenger.add("Netherite Kit ", Formatting.DARK_PURPLE);
                                                                                                    Messenger.add("to ",Colors.BaseColor);
                                                                                                    Messenger.add(s,Colors.BaseColor);
                                                                                                    Messenger.send(context.getSource().getPlayer(),true,true);
                                                                                                    return 1;
                                                                                                }
                                                                                        )
                                                                        )
                                                        )
                                                        .then(
                                                                CommandManager.literal("diamond")
                                                                        .executes(
                                                                                context -> {
                                                                                    String s = context.getSource().getName();
                                                                                    Kits.giveKit(context.getSource(), Kits.Kit.MACE_DIAMOND,true,s);
                                                                                    Messenger.add("Gave ",Colors.BaseColor);
                                                                                    Messenger.add("unbreakable ",Colors.BaseColor);
                                                                                    Messenger.add("Diamond Kit ", Formatting.AQUA);
                                                                                    Messenger.add("to ",Colors.BaseColor);
                                                                                    Messenger.add(s,Colors.BaseColor);
                                                                                    Messenger.send(context.getSource().getPlayer(),true,true);
                                                                                    return 1;
                                                                                }
                                                                        )
                                                                        .then(
                                                                                CommandManager.argument("unbreakable",BoolArgumentType.bool())
                                                                                        .executes(
                                                                                                context -> {
                                                                                                    boolean bl = BoolArgumentType.getBool(context, "unbreakable");
                                                                                                    String s = context.getSource().getName();
                                                                                                    Kits.giveKit(context.getSource(), Kits.Kit.MACE_DIAMOND,bl,s);
                                                                                                    Messenger.add("Gave ",Colors.BaseColor);
                                                                                                    if(bl) Messenger.add("unbreakable ",Colors.BaseColor);
                                                                                                    Messenger.add("Diamond Kit ", Formatting.AQUA);
                                                                                                    Messenger.add("to ",Colors.BaseColor);
                                                                                                    Messenger.add(s,Colors.BaseColor);
                                                                                                    Messenger.send(context.getSource().getPlayer(),true,true);
                                                                                                    return 1;
                                                                                                }
                                                                                        )
                                                                        )
                                                        )
                                        )
                                        .then(
                                                CommandManager.literal("settings")
                                                        .then(
                                                                CommandManager.literal("auto-refill")
                                                                        .executes(
                                                                                context -> {
                                                                                    boolean bl = PlayerSettings.toggleAutoRefill();
                                                                                    Messenger.add("Auto Refill: ",Colors.ComponentColor);
                                                                                    Messenger.add(bl?"enabled":"disabled",bl?Colors.TrueColor:Colors.FalseColor);
                                                                                    Messenger.send(context.getSource().getPlayer(), true,true);
                                                                                    return 1;
                                                                                }
                                                                        )
                                                        )
                                        )
                        )
        )
        );
    }
}
