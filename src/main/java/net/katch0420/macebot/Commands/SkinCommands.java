package net.katch0420.macebot.Commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.katch0420.macebot.utils.SkinManager;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;

public class SkinCommands {

    // Suggest online player names
    private static final SuggestionProvider<ServerCommandSource> PLAYER_SUGGESTIONS = (ctx, builder) -> {
        for (ServerPlayerEntity player : ctx.getSource().getServer().getPlayerManager().getPlayerList()) {
            builder.suggest(player.getGameProfile().getName());
        }
        return builder.buildFuture();
    };

    // Suggest PNG files from macebot/skins/
    private static final SuggestionProvider<ServerCommandSource> FILE_SUGGESTIONS = (ctx, builder) -> {
        File skinDir = new File(System.getProperty("user.dir"), "macebot/skins/");
        if (skinDir.exists() && skinDir.isDirectory()) {
            Arrays.stream(Objects.requireNonNull(skinDir.listFiles((dir, name) -> name.endsWith(".png"))))
                    .forEach(file -> builder.suggest(file.getName()));
        }
        return builder.buildFuture();
    };

    public static void Register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, registrationEnvironment) -> dispatcher.register(
                CommandManager.literal("skin")
                        .requires(source -> source.hasPermissionLevel(2)) // only ops
                        .then(CommandManager.argument("player", StringArgumentType.string())
                                .suggests(PLAYER_SUGGESTIONS)
                                .then(
                                        CommandManager.literal("from-file")
                                                .then(
                                                        CommandManager.argument("file", StringArgumentType.string())
                                                                .suggests(FILE_SUGGESTIONS)
                                                                .executes(SkinCommands::file)
                                                )
                                )
                                .then(
                                        CommandManager.literal("from-url")
                                                .then(
                                                        CommandManager.argument("url",StringArgumentType.string())
                                                                .executes(SkinCommands::url)
                                                )
                                )
                        )
                )
        );
    }

    private static int file(CommandContext<ServerCommandSource> ctx) {
        String playerName = StringArgumentType.getString(ctx, "player");
        String fileName = StringArgumentType.getString(ctx, "file");

        ServerPlayerEntity target = ctx.getSource().getServer().getPlayerManager().getPlayer(playerName);

        if (target == null) {
            ctx.getSource().sendError(Text.literal("Player not found"));
            return 0;
        }

        int a = SkinManager.applySkin(target, fileName);
        switch (a){
            case 0 -> {
                ctx.getSource().sendError(Text.literal("Such File Don't Exist"));
            }
            case 1 -> {
                ctx.getSource().sendFeedback(() ->Text.literal(
                        "Applied skin " + fileName + " to " + playerName
                ), true);
            }
            case 2 -> {
                ctx.getSource().sendError(Text.literal("Unexpected Error occurred, Check console for further info."));
            }
        }

        return 1;
    }
    private static int url(CommandContext<ServerCommandSource> ctx){
        String playerName = StringArgumentType.getString(ctx, "player");
        String url = StringArgumentType.getString(ctx, "url");

        ServerPlayerEntity target = ctx.getSource().getServer().getPlayerManager().getPlayer(playerName);

        if (target == null) {
            ctx.getSource().sendError(Text.literal("Player not found"));
            return 0;
        }

        int a = SkinManager.applySkin(target, url);
        switch (a){
            case 0 -> {
                ctx.getSource().sendError(Text.literal("Invalid URL try again"));
            }
            case 1 -> {
                ctx.getSource().sendFeedback(() ->Text.literal(
                        "Applied skin from " + url + " to " + playerName
                ), true);
            }
            case 2 -> {
                ctx.getSource().sendError(Text.literal("Unexpected Error occurred, Check console for further info."));
            }
        }
        return 1;
    }
}
