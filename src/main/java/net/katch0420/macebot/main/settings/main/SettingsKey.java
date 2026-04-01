package net.katch0420.macebot.main.settings.main;

import net.katch0420.macebot.main.macebot.control.Controller;
import net.katch0420.macebot.main.settings.client.ClientSideSettings;
import net.katch0420.macebot.main.settings.server.Settings;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Single source-of-truth for every setting synced between server and clients.
 *
 * Each entry carries:
 *  - the value type
 *  - server getter/setter  → reads/writes Settings
 *  - client getter/setter  → reads/writes ClientSideSettings
 *
 * This means NO hardcoded switches are needed anywhere else — packets, helpers,
 * commands and screens all just call key.getServerValue() / key.applyToClient() etc.
 *
 * ── Categories ────────────────────────────────────────────────────────────────
 *  STATUS   — runtime flags (online/paused). Set by lifecycle code, not commands.
 *  MACEBOT  — bot behaviour toggles + enum settings (Difficulty, Mode)
 *  PLAYER   — player-side settings
 *  GENERAL  — message/mode settings
 */
public enum SettingsKey {

    // ── Status flags (set by lifecycle, broadcast to all clients) ─────────────
    MACEBOT_ONLINE(
            Boolean.class,
            Settings::isMacebotOnline,
            v -> Settings.setMacebotOnline((Boolean) v),
            ClientSideSettings::isMacebotOnline,
            v -> ClientSideSettings.setMacebotOnline((Boolean) v)
    ),
    MACEBOT_PAUSED(
            Boolean.class,
            Settings::isMacebotPaused,
            v -> Settings.setMacebotPaused((Boolean) v),
            ClientSideSettings::isMacebotPaused,
            v -> ClientSideSettings.setMacebotPaused((Boolean) v)
    ),

    // ── MaceBot — enum ────────────────────────────────────────────────────────
    DIFFICULTY(
            Controller.Difficulty.class,
            Settings::getDifficulty,
            v -> Settings.setDifficulty((Controller.Difficulty) v),
            ClientSideSettings::getDifficulty,
            v -> ClientSideSettings.setDifficulty((Controller.Difficulty) v)
    ),

    // ── MaceBot — booleans ────────────────────────────────────────────────────
    MACEBOT_CAN_USE_ELYTRA(
            Boolean.class,
            Settings::isMacebotCanUseElytra,
            v -> Settings.setMacebotCanUseElytra((Boolean) v),
            ClientSideSettings::isMacebotCanUseElytra,
            v -> ClientSideSettings.setMacebotCanUseElytra((Boolean) v)
    ),
    MACEBOT_AUTO_REFILL_ENABLED(
            Boolean.class,
            Settings::isMacebotAutoRefillEnabled,
            v -> Settings.setMacebotAutoRefillEnabled((Boolean) v),
            ClientSideSettings::isMacebotAutoRefillEnabled,
            v -> ClientSideSettings.setMacebotAutoRefillEnabled((Boolean) v)
    ),
    MACEBOT_BUFFS_ENABLED(
            Boolean.class,
            Settings::isMacebotBuffsEnabled,
            v -> Settings.setMacebotBuffsEnabled((Boolean) v),
            ClientSideSettings::isMacebotBuffsEnabled,
            v -> ClientSideSettings.setMacebotBuffsEnabled((Boolean) v)
    ),
    MACEBOT_CAN_DO_MACE_ATTACK(
            Boolean.class,
            Settings::isMacebotCanDoMaceAttack,
            v -> Settings.setMacebotCanDoMaceAttack((Boolean) v),
            ClientSideSettings::isMacebotCanDoMaceAttack,
            v -> ClientSideSettings.setMacebotCanDoMaceAttack((Boolean) v)
    ),
    MACEBOT_CAN_DO_KB_HIT(
            Boolean.class,
            Settings::isMacebotCanDoKbHit,
            v -> Settings.setMacebotCanDoKbHit((Boolean) v),
            ClientSideSettings::isMacebotCanDoKbHit,
            v -> ClientSideSettings.setMacebotCanDoKbHit((Boolean) v)
    ),
    MACEBOT_CAN_DO_CRIT_HIT(
            Boolean.class,
            Settings::isMacebotCanDoCritHit,
            v -> Settings.setMacebotCanDoCritHit((Boolean) v),
            ClientSideSettings::isMacebotCanDoCritHit,
            v -> ClientSideSettings.setMacebotCanDoCritHit((Boolean) v)
    ),
    MACEBOT_CAN_DO_ATTACK(
            Boolean.class,
            Settings::isMacebotCanDoAttack,
            v -> Settings.setMacebotCanDoAttack((Boolean) v),
            ClientSideSettings::isMacebotCanDoAttack,
            v -> ClientSideSettings.setMacebotCanDoAttack((Boolean) v)
    ),
    MACEBOT_CAN_USE_SHIELD(
            Boolean.class,
            Settings::isMacebotCanUseShield,
            v -> Settings.setMacebotCanUseShield((Boolean) v),
            ClientSideSettings::isMacebotCanUseShield,
            v -> ClientSideSettings.setMacebotCanUseShield((Boolean) v)
    ),
    MACEBOT_CAN_DO_TRACKING(
            Boolean.class,
            Settings::isMacebotCanDoTracking,
            v -> Settings.setMacebotCanDoTracking((Boolean) v),
            ClientSideSettings::isMacebotCanDoTracking,
            v -> ClientSideSettings.setMacebotCanDoTracking((Boolean) v)
    ),

    // ── Player ────────────────────────────────────────────────────────────────
    PLAYER_AUTO_REFILL_ENABLED(
            Boolean.class,
            Settings::isPlayerAutoRefillEnabled,
            v -> Settings.setPlayerAutoRefillEnabled((Boolean) v),
            ClientSideSettings::isPlayerAutoRefillEnabled,
            v -> ClientSideSettings.setPlayerAutoRefillEnabled((Boolean) v)
    ),
    PLAYER_BUFFS_ENABLED(
            Boolean.class,
            Settings::isPlayerBuffsEnabled,
            v -> Settings.setPlayerBuffsEnabled((Boolean) v),
            ClientSideSettings::isPlayerBuffsEnabled,
            v -> ClientSideSettings.setPlayerBuffsEnabled((Boolean) v)
    ),

    // ── General — enum ────────────────────────────────────────────────────────
    MODE(
            Controller.Mode.class,
            Settings::getMode,
            v -> Settings.setMode((Controller.Mode) v),
            ClientSideSettings::getMode,
            v -> ClientSideSettings.setMode((Controller.Mode) v)
    ),

    // ── General — booleans ────────────────────────────────────────────────────
    CHAT_MESSAGES_ENABLED(
            Boolean.class,
            Settings::isChatMessagesEnabled,
            v -> Settings.setChatMessagesEnabled((Boolean) v),
            ClientSideSettings::isChatMessagesEnabled,
            v -> ClientSideSettings.setChatMessagesEnabled((Boolean) v)
    ),
    ACTION_BAR_MESSAGES_ENABLED(
            Boolean.class,
            Settings::isActionBarMessagesEnabled,
            v -> Settings.setActionBarMessagesEnabled((Boolean) v),
            ClientSideSettings::isActionBarMessagesEnabled,
            v -> ClientSideSettings.setActionBarMessagesEnabled((Boolean) v)
    );

    // ── Enum infrastructure ───────────────────────────────────────────────────

    private final Class<?>         type;
    private final Supplier<Object> serverGetter;
    private final Consumer<Object> serverSetter;
    private final Supplier<Object> clientGetter;
    private final Consumer<Object> clientSetter;

    SettingsKey(Class<?> type,
                Supplier<Object> serverGetter, Consumer<Object> serverSetter,
                Supplier<Object> clientGetter, Consumer<Object> clientSetter) {
        this.type         = type;
        this.serverGetter = serverGetter;
        this.serverSetter = serverSetter;
        this.clientGetter = clientGetter;
        this.clientSetter = clientSetter;
    }

    // ── Type ──────────────────────────────────────────────────────────────────

    public Class<?> getType() { return type; }

    /** True if this key holds a boolean (safe to use in toggle commands). */
    public boolean isBoolean() { return type == Boolean.class; }

    /** True if this key holds an enum value (Difficulty, Mode, etc.). */
    public boolean isEnum() { return type.isEnum(); }

    // ── Server ────────────────────────────────────────────────────────────────

    public Object getServerValue()          { return serverGetter.get(); }
    public void   applyToServer(Object v)   { serverSetter.accept(v);   }
    public String getServerValueAsString()  { return String.valueOf(getServerValue()); }

    // ── Client ────────────────────────────────────────────────────────────────

    public Object getClientValue()          { return clientGetter.get(); }
    public void   applyToClient(Object v)   { clientSetter.accept(v);   }
    public String getClientValueAsString()  { return String.valueOf(getClientValue()); }

    // ── Shared parsing ────────────────────────────────────────────────────────

    /**
     * Parse a raw string back into the correct typed value for this key.
     * Works for Boolean, Integer, and any enum. Used by both packet handlers.
     */
    @SuppressWarnings("unchecked")
    public Object parse(String raw) {
        if (type == Boolean.class) return Boolean.parseBoolean(raw);
        if (type == Integer.class) return Integer.parseInt(raw);
        if (type.isEnum())         return Enum.valueOf((Class<? extends Enum>) type, raw);
        return raw;
    }
}