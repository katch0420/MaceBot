package net.katch0420.macebot.main.macebot.bot;

/**
 * All configurable constants for the bot in one place.
 * Change values here — nothing else needs editing.
 */
public final class BotConfig {

    /** In-game username the bot will use. */
    public static final String BOT_NAME = "MaceBot";

    /** Loopback address used for the fake network connection. */
    public static final String BOT_IP = "127.0.0.1";

    /** Starting health on spawn. */
    public static final float SPAWN_HEALTH = 20.0F;

    /** Step height — 0.6 lets the bot walk up slabs like a player. */
    public static final float STEP_HEIGHT = 0.6F;

    /** How often (ticks) the bot resets its position for anti-desync. */
    public static final int POSITION_RESET_INTERVAL = 10;

    /** Kit given to the bot on spawn. */
    public static final String DEFAULT_KIT = "netherite_mace";

    // ── Travel multipliers ────────────────────────────────────────────────────

    public static final float SNEAK_MULTIPLIER   = 0.3F;
    public static final float SPRINT_FORWARD     = 1.3F;
    public static final float SPRINT_SIDEWAYS    = 9.8F; // intentionally high for strafing
    public static final float EAT_MULTIPLIER     = 0.2F;
    public static final float SHIELD_MULTIPLIER  = 0.3F;
    public static final float SPYGLASS_MULTIPLIER= 0.1F;

    private BotConfig() {}
}