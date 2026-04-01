package net.katch0420.macebot.main.commands;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.katch0420.macebot.main.kits.server.KitGiver;
import net.katch0420.macebot.main.kits.server.KitRegistry;
import net.katch0420.macebot.main.settings.main.SettingsKey;
import net.katch0420.macebot.main.settings.server.SettingsSyncHelper;
import net.katch0420.macebot.main.utils.Colors;
import net.katch0420.macebot.main.utils.Messenger;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class PlayerCommands {

    private static final SuggestionProvider<ServerCommandSource> KIT_SUGGESTIONS =
            (ctx, builder) -> {
                KitRegistry.getAllIds().forEach(id -> {
                    var kit = KitRegistry.get(id);
                    builder.suggest(id, Text.literal(kit != null ? kit.getDisplayName() : id));
                });
                return builder.buildFuture();
            };

    private static ArgumentBuilder<ServerCommandSource, ?> toggle(
            String name, SettingsKey key, String label) {
        return CommandManager.literal(name)
                .executes(ctx -> {
                    boolean next = !((Boolean) key.getServerValue());
                    SettingsSyncHelper.applyAndBroadcast(key, next, ctx.getSource().getServer());
                    Messenger.add(label + ": ", Colors.ComponentColor);
                    Messenger.add(next ? "enabled" : "disabled",
                            next ? Colors.TrueColor : Colors.FalseColor);
                    Messenger.send(ctx.getSource().getPlayer(), true, true);
                    return 1;
                });
    }

    private static int givePlayerKit(CommandContext<ServerCommandSource> ctx,
                                     String kitId, boolean unbreakable) {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        if (player == null) return 0;

        var kit = KitRegistry.get(kitId);
        if (kit == null) {
            Messenger.add("Unknown kit: ", Colors.FalseColor);
            Messenger.add(kitId, Colors.BaseColor);
            Messenger.send(player, true, true);
            return 0;
        }

        KitGiver.giveBuiltInKit(player, kit, unbreakable);
        Messenger.add("Gave ", Colors.BaseColor);
        if (unbreakable) Messenger.add("unbreakable ", Colors.BaseColor);
        Messenger.add(kit.getDisplayName(), Colors.BaseColor);
        Messenger.add(" to " + player.getName().getString(), Colors.BaseColor);
        Messenger.send(player, true, true);
        return 1;
    }

    public static void Register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, env) ->
                dispatcher.register(
                        CommandManager.literal("macebot")
                                .then(CommandManager.literal("player")

                                        .then(CommandManager.literal("kit")
                                                .then(CommandManager.argument("kitId", StringArgumentType.word())
                                                        .suggests(KIT_SUGGESTIONS)
                                                        .executes(ctx -> givePlayerKit(ctx,
                                                                StringArgumentType.getString(ctx, "kitId"), true))
                                                        .then(CommandManager.argument("unbreakable", BoolArgumentType.bool())
                                                                .executes(ctx -> givePlayerKit(ctx,
                                                                        StringArgumentType.getString(ctx, "kitId"),
                                                                        BoolArgumentType.getBool(ctx, "unbreakable"))))))

                                        .then(CommandManager.literal("settings")
                                                .then(toggle("auto-refill", SettingsKey.PLAYER_AUTO_REFILL_ENABLED, "Auto Refill"))
                                                .then(toggle("buffs",       SettingsKey.PLAYER_BUFFS_ENABLED,       "Buffs")))
                                )
                )
        );
    }
}