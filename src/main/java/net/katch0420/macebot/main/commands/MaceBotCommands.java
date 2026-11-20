package net.katch0420.macebot.main.commands;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.katch0420.macebot.main.MaceBot;
import net.katch0420.macebot.main.kits.main.Kit;
import net.katch0420.macebot.main.kits.main.KitStack;
import net.katch0420.macebot.main.kits.server.CustomKitManager;
import net.katch0420.macebot.main.kits.server.KitGiver;
import net.katch0420.macebot.main.kits.server.KitRegistry;
import net.katch0420.macebot.main.macebot.ai.Mode;
import net.katch0420.macebot.main.macebot.ai.PracticeMode;
import net.katch0420.macebot.main.macebot.bot.PlayerBot;
import net.katch0420.macebot.main.messenger.Messages;
import net.katch0420.macebot.main.messenger.ServerChatMessageManager;
import net.katch0420.macebot.main.settings.*;
import net.katch0420.macebot.main.utils.LegacyText;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Items;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class MaceBotCommands {
    private static final SimpleCommandExceptionType INVALID_KIT =
            new SimpleCommandExceptionType(Messages.WARNING.copy().append("Invalid kit ID"));
    private static final SimpleCommandExceptionType ENTER_KIT_ID =
            new SimpleCommandExceptionType(Messages.WARNING.copy().append("Enter kit ID"));
    private static final SimpleCommandExceptionType CHOOSE_KIT_ACTION =
            new SimpleCommandExceptionType(Messages.WARNING.copy().append("Choose kit action"));
    private static final SimpleCommandExceptionType BOT_NOT_SPAWNED =
            new SimpleCommandExceptionType(Messages.WARNING.copy().append("Bot not spawned"));
    private static final SimpleCommandExceptionType BOT_ALREADY_ONLINE =
            new SimpleCommandExceptionType(Messages.WARNING.copy().append("Bot already online"));
    private static final SimpleCommandExceptionType NOT_ENOUGH_PERMISSION =
            new SimpleCommandExceptionType(Messages.WARNING.copy().append("Not enough permissions"));
    private static final SimpleCommandExceptionType SERVER_NOT_ENABLED =
            new SimpleCommandExceptionType(Messages.WARNING.copy().append("Server is not enabled"));
    private static final SimpleCommandExceptionType PLAYER_ONLY_COMMAND =
            new SimpleCommandExceptionType(Messages.WARNING.copy().append("Player only command"));

    public static void Register(Commands c){
        c.getDispatcher().register(
                Commands.literal("macebot").requires(p -> Settings.RESTRICT.getValue() && hasOperatorAccess(p))
                        .then(buildControlCommands())
                        .then(buildSettingsCommands())
                        .then(buildKitCommands())
        );
    }

    private static boolean hasOperatorAccess(CommandSourceStack p) {
        return p.permissions().hasPermission(Permissions.COMMANDS_ADMIN);
    }

    private static LiteralCommandNode<CommandSourceStack> buildControlCommands() {
        List<LiteralArgumentBuilder<CommandSourceStack>> controls = List.of(
                Commands.literal("spawn").executes(
                        c -> {
                            if (PlayerBot.playerBot != null) throw BOT_ALREADY_ONLINE.create();
                            ServerChatMessageManager.info(c.getSource().getPlayerOrException(), Component.literal("MaceBot spawn requested."));
                            PlayerBot.spawnMaceBot(c.getSource().getPlayerOrException());
                            return 1;
                        }
                )
                ,
                Commands.literal("kick").executes(
                        c -> {
                            if (PlayerBot.playerBot == null) throw BOT_NOT_SPAWNED.create();
                            ServerChatMessageManager.info(c.getSource().getPlayerOrException(), Messages.I_BOT_KICK);
                            PlayerBot.kick();
                            return 1;
                        }
                ),
                Commands.literal("play").executes(
                        c -> {
                            if (PlayerBot.playerBot == null) throw BOT_NOT_SPAWNED.create();
                            ServerChatMessageManager.info(c.getSource().getPlayerOrException(), Messages.I_BOT_PLAY);
                            PlayerBot.playerBot.core.play();
                            return 1;
                        }
                ),
                Commands.literal("stop").executes(
                        c -> {
                            if (PlayerBot.playerBot == null) throw BOT_NOT_SPAWNED.create();
                            ServerChatMessageManager.info(c.getSource().getPlayerOrException(), Messages.I_BOT_STOP);
                            PlayerBot.playerBot.core.pause();
                            return 1;
                        }
                ),
                Commands.literal("setName")
                        .then(Commands.argument("name", StringArgumentType.string())
                                .executes(c -> {
                                            PlayerBot.applyNewIdentity(LegacyText.parse(StringArgumentType.getString(c, "name")));
                                            return 1;
                                        }
                                )
                        )

        );
        LiteralArgumentBuilder<CommandSourceStack> b = Commands.literal("controls");
        controls.forEach(b::then);
        b.requires(MaceBotCommands::isServerEnabled);
        return b.build();
    }

    private static LiteralCommandNode<CommandSourceStack> buildSettingsCommands() {
        LiteralArgumentBuilder<CommandSourceStack> settings = Commands.literal("settings");

        for (SettingsCategories.SettingCategory c : SettingsCategories.getCategories()) {
            if(c.hasFlag(Flags.CATEGORY_DO_NOT_DISPLAY)) continue;
            LiteralArgumentBuilder<CommandSourceStack> l = Commands.literal(c.getId());
            c.settings.forEach(
                    s -> {
                        if(s.hasFlag(Flags.SETTING_CAN_NOT_MODIFY) || s.hasFlag(Flags.SETTING_CLIENT_ONLY)) return;
                        if(s.getCategory() == c){
                            buildSettingCommand(l, s);
                        }
                    }
            );
            settings.then(l);
        }
        return settings.build();
    }

    @SuppressWarnings("unchecked")
    private static <T> void buildSettingCommand(LiteralArgumentBuilder<CommandSourceStack> s, Setting<T> setting){
        String literal = setting.getDisplayName().getString().replace(" ", "_").toLowerCase(Locale.ROOT);
        LiteralArgumentBuilder<CommandSourceStack> keyNode = Commands.literal(literal);
        if (setting.getType() == Boolean.class) {
            keyNode.executes(c -> {
                ServerChatMessageManager.sendBoolSettingUpdate(c.getSource().getPlayerOrException(), (Setting<Boolean>) setting);
                return 1;
            });
            keyNode.then(Commands.argument("value", BoolArgumentType.bool()).executes(c -> {
                ServerChatMessageManager.sendBoolSettingUpdate(c.getSource().getPlayerOrException(), (Setting<Boolean>) setting, BoolArgumentType.getBool(c,"value"));
                return 1;
            }));
        }
        else if (setting.getType() == Integer.class) {
            keyNode.then(Commands.argument("value", IntegerArgumentType.integer(
                    setting.getIntegerMin(), setting.getIntegerMax())).executes(c -> {
                ServerChatMessageManager.sendIntegerSettingUpdate(c.getSource().getPlayerOrException(),
                        (Setting<Integer>) setting, IntegerArgumentType.getInteger(c, "value"));
                return 1;
            }));
        }
        else if (setting.getType().isEnum()) {
            ArgumentType<?> arg = getEnumArgumentType(setting);
            if (arg == null) {
                MaceBot.LOGGER.warn("No command argument type registered for enum setting {} ({})",
                        setting.getId(), setting.getType().getName());
                return;
            }
            keyNode.then(Commands.argument("value", arg).executes(c -> {
                ServerChatMessageManager.sendEnumSettingUpdate(c.getSource().getPlayerOrException(), setting, (T) getEnumArgumentValue(c, arg, "value"));
                return 1;
            }));
        }
        if(setting.hasFlag(Flags.SETTING_RESTRICTED)) keyNode.requires(MaceBotCommands::hasOperatorAccess);
        if(setting != Settings.SERVER) keyNode.requires(MaceBotCommands::isServerEnabled);
        s.then(keyNode);
    }

    private static LiteralCommandNode<CommandSourceStack> buildKitCommands() {
        LiteralArgumentBuilder<CommandSourceStack> k = Commands.literal("kits");

        LiteralArgumentBuilder<CommandSourceStack> n = Commands.literal("new");

        LiteralArgumentBuilder<CommandSourceStack> em = Commands.literal("empty");
        LiteralArgumentBuilder<CommandSourceStack> f = Commands.literal("fromInv");

        LiteralArgumentBuilder<CommandSourceStack> l = Commands.literal("load");
        LiteralArgumentBuilder<CommandSourceStack> du = Commands.literal("duplicate");
        LiteralArgumentBuilder<CommandSourceStack> in = Commands.literal("info");
        LiteralArgumentBuilder<CommandSourceStack> i = Commands.literal("copyInv");
        LiteralArgumentBuilder<CommandSourceStack> d = Commands.literal("delete");

        LiteralArgumentBuilder<CommandSourceStack> it = Commands.literal("items");

        k.executes(c -> {throw ENTER_KIT_ID.create();})
                .then(n.then(em.then(Commands.argument("name", StringArgumentType.string()).executes(MaceBotCommands::newKit))).then(f.then(Commands.argument("name", StringArgumentType.string()).executes(MaceBotCommands::newKitFromInv))))
                .then(Commands.argument("kit", StringArgumentType.word())
                        .suggests(MaceBotCommands::getAllKitSuggestions)
                        .executes(c -> {throw CHOOSE_KIT_ACTION.create();})
                        .then(l.then(Commands.argument("target", StringArgumentType.word())
                                .suggests(MaceBotCommands::getKitLoadTargets)
                                .executes(c -> giveKit(c, false, true, true))
                                .then(Commands.literal("unbreakable").executes(c -> giveKit(c, true, false, false)))
                                .then(Commands.literal("unbreakingAndMending").executes(c -> giveKit(c, false, true, true)))
                                .then(Commands.literal("unbreakingOnly").executes(c -> giveKit(c, false, true, false)))
                                .then(Commands.literal("mendingOnly").executes(c -> giveKit(c, false, false, true)))
                        ))
                        .then(du.executes(c -> {
                            Kit src = getKitOrError(c);
                            String newId = CustomKitManager.generateId(src.getDisplayName());
                            Kit dup = new Kit(newId, src.getDisplayName() + " (Copy)", src.getIconId(), true);
                            src.getItems().forEach(dup::addItem);
                            KitRegistry.register(dup);
                            ServerChatMessageManager.info(c.getSource().getPlayerOrException(),
                                    Component.literal("Kit duplicated: " + dup.getDisplayName()));
                            return 1;
                        }))
                        .then(in.then(it.executes(MaceBotCommands::sendKitInfoWithItemList)).executes(MaceBotCommands::sendKitInfo))
                        .then(i.executes(MaceBotCommands::copyInv))
                        .then(d.executes(c -> {
                            Kit kit = getKitOrError(c);
                            if(kit.isCustom()) {
                                ServerChatMessageManager.info(c.getSource().getPlayerOrException(), Component.literal("Kit deleted: " + kit.getDisplayName()));
                                KitRegistry.unregister(kit.getId());
                            } else {
                                ServerChatMessageManager.warn(c.getSource().getPlayerOrException(), Component.literal("Cannot delete built-in kits"));
                            }
                            return 1;
                        }))
                );
        k.requires(MaceBotCommands::isServerEnabled);
        return k.build();
    }

    private static boolean isServerEnabled(CommandSourceStack p) {
        if(Settings.SERVER.getValue()) {
            return true;
        }
        p.sendFailure(Messages.ERROR.copy().append("Please Enable Macebot on server side before executing. /macebot settings server true"));
        return false;
    }

    private static int newKitFromInv(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        Kit kit = newKitFromDisplayName(StringArgumentType.getString(c, "name"));
        ServerPlayer p = c.getSource().getPlayerOrException();
        Inventory inv = p.getInventory();
        for (int i = 0; i < 41; i++) {
            kit.addItem(i, KitStack.fromStack(inv.getItem(i), i));
        }
        KitRegistry.register(kit);
        ServerChatMessageManager.info(p, Component.literal("Created New Kit from Inventory: "+ kit.getDisplayName()));
        return 1;
    }

    private static int newKit(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        Kit kit = newKitFromDisplayName(StringArgumentType.getString(c, "name"));
        ServerPlayer p = c.getSource().getPlayerOrException();
        ServerChatMessageManager.info(p, Component.literal("Created New Kit: "+ kit.getDisplayName()));
        return 1;
    }

    private static Kit newKitFromDisplayName(String name){
        String id = CustomKitManager.generateId(name);
        Kit kit = new Kit(id, name, BuiltInRegistries.ITEM.getKey(Items.WOODEN_SWORD), true);
        KitRegistry.register(kit);
        return kit;
    }

    private static int copyInv(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        Kit kit = getKitOrError(c);
        ServerPlayer p = c.getSource().getPlayerOrException();
        if(kit.isCustom()) {
            Inventory inv = p.getInventory();
            for (int i = 0; i < 41; i++) {
                kit.addItem(i, KitStack.fromStack(inv.getItem(i), i));
            }
            KitRegistry.register(kit);
        } else {
            ServerChatMessageManager.warn(p, Component.literal("Cannot Modify Built-in Kits"));
        }
        return 1;
    }

    private static int sendKitInfoWithItemList(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        Kit kit = getKitOrError(c);
        ServerPlayer p = c.getSource().getPlayerOrException();
        List<String> l = List.of(
                "§f§m           §eKit Information§f§m           ",
                "§aName: §7" + kit.getDisplayName(),
                "§aID: §7" + kit.getId(),
                "§aCustom Kit: §7" + kit.isCustom()
        );
        l.forEach(s -> ServerChatMessageManager.send(p, Component.literal(s)));

        List<String> il = new ArrayList<>();
        il.add("§aItems: ");
        for(Map.Entry<Integer, KitStack> e: kit.getItems().entrySet()) {
            if(e.getValue() == null || e.getValue().toStack().isEmpty()) continue;
            il.add("§7 - §fSlot §a" + e.getValue().slot + " §fItem: §a" + e.getValue().toStack().toString());
        }
        il.forEach(s -> ServerChatMessageManager.send(p, Component.literal(s)));
        ServerChatMessageManager.send(p, Component.literal("§f§m                              "));
        return 1;
    }

    private static int sendKitInfo(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        Kit kit = getKitOrError(c);
        ServerPlayer p = c.getSource().getPlayerOrException();
        List<String> l = List.of(
                "§f§m           §eKit Information§f§m           ",
                "§aName: §7" + kit.getDisplayName(),
                "§aID: §7" + kit.getId(),
                "§aCustom Kit: §7" + kit.isCustom()
        );
        l.forEach(s -> ServerChatMessageManager.send(p, Component.literal(s)));

        ServerChatMessageManager.send(p, Component.literal("§e[Click here to view items]").withStyle(Style.EMPTY.withClickEvent(
                //? if >=1.21.5 {
                new ClickEvent.RunCommand("/macebot kits " + kit.getId() + " info items")
                //?} else
                //new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/bot kits " + kit.getId() + " info items")
        )));
        ServerChatMessageManager.send(p, Component.literal("§f§m                              "));
        return 1;
    }

    private static int giveKit(CommandContext<CommandSourceStack> c, boolean u, boolean un, boolean m) throws CommandSyntaxException {
        String target = StringArgumentType.getString(c, "target");
        Kit kit = getKitOrError(c);

        switch (target){
            case "MACEBOT" -> {
                if (PlayerBot.playerBot == null) throw BOT_NOT_SPAWNED.create();
                KitGiver.giveKit(PlayerBot.playerBot, kit, u, un, m);
            }
            case "PLAYER" -> KitGiver.giveKit(c.getSource().getPlayerOrException(), kit, u, un, m);
            case "ALL_PLAYERS" -> c.getSource().getServer().getPlayerList().getPlayers().forEach(p -> {
                if(!kit.isCustom() && p != PlayerBot.playerBot) {
                    KitGiver.giveKit(p, kit, u, un, m);
                }
            });
            default -> {
                ServerChatMessageManager.warn(c.getSource().getPlayerOrException(), Component.literal("Unknown target: " + target));
            }
        }
        return 1;
    }

    private static Kit getKitOrError(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        Kit kit = KitRegistry.get(StringArgumentType.getString(c, "kit"));
        if(kit == null) throw INVALID_KIT.create();
        return kit;
    }

    private static CompletableFuture<Suggestions> getKitLoadTargets(CommandContext<CommandSourceStack> commandSourceStackCommandContext, SuggestionsBuilder builder) {
        List<String> l = List.of("MACEBOT", "PLAYER", "ALL_PLAYERS");
        String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);
        for(String cl: l){
            if (cl.toLowerCase(Locale.ROOT).startsWith(remaining)) {
                builder.suggest(cl);
            }
        }
        return builder.buildFuture();
    }

    private static CompletableFuture<Suggestions> getAllKitSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);
        for(Kit kit: KitRegistry.all()){
            if (kit.getId().toLowerCase(Locale.ROOT).startsWith(remaining)) {
                builder.suggest(kit.getId());
            }
        }
        return builder.buildFuture();
    }

    private static Object getEnumArgumentValue(CommandContext<CommandSourceStack> c, ArgumentType<?> arg, String id) {
        if(arg instanceof Mode.ModeArgumentType){
            return Mode.ModeArgumentType.getMode(c,id);
        }
        if(arg instanceof PracticeMode.PracticeModeArgumentType){
            return PracticeMode.PracticeModeArgumentType.getPracticeMode(c,id);
        }
        MaceBot.LOGGER.warn("Unknown Type of Enum Value has been requested: {}", arg);
        return null;
    }

    private static ArgumentType<?> getEnumArgumentType(Setting<?> setting) {
        if (setting.getType() == Mode.class){
            return new Mode.ModeArgumentType();
        } else if (setting.getType() == PracticeMode.class){
            return new PracticeMode.PracticeModeArgumentType();
        }
        MaceBot.debugLog("Unknown Type of Enum has been registered: " + setting.getType());
        return null;
    }

}
