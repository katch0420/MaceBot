package net.katch0420.macebot;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.katch0420.macebot.playerbot.PlayerBot;
import net.minecraft.server.command.CommandManager;

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
                                                                    return 1;
                                                                }
                                                        )
                                        )
                                        .then(
                                                CommandManager.literal("pause")
                                                        .executes(
                                                                context -> {
                                                                    PlayerBot.BotAI.idle = true;
                                                                    return 1;
                                                                }
                                                        )
                                        )
                                        .then(
                                                CommandManager.literal("play")
                                                        .executes(
                                                                context -> {
                                                                    PlayerBot.BotAI.idle = false;
                                                                    return 1;
                                                                }
                                                        )
                                        )
                                        .then(
                                                CommandManager.literal("settings")
                                                        .then(
                                                                CommandManager.literal("auto-refill")
                                                                        .then(
                                                                                CommandManager.argument("value", BoolArgumentType.bool())
                                                                                        .executes(
                                                                                                context -> {
                                                                                                    PlayerBot.BotAI.refreshInv = BoolArgumentType.getBool(context, "value");
                                                                                                    return 1;
                                                                                                }
                                                                                        )
                                                                        )
                                                        )
                                                        .then(
                                                                CommandManager.literal("elytra")
                                                                        .then(
                                                                                CommandManager.argument("value", BoolArgumentType.bool())
                                                                                        .executes(
                                                                                                context -> {
                                                                                                    PlayerBot.BotAI.elytra = BoolArgumentType.getBool(context, "value");
                                                                                                    return 1;
                                                                                                }
                                                                                        )
                                                                        )
                                                        )
                                        )
                                        .then(
                                                CommandManager.literal("mace-kit")
                                                        .then(
                                                                CommandManager.argument("netherite", BoolArgumentType.bool())
                                                                        .executes(
                                                                                context -> {
                                                                                    PlayerBot.BotAI.currentMaterialisDia = !BoolArgumentType.getBool(context,"netherite");
                                                                                    PlayerBot.BotAI.refillInventory("full",PlayerBot.BotAI.currentMaterialisDia?"diamond":"netherite", "MaceBot");
                                                                                    return 1;
                                                                                }
                                                                        )
                                                        )
                                        )
                        )
                        .then(
                                CommandManager.literal("player")
                                        .then(
                                                CommandManager.literal("mace-kit")
                                                        .then(
                                                                CommandManager.argument("netherite", BoolArgumentType.bool())
                                                                        .executes(
                                                                                context -> {
                                                                                    PlayerBot.BotAI.ctrlInvMgmt(context.getSource(),!BoolArgumentType.getBool(context,"netherite"),PlayerBot.BotAI.refillInvofuser,true);
                                                                                    return 1;
                                                                                }
                                                                        )
                                                        )
                                        )
                                        .then(
                                                CommandManager.literal("settings")
                                                        .then(
                                                                CommandManager.literal("auto-refill")
                                                                        .then(
                                                                                CommandManager.argument("value", BoolArgumentType.bool())
                                                                                        .executes(
                                                                                                context -> {
                                                                                                    PlayerBot.BotAI.ctrlInvMgmt(context.getSource(),PlayerBot.BotAI.currentMaterialofPlayerisDia,BoolArgumentType.getBool(context, "value"), false);
                                                                                                    return 1;
                                                                                                }
                                                                                        )
                                                                        )
                                                        )
                                        )
                        )
        )
        );
    }
}
