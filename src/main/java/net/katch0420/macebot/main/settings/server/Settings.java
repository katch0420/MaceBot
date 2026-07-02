package net.katch0420.macebot.main.settings.server;

import net.katch0420.macebot.client.MaceBotClient;
import net.katch0420.macebot.main.macebot.control.Controller;
import net.katch0420.macebot.main.macebot.control.Difficulty;
import net.katch0420.macebot.main.messenger.ModMessages;
import net.katch0420.macebot.main.settings.client.ClientSideSettings;
import net.minecraft.server.network.ServerPlayerEntity;

public class Settings {

    //Status
    public static boolean MACEBOT_ONLINE = false;
    public static int MACEBOT_ID = -1;
    //MaceBot
    public static Difficulty DIFFICULTY = Difficulty.PRO;
    public static boolean MACEBOT_CAN_USE_ELYTRA = true;
    public static boolean MACEBOT_AUTO_REFILL_ENABLED = true;
    public static boolean MACEBOT_BUFFS_ENABLED = true;
    public static boolean MACEBOT_CAN_DO_MACE_ATTACK = true;
    public static boolean MACEBOT_CAN_DO_KB_HIT = true;
    public static boolean MACEBOT_CAN_DO_CRIT_HIT = true;
    public static boolean MACEBOT_CAN_DO_ATTACK = true;
    public static boolean MACEBOT_CAN_USE_SHIELD = false;
    public static boolean MACEBOT_CAN_DO_TRACKING = true;

    //Player
    public static int OPPONENT_ID = -1;
    public static boolean PLAYER_AUTO_REFILL_ENABLED = true;
    public static boolean PLAYER_BUFFS_ENABLED = true;

    //General
    public static Controller.Mode MODE = Controller.Mode.NPC;
    public static Controller.PracticeMode PRACTICE_MODE = Controller.PracticeMode.STUN_SLAM;
    public static boolean CHAT_MESSAGES_ENABLED = true;
    public static boolean ACTION_BAR_MESSAGES_ENABLED = true;
    public static boolean OPS_ONLY = true;
    public static boolean MOD_ENABLED = true;

    public static boolean isMacebotOnline() {
        return MACEBOT_ONLINE;
    }

    public static void setMacebotOnline(boolean macebotOnline) {
        MACEBOT_ONLINE = macebotOnline;
    }

    public static int getMacebotId() {
        return MACEBOT_ID;
    }

    public static void setMacebotId(int macebotId) {
        MACEBOT_ID = macebotId;
    }

    public static Controller.Mode getMode() {
        return MODE;
    }

    public static void setMode(Controller.Mode MODE) {
        Settings.MODE = MODE;
    }

    public static Controller.PracticeMode getPracticeMode() {
        return PRACTICE_MODE;
    }

    public static void setPracticeMode(Controller.PracticeMode practiceMode) {
        PRACTICE_MODE = practiceMode;
    }

    public static Difficulty getDifficulty() {
        return DIFFICULTY;
    }

    public static void setDifficulty(Difficulty DIFFICULTY) {
        Settings.DIFFICULTY = DIFFICULTY;
    }

    public static boolean isMacebotCanDoKbHit() {
        return MACEBOT_CAN_DO_KB_HIT;
    }

    public static void setMacebotCanDoKbHit(boolean macebotCanDoKbHit) {
        MACEBOT_CAN_DO_KB_HIT = macebotCanDoKbHit;
    }

    public static boolean isMacebotCanUseElytra() {
        return MACEBOT_CAN_USE_ELYTRA;
    }

    public static void setMacebotCanUseElytra(boolean macebotCanUseElytra) {
        MACEBOT_CAN_USE_ELYTRA = macebotCanUseElytra;
    }

    public static boolean isMacebotAutoRefillEnabled() {
        return MACEBOT_AUTO_REFILL_ENABLED;
    }

    public static void setMacebotAutoRefillEnabled(boolean macebotAutoRefillEnabled) {
        MACEBOT_AUTO_REFILL_ENABLED = macebotAutoRefillEnabled;
    }

    public static boolean isMacebotBuffsEnabled() {
        return MACEBOT_BUFFS_ENABLED;
    }

    public static void setMacebotBuffsEnabled(boolean macebotBuffsEnabled) {
        MACEBOT_BUFFS_ENABLED = macebotBuffsEnabled;
    }

    public static boolean isMacebotCanDoMaceAttack() {
        return MACEBOT_CAN_DO_MACE_ATTACK;
    }

    public static void setMacebotCanDoMaceAttack(boolean macebotCanDoMaceAttack) {
        MACEBOT_CAN_DO_MACE_ATTACK = macebotCanDoMaceAttack;
    }

    public static boolean isMacebotCanDoCritHit() {
        return MACEBOT_CAN_DO_CRIT_HIT;
    }

    public static void setMacebotCanDoCritHit(boolean macebotCanDoCritHit) {
        MACEBOT_CAN_DO_CRIT_HIT = macebotCanDoCritHit;
    }

    public static boolean isMacebotCanDoAttack() {
        return MACEBOT_CAN_DO_ATTACK;
    }

    public static void setMacebotCanDoAttack(boolean macebotCanDoAttack) {
        MACEBOT_CAN_DO_ATTACK = macebotCanDoAttack;
    }

    public static boolean isMacebotCanUseShield() {
        return MACEBOT_CAN_USE_SHIELD;
    }

    public static void setMacebotCanUseShield(boolean macebotCanUseShield) {
        MACEBOT_CAN_USE_SHIELD = macebotCanUseShield;
    }

    public static boolean isMacebotCanDoTracking() {
        return MACEBOT_CAN_DO_TRACKING;
    }

    public static void setMacebotCanDoTracking(boolean macebotCanDoTracking) {
        MACEBOT_CAN_DO_TRACKING = macebotCanDoTracking;
    }


    public static int getOpponentId() {
        return OPPONENT_ID;
    }

    public static void setOpponentId(int opponentId) {
        OPPONENT_ID = opponentId;
    }

    public static boolean isPlayerAutoRefillEnabled() {
        return PLAYER_AUTO_REFILL_ENABLED;
    }

    public static void setPlayerAutoRefillEnabled(boolean playerAutoRefillEnabled) {
        PLAYER_AUTO_REFILL_ENABLED = playerAutoRefillEnabled;
    }

    public static boolean isPlayerBuffsEnabled() {
        return PLAYER_BUFFS_ENABLED;
    }

    public static void setPlayerBuffsEnabled(boolean playerBuffsEnabled) {
        PLAYER_BUFFS_ENABLED = playerBuffsEnabled;
    }

    public static boolean isChatMessagesEnabled() {
        return CHAT_MESSAGES_ENABLED;
    }

    public static void setChatMessagesEnabled(boolean chatMessagesEnabled) {
        CHAT_MESSAGES_ENABLED = chatMessagesEnabled;
    }

    public static boolean isActionBarMessagesEnabled() {
        return ACTION_BAR_MESSAGES_ENABLED;
    }

    public static void setActionBarMessagesEnabled(boolean actionBarMessagesEnabled) {
        ACTION_BAR_MESSAGES_ENABLED = actionBarMessagesEnabled;
    }

    public static boolean isOpsOnly() {
        return OPS_ONLY;
    }

    public static void setOpsOnly(boolean opsOnly) {
        OPS_ONLY = opsOnly;
    }

    public static boolean isModEnabled() {
        return MOD_ENABLED;
    }

    public static void setModEnabled(boolean modEnabled) {
        MOD_ENABLED = modEnabled;
    }

    public static boolean hasAccess(ServerPlayerEntity player) {
        if (player.hasPermissionLevel(3) && Settings.isOpsOnly()) return true;
        else
            player.sendMessage(ModMessages.WARNING.copy().append(ModMessages.CLIENT_WARN_RESTRICTED_ACTION));
        return false;
    }
}