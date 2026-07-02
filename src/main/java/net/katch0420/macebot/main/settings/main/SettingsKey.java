package net.katch0420.macebot.main.settings.main;

import net.katch0420.macebot.main.macebot.control.Controller;
import net.katch0420.macebot.main.macebot.control.Difficulty;
import net.katch0420.macebot.main.settings.client.ClientSideSettings;
import net.katch0420.macebot.main.settings.server.Settings;
import net.katch0420.macebot.main.settings.main.Flags.Flag;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Single source-of-truth for every setting synced between server and clients.
 * <p>
 * Each entry carries:
 * - the value type
 * - server getter/setter  → reads/writes Settings
 * - client getter/setter  → reads/writes ClientSideSettings
 * - a {@link SettingsCategory} + human-readable display name, so generic UI
 *   (e.g. SettingsBody) can group and label settings WITHOUT any per-setting
 *   UI code - add a constant here and it just shows up in the right tab.
 * <p>
 * This means NO hardcoded switches are needed anywhere else — packets, helpers,
 * commands and screens all just call key.getServerValue() / key.applyToClient() etc.
 * <p>
 * ── To add a new setting ─────────────────────────────────────────────────────
 * 1. Add the backing field + getter/setter to ClientSideSettings AND Settings.
 * 2. Add one constant below with its category + display label.
 * That's it - SettingsBody will render it automatically with the right control
 * type (toggle for booleans, cycle button for enums).
 */
public enum SettingsKey {

    // ── Status flags (set by lifecycle, broadcast to all clients; read-only in UI) ─
    MACEBOT_ONLINE(
            Boolean.class, SettingsCategory.STATUS, "MaceBot Online", false,
            Settings::isMacebotOnline,
            v -> Settings.setMacebotOnline((Boolean) v),
            ClientSideSettings::isMacebotOnline,
            v -> ClientSideSettings.setMacebotOnline((Boolean) v)
    ),

    MACEBOT_ID(
            Integer.class, SettingsCategory.STATUS, "MaceBot Entity ID", false,
            Settings::getMacebotId,
            v -> Settings.setMacebotId((Integer) v),
            ClientSideSettings::getMacebotId,
            v -> ClientSideSettings.setMacebotId((Integer) v)
    ),

    // ── MaceBot — enum ────────────────────────────────────────────────────────
    DIFFICULTY(
            Difficulty.class, SettingsCategory.MACEBOT, "Difficulty", true,
            Settings::getDifficulty,
            v -> Settings.setDifficulty((Difficulty) v),
            ClientSideSettings::getDifficulty,
            v -> ClientSideSettings.setDifficulty((Difficulty) v)
    ),

    // ── MaceBot — booleans ────────────────────────────────────────────────────
    MACEBOT_CAN_USE_ELYTRA(
            Boolean.class, SettingsCategory.MACEBOT, "Can Use Elytra", true,
            Settings::isMacebotCanUseElytra,
            v -> Settings.setMacebotCanUseElytra((Boolean) v),
            ClientSideSettings::isMacebotCanUseElytra,
            v -> ClientSideSettings.setMacebotCanUseElytra((Boolean) v)
    ),
    MACEBOT_AUTO_REFILL_ENABLED(
            Boolean.class, SettingsCategory.MACEBOT, "Auto-Refill", true,
            Settings::isMacebotAutoRefillEnabled,
            v -> Settings.setMacebotAutoRefillEnabled((Boolean) v),
            ClientSideSettings::isMacebotAutoRefillEnabled,
            v -> ClientSideSettings.setMacebotAutoRefillEnabled((Boolean) v)
    ),
    MACEBOT_BUFFS_ENABLED(
            Boolean.class, SettingsCategory.MACEBOT, "Buffs Enabled", true,
            Settings::isMacebotBuffsEnabled,
            v -> Settings.setMacebotBuffsEnabled((Boolean) v),
            ClientSideSettings::isMacebotBuffsEnabled,
            v -> ClientSideSettings.setMacebotBuffsEnabled((Boolean) v)
    ),
    MACEBOT_CAN_DO_MACE_ATTACK(
            Boolean.class, SettingsCategory.MACEBOT, "Mace Attack", true,
            Settings::isMacebotCanDoMaceAttack,
            v -> Settings.setMacebotCanDoMaceAttack((Boolean) v),
            ClientSideSettings::isMacebotCanDoMaceAttack,
            v -> ClientSideSettings.setMacebotCanDoMaceAttack((Boolean) v)
    ),
    MACEBOT_CAN_DO_KB_HIT(
            Boolean.class, SettingsCategory.MACEBOT, "Knockback Hit", true,
            Settings::isMacebotCanDoKbHit,
            v -> Settings.setMacebotCanDoKbHit((Boolean) v),
            ClientSideSettings::isMacebotCanDoKbHit,
            v -> ClientSideSettings.setMacebotCanDoKbHit((Boolean) v)
    ),
    MACEBOT_CAN_DO_CRIT_HIT(
            Boolean.class, SettingsCategory.MACEBOT, "Critical Hit", true,
            Settings::isMacebotCanDoCritHit,
            v -> Settings.setMacebotCanDoCritHit((Boolean) v),
            ClientSideSettings::isMacebotCanDoCritHit,
            v -> ClientSideSettings.setMacebotCanDoCritHit((Boolean) v)
    ),
    MACEBOT_CAN_DO_ATTACK(
            Boolean.class, SettingsCategory.MACEBOT, "Can Attack", true,
            Settings::isMacebotCanDoAttack,
            v -> Settings.setMacebotCanDoAttack((Boolean) v),
            ClientSideSettings::isMacebotCanDoAttack,
            v -> ClientSideSettings.setMacebotCanDoAttack((Boolean) v)
    ),
    MACEBOT_CAN_USE_SHIELD(
            Boolean.class, SettingsCategory.MACEBOT, "Can Use Shield", true,
            Settings::isMacebotCanUseShield,
            v -> Settings.setMacebotCanUseShield((Boolean) v),
            ClientSideSettings::isMacebotCanUseShield,
            v -> ClientSideSettings.setMacebotCanUseShield((Boolean) v),
            Flags.EXPERIMENTAL
    ),
    MACEBOT_CAN_DO_TRACKING(
            Boolean.class, SettingsCategory.MACEBOT, "Tracking", true,
            Settings::isMacebotCanDoTracking,
            v -> Settings.setMacebotCanDoTracking((Boolean) v),
            ClientSideSettings::isMacebotCanDoTracking,
            v -> ClientSideSettings.setMacebotCanDoTracking((Boolean) v)
    ),

    // ── Player ────────────────────────────────────────────────────────────────
    OPPONENT_ID(
            Integer.class, SettingsCategory.STATUS, "Opponent Entity ID", false,
            Settings::getOpponentId,
            v -> Settings.setOpponentId((int) v),
            ClientSideSettings::getOpponentId,
            v -> ClientSideSettings.setOpponentId((int) v)
    ),
    PLAYER_AUTO_REFILL_ENABLED(
            Boolean.class, SettingsCategory.PLAYER, "Auto-Refill", true,
            Settings::isPlayerAutoRefillEnabled,
            v -> Settings.setPlayerAutoRefillEnabled((Boolean) v),
            ClientSideSettings::isPlayerAutoRefillEnabled,
            v -> ClientSideSettings.setPlayerAutoRefillEnabled((Boolean) v)
    ),
    PLAYER_BUFFS_ENABLED(
            Boolean.class, SettingsCategory.PLAYER, "Buffs Enabled", true,
            Settings::isPlayerBuffsEnabled,
            v -> Settings.setPlayerBuffsEnabled((Boolean) v),
            ClientSideSettings::isPlayerBuffsEnabled,
            v -> ClientSideSettings.setPlayerBuffsEnabled((Boolean) v)
    ),

    // ── Mod — enum ────────────────────────────────────────────────────────────
    MODE(
            Controller.Mode.class, SettingsCategory.MOD, "Mode", true,
            Settings::getMode,
            v -> Settings.setMode((Controller.Mode) v),
            ClientSideSettings::getMode,
            v -> ClientSideSettings.setMode((Controller.Mode) v)
    ),

    PRACTICE_MODE(
            Controller.PracticeMode.class, SettingsCategory.MOD, "Practice Mode", true,
            Settings::getPracticeMode,
            v -> Settings.setPracticeMode((Controller.PracticeMode) v),
            ClientSideSettings::getPracticeMode,
            v -> ClientSideSettings.setPracticeMode((Controller.PracticeMode) v)
    ),

    // ── Mod — booleans ────────────────────────────────────────────────────────
    CHAT_MESSAGES_ENABLED(
            Boolean.class, SettingsCategory.MOD, "Chat Messages", true,
            Settings::isChatMessagesEnabled,
            v -> Settings.setChatMessagesEnabled((Boolean) v),
            ClientSideSettings::isChatMessagesEnabled,
            v -> ClientSideSettings.setChatMessagesEnabled((Boolean) v)
    ),
    ACTION_BAR_MESSAGES_ENABLED(
            Boolean.class, SettingsCategory.MOD, "Action Bar Messages", true,
            Settings::isActionBarMessagesEnabled,
            v -> Settings.setActionBarMessagesEnabled((Boolean) v),
            ClientSideSettings::isActionBarMessagesEnabled,
            v -> ClientSideSettings.setActionBarMessagesEnabled((Boolean) v)
    ),
    OPS_ONLY(
            Boolean.class, SettingsCategory.MOD, "Redistricted Access", true,
            Settings::isOpsOnly,
            v -> Settings.setOpsOnly((Boolean) v),
            ClientSideSettings::isOpsOnly,
            v -> ClientSideSettings.setOpsOnly((Boolean) v),
            Flags.RESTRICTED
    ),
    MOD_ENABLED(
            Boolean.class, SettingsCategory.MOD, "Mod Active", true,
            Settings::isChatMessagesEnabled,
            v -> Settings.setChatMessagesEnabled((Boolean) v),
            ClientSideSettings::isChatMessagesEnabled,
            v -> ClientSideSettings.setChatMessagesEnabled((Boolean) v),
            Flags.RESTRICTED
    );


    // ── Enum infrastructure ───────────────────────────────────────────────────

    private final Class<?> type;
    private final SettingsCategory category;
    private final String displayName;
    /** False for read-only/status values (e.g. entity IDs) that the UI should show but not let the user edit. */
    private final boolean editable;
    private FlagContainer flags = new FlagContainer();

    private final Supplier<Object> serverGetter;
    private final Consumer<Object> serverSetter;
    private final Supplier<Object> clientGetter;
    private final Consumer<Object> clientSetter;

    SettingsKey(Class<?> type, SettingsCategory category, String displayName, boolean editable,
                Supplier<Object> serverGetter, Consumer<Object> serverSetter,
                Supplier<Object> clientGetter, Consumer<Object> clientSetter) {
        this.type = type;
        this.category = category;
        this.displayName = displayName;
        this.editable = editable;
        this.serverGetter = serverGetter;
        this.serverSetter = serverSetter;
        this.clientGetter = clientGetter;
        this.clientSetter = clientSetter;
    }
    SettingsKey(Class<?> type, SettingsCategory category, String displayName, boolean editable,
                Supplier<Object> serverGetter, Consumer<Object> serverSetter,
                Supplier<Object> clientGetter, Consumer<Object> clientSetter, Flag... flags) {
        this.type = type;
        this.category = category;
        this.displayName = displayName;
        this.editable = editable;
        this.serverGetter = serverGetter;
        this.serverSetter = serverSetter;
        this.clientGetter = clientGetter;
        this.clientSetter = clientSetter;
        this.flags.addFlags(flags);
    }

    // ── Metadata ──────────────────────────────────────────────────────────────

    public Class<?> getType() {
        return type;
    }

    public SettingsCategory getCategory() {
        return category;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isEditable() {
        return editable;
    }

    public boolean isExperimental(){
        return flags.contains(Flags.EXPERIMENTAL);
    }

    public boolean isRestricted(){
        return flags.contains(Flags.RESTRICTED);
    }

    /** True if this key holds a boolean (safe to use in toggle commands/controls). */
    public boolean isBoolean() {
        return type == Boolean.class;
    }

    /** True if this key holds an enum value (Difficulty, Mode, etc.). */
    public boolean isEnum() {
        return type.isEnum();
    }

    /** All keys belonging to a given category, in declaration order - what SettingsBody iterates per tab. */
    public static List<SettingsKey> byCategory(SettingsCategory category) {
        return Arrays.stream(values())
                .filter(k -> k.category == category)
                .collect(Collectors.toList());
    }

    // ── Server ────────────────────────────────────────────────────────────────

    public Object getServerValue() {
        return serverGetter.get();
    }

    public void applyToServer(Object v) {
        serverSetter.accept(v);
    }

    public String getServerValueAsString() {
        return String.valueOf(getServerValue());
    }

    // ── Client ────────────────────────────────────────────────────────────────

    public Object getClientValue() {
        return clientGetter.get();
    }

    public void applyToClient(Object v) {
        clientSetter.accept(v);
    }

    public String getClientValueAsString() {
        return String.valueOf(getClientValue());
    }

    /** For booleans: the *next* value after the current client-side value (used by toggle controls). */
    public Object nextBooleanClientValue() {
        return !((Boolean) getClientValue());
    }

    /** For enums: the *next* value (wrapping) after the current client-side value (used by cycle controls). */
    public Object nextEnumClientValue() {
        Object[] constants = type.getEnumConstants();
        Object current = getClientValue();
        int idx = 0;
        for (int i = 0; i < constants.length; i++) {
            if (constants[i] == current) {
                idx = i;
                break;
            }
        }
        return constants[(idx + 1) % constants.length];
    }

    // ── Shared parsing ────────────────────────────────────────────────────────

    /**
     * Parse a raw string back into the correct typed value for this key.
     * Works for Boolean, Integer, and any enum. Used by both packet handlers.
     */
    @SuppressWarnings("unchecked")
    public Object parse(String raw) {
        if (type == Boolean.class) return Boolean.parseBoolean(raw);
        if (type == Integer.class) return Integer.parseInt(raw);
        if (type.isEnum()) return Enum.valueOf((Class<? extends Enum>) type, raw);
        return raw;
    }
}