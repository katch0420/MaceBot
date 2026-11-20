package net.katch0420.macebot.main.settings;

import net.katch0420.macebot.main.MaceBot;
import net.katch0420.macebot.main.macebot.ai.Mode;
import net.katch0420.macebot.main.macebot.ai.PracticeMode;
import net.katch0420.macebot.main.messenger.ClientChatMessageManager;
import net.katch0420.macebot.main.messenger.Messages;
import net.katch0420.macebot.main.messenger.ServerChatMessageManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
//? if >=1.21.11 {
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.world.entity.player.Player;
//?}

public class Settings {

    public static Setting<Boolean> MACEBOT_ONLINE = SettingManager.register(new Setting<>(Boolean.class, Component.nullToEmpty("MaceBot Online"), "macebot_online", SettingsCategories.STATUS, false, Boolean::parseBoolean, Object::toString)).setFlags(Flags.SETTING_CAN_NOT_MODIFY);
    public static Setting<Integer> MACEBOT_ID = SettingManager.register(new Setting<>(Integer.class, Component.nullToEmpty("MaceBot ID"), "macebot_id", SettingsCategories.STATUS, -1, Integer::parseInt, Object::toString)).setFlags(Flags.SETTING_CAN_NOT_MODIFY);

    public static Setting<Integer> SKILL = SettingManager.register(new Setting<>(Integer.class, Component.nullToEmpty("Skill Level"), "skill", SettingsCategories.MACEBOT, 50, Integer::parseInt, Object::toString).setIntegerRange(0, 100));
    public static Setting<Boolean> MACEBOT_CAN_USE_ELYTRA = SettingManager.register(new Setting<>(Boolean.class, Component.nullToEmpty("Can Use Elytra"), "macebot_can_use_elytra", SettingsCategories.MACEBOT, true, Boolean::parseBoolean, Object::toString));
    public static Setting<Boolean> MACEBOT_AUTO_REFILL = SettingManager.register(new Setting<>(Boolean.class, Component.nullToEmpty("Auto Refill"), "macebot_auto_refill", SettingsCategories.MACEBOT, true, Boolean::parseBoolean, Object::toString));
    public static Setting<Boolean> MACEBOT_BUFFS = SettingManager.register(new Setting<>(Boolean.class, Component.nullToEmpty("Buffs"), "macebot_buffs", SettingsCategories.MACEBOT, true, Boolean::parseBoolean, Object::toString));
    public static Setting<Boolean> MACEBOT_CAN_DO_MACE_ATTACK = SettingManager.register(new Setting<>(Boolean.class, Component.nullToEmpty("Mace Attack"), "macebot_mace_attack", SettingsCategories.MACEBOT, true, Boolean::parseBoolean, Object::toString));
    public static Setting<Boolean> MACEBOT_CAN_DO_KB_HIT = SettingManager.register(new Setting<>(Boolean.class, Component.nullToEmpty("Kb Hits"), "macebot_kb_hits", SettingsCategories.MACEBOT, true, Boolean::parseBoolean, Object::toString));
    public static Setting<Boolean> MACEBOT_CAN_DO_CRIT_HIT = SettingManager.register(new Setting<>(Boolean.class, Component.nullToEmpty("Crit Hits"), "macebot_crit_hits", SettingsCategories.MACEBOT, true, Boolean::parseBoolean, Object::toString));
    public static Setting<Boolean> MACEBOT_CAN_DO_STRAFE = SettingManager.register(new Setting<>(Boolean.class, Component.nullToEmpty("Strafe"), "macebot_strafe", SettingsCategories.MACEBOT, true, Boolean::parseBoolean, Object::toString));
    public static Setting<Boolean> MACEBOT_CAN_DO_ATTACK = SettingManager.register(new Setting<>(Boolean.class, Component.nullToEmpty("Can Attack"), "macebot_can_attack", SettingsCategories.MACEBOT, true, Boolean::parseBoolean, Object::toString));
    public static Setting<Boolean> MACEBOT_CAN_USE_SHIELD = SettingManager.register(new Setting<>(Boolean.class, Component.nullToEmpty("Can Use Shield"), "macebot_can_shield", SettingsCategories.MACEBOT, false, Boolean::parseBoolean, Object::toString));
    public static Setting<Boolean> MACEBOT_CAN_DO_TRACKING = SettingManager.register(new Setting<>(Boolean.class, Component.nullToEmpty("Can Track Player"), "macebot_can_track", SettingsCategories.MACEBOT, true, Boolean::parseBoolean, Object::toString));
    public static Setting<Boolean> MACEBOT_CAN_HEAL = SettingManager.register(new Setting<>(Boolean.class, Component.nullToEmpty("Can Heal"), "macebot_can_heal", SettingsCategories.MACEBOT, true, Boolean::parseBoolean, Object::toString));
    public static Setting<Boolean> MACEBOT_CAN_GRAPPLING = SettingManager.register(new Setting<>(Boolean.class, Component.nullToEmpty("Can Do Grappling"), "macebot_can_grappling", SettingsCategories.MACEBOT, true, Boolean::parseBoolean, Object::toString ));
    public static Setting<Boolean> MACEBOT_CAN_RETREAT = SettingManager.register(new Setting<>(Boolean.class, Component.nullToEmpty("Can Retreat"), "macebot_can_retreat", SettingsCategories.MACEBOT, true, Boolean::parseBoolean, Object::toString));

    public static Setting<Integer> OPPONENT_ID = SettingManager.register(new Setting<>(Integer.class, Component.nullToEmpty("Opponent ID"), "opponent_id", SettingsCategories.PLAYER, -1, Integer::parseInt, Object::toString)).setFlags(Flags.SETTING_CAN_NOT_MODIFY);
    public static Setting<Boolean> PLAYER_AUTO_REFILL = SettingManager.register(new Setting<>(Boolean.class, Component.nullToEmpty("Auto Refill"), "player_auto_refill", SettingsCategories.PLAYER, false, Boolean::parseBoolean, Object::toString));
    public static Setting<Boolean> PLAYER_BUFFS = SettingManager.register(new Setting<>(Boolean.class, Component.nullToEmpty("Buffs"), "player_buffs", SettingsCategories.PLAYER, false, Boolean::parseBoolean, Object::toString));

    public static Setting<Mode> MODE = SettingManager.register(new Setting<>(Mode.class, Component.nullToEmpty("Mode"), "mode", SettingsCategories.MOD, Mode.NPC, Mode::valueOf, Enum::toString));
    public static Setting<PracticeMode> PRACTICE_MODE = SettingManager.register(new Setting<>(PracticeMode.class, Component.nullToEmpty("Practice Mode"), "practice_mode", SettingsCategories.MOD, PracticeMode.STUN_SLAM, PracticeMode::valueOf, Enum::toString));
    public static Setting<Boolean> COMBAT_HITBOX = SettingManager.register(new Setting<>(Boolean.class, Component.nullToEmpty("Combat Hitbox"), "combat_hitbox", SettingsCategories.MOD, true, Boolean::parseBoolean, Object::toString)).setFlags(Flags.SETTING_CLIENT_ONLY, Flags.SETTING_RESTRICTED);
    public static Setting<Boolean> CHAT_MESSAGES = SettingManager.register(new Setting<>(Boolean.class, Component.nullToEmpty("Chat Message"), "chat_message", SettingsCategories.MOD, false, Boolean::parseBoolean, Object::toString));
    public static Setting<Boolean> ACTION_BAR_MESSAGES = SettingManager.register(new Setting<>(Boolean.class, Component.nullToEmpty("Action Bar Message"), "action_message", SettingsCategories.MOD, true, Boolean::parseBoolean, Object::toString));
    public static Setting<Boolean> RESTRICT = SettingManager.register(new Setting<>(Boolean.class, Component.nullToEmpty("Restrict Access"), "restricted", SettingsCategories.MOD, true, Boolean::parseBoolean, Object::toString)).setFlags(Flags.SETTING_RESTRICTED);
    public static Setting<Boolean> CLIENT = SettingManager.register(new Setting<>(Boolean.class, Component.nullToEmpty("Client Side"), "client_side", SettingsCategories.MOD, true, Boolean::parseBoolean, Object::toString)).setFlags(Flags.SETTING_CLIENT_ONLY, Flags.SETTING_RESTRICTED);
    public static Setting<Boolean> SERVER = SettingManager.register(new Setting<>(Boolean.class, Component.nullToEmpty("Server Side"), "server_side", SettingsCategories.MOD, true, Boolean::parseBoolean, Object::toString)).setFlags(Flags.SETTING_RESTRICTED);;

    public static boolean hasAccess(ServerPlayer player){
        return hasAccess(player, true);
    }

    public static boolean hasLevel3Permission(Player player){
        //? if <1.21.11 {
        /*return player.hasPermissions(3);
         *///?} else {
        PermissionSet set = player.permissions();
        return set == LevelBasedPermissionSet.OWNER || set == LevelBasedPermissionSet.ADMIN;
        //?}
    }

    public static boolean hasAccess(Player player, boolean sendMsg) {
        if(Env.getEnv() == Env.CLIENT) {
            MaceBot.debugLog("Invalid Environment");
            return false;
        }
        if(Settings.RESTRICT.getValue()) {
            if (hasLevel3Permission(player)) return true;
            else if (sendMsg) {
                ServerChatMessageManager.warn((ServerPlayer) player, Messages.W_RESTRICTED_ACCESS);
                return false;
            }
        }
        return true;
    }

    public static boolean hasAccess(boolean sendMsg){
        if(Env.getEnv() == Env.SERVER) {
            MaceBot.debugLog("Invalid Environment");
            return false;
        }
        if(Settings.RESTRICT.getValue()) {
            if (Minecraft.getInstance().player != null && hasLevel3Permission(Minecraft.getInstance().player)) return true;
            else if (sendMsg) {
                ClientChatMessageManager.warn(Messages.W_RESTRICTED_ACCESS);
                return false;
            }
        }
        return true;
    }
}