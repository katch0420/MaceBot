package net.katch0420.macebot.main.settings.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.katch0420.macebot.main.macebot.control.Controller;

@Environment(EnvType.CLIENT)
public class ClientSideSettings {
    //Client-side only
    public static boolean CONNECTED = false;
    //Status
    public static boolean MACEBOT_ONLINE = false;
    public static boolean MACEBOT_PAUSED = true;
    //MaceBot
    public static Controller.Difficulty DIFFICULTY = Controller.Difficulty.PRO;
    public static boolean MACEBOT_CAN_USE_ELYTRA = true;
    public static boolean MACEBOT_AUTO_REFILL_ENABLED = true;
    public static boolean MACEBOT_BUFFS_ENABLED = true;
    public static boolean MACEBOT_CAN_DO_MACE_ATTACK = true;
    public static boolean MACEBOT_CAN_DO_KB_HIT = true;
    public static boolean MACEBOT_CAN_DO_CRIT_HIT = true;
    public static boolean MACEBOT_CAN_DO_ATTACK = true;
    public static boolean MACEBOT_CAN_USE_SHIELD = true;
    public static boolean MACEBOT_CAN_DO_TRACKING = true;

    //Player
    public static boolean PLAYER_AUTO_REFILL_ENABLED = true;
    public static boolean PLAYER_BUFFS_ENABLED = true;

    //General
    public static Controller.Mode MODE = Controller.Mode.FIGHT;
    public static boolean CHAT_MESSAGES_ENABLED = true;
    public static boolean ACTION_BAR_MESSAGES_ENABLED = true;

    public static boolean isMacebotOnline() {
        return MACEBOT_ONLINE;
    }

    public static void setMacebotOnline(boolean macebotOnline) {
        MACEBOT_ONLINE = macebotOnline;
    }

    public static boolean isMacebotPaused() {
        return MACEBOT_PAUSED;
    }

    public static void setMacebotPaused(boolean macebotPaused) {
        MACEBOT_PAUSED = macebotPaused;
    }

    public static boolean isConnected() {
        return CONNECTED;
    }

    public static void setConnected(boolean CONNECTED) {
        ClientSideSettings.CONNECTED = CONNECTED;
    }

    public static Controller.Mode getMode() {
        return MODE;
    }

    public static void setMode(Controller.Mode MODE) {
        ClientSideSettings.MODE = MODE;
    }

    public static Controller.Difficulty getDifficulty() {
        return DIFFICULTY;
    }

    public static void setDifficulty(Controller.Difficulty DIFFICULTY) {
        ClientSideSettings.DIFFICULTY = DIFFICULTY;
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

    public static boolean toggleMacebotCanUseElytra() {
        MACEBOT_CAN_USE_ELYTRA = !MACEBOT_CAN_USE_ELYTRA;
        return MACEBOT_CAN_USE_ELYTRA;
    }

    public static boolean toggleMacebotAutoRefillEnabled() {
        MACEBOT_AUTO_REFILL_ENABLED = !MACEBOT_AUTO_REFILL_ENABLED;
        return MACEBOT_AUTO_REFILL_ENABLED;
    }

    public static boolean toggleMacebotBuffsEnabled() {
        MACEBOT_BUFFS_ENABLED = !MACEBOT_BUFFS_ENABLED;
        return MACEBOT_BUFFS_ENABLED;
    }

    public static boolean toggleMacebotCanDoMaceAttack() {
        MACEBOT_CAN_DO_MACE_ATTACK = !MACEBOT_CAN_DO_MACE_ATTACK;
        return MACEBOT_CAN_DO_MACE_ATTACK;
    }

    public static boolean toggleMacebotCanDoKbHit() {
        MACEBOT_CAN_DO_KB_HIT = !MACEBOT_CAN_DO_KB_HIT;
        return MACEBOT_CAN_DO_KB_HIT;
    }

    public static boolean toggleMacebotCanDoCritHit() {
        MACEBOT_CAN_DO_CRIT_HIT = !MACEBOT_CAN_DO_CRIT_HIT;
        return MACEBOT_CAN_DO_CRIT_HIT;
    }

    public static boolean toggleMacebotCanDoAttack() {
        MACEBOT_CAN_DO_ATTACK = !MACEBOT_CAN_DO_ATTACK;
        return MACEBOT_CAN_DO_ATTACK;
    }

    public static boolean toggleMacebotCanUseShield() {
        MACEBOT_CAN_USE_SHIELD = !MACEBOT_CAN_USE_SHIELD;
        return MACEBOT_CAN_USE_SHIELD;
    }

    public static boolean toggleMacebotCanDoTracking() {
        MACEBOT_CAN_DO_TRACKING = !MACEBOT_CAN_DO_TRACKING;
        return MACEBOT_CAN_DO_TRACKING;
    }

    public static boolean togglePlayerAutoRefillEnabled() {
        PLAYER_AUTO_REFILL_ENABLED = !PLAYER_AUTO_REFILL_ENABLED;
        return PLAYER_AUTO_REFILL_ENABLED;
    }

    public static boolean togglePlayerBuffsEnabled() {
        PLAYER_BUFFS_ENABLED = !PLAYER_BUFFS_ENABLED;
        return PLAYER_BUFFS_ENABLED;
    }

    public static boolean toggleChatMessagesEnabled() {
        CHAT_MESSAGES_ENABLED = !CHAT_MESSAGES_ENABLED;
        return CHAT_MESSAGES_ENABLED;
    }

    public static boolean toggleActionBarMessagesEnabled() {
        ACTION_BAR_MESSAGES_ENABLED = !ACTION_BAR_MESSAGES_ENABLED;
        return ACTION_BAR_MESSAGES_ENABLED;
    }
}
