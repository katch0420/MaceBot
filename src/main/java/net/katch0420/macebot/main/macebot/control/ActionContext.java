package net.katch0420.macebot.main.macebot.control;

import net.katch0420.macebot.main.macebot.bot.BotPlayer;
import net.katch0420.macebot.main.settings.server.Settings;

/**
 * Holds the two BotPlayer contexts (bot and opponent) and updates them each tick.
 *
 * No hardcoded player names, isMaceBot booleans, or static references here.
 * PlayerResolver implementations decide how each player is found.
 *
 * Access from anywhere via the public fields — same API as before.
 */
public class ActionContext {

    public final Controller controller;

    // These are still public so existing action code can use macebotCtx / opponentCtx
    // but they're no longer static — Controller owns one ActionContext instance.
    public final BotPlayer bot;      // the MaceBot
    public final BotPlayer opponent; // the human player being fought

    // Legacy-friendly aliases (so old action code still compiles with minimal changes)
    public static BotPlayer macebotCtx;
    public static BotPlayer opponentCtx;

    public ActionContext(Controller controller) {
        this.controller = controller;
        // Bot always resolves to PlayerBot.playerBot
        bot = new BotPlayer(PlayerResolver.bot(), 1);

        // Opponent re-resolves every 100 ticks or when lost
        opponent = new BotPlayer(PlayerResolver.nearest(), 100);

        // Cross-link so each can reference the other
        bot.opponent      = opponent;
        opponent.opponent = bot;

        // Pass back-reference so resolvers can query context
        bot.init(this);
        opponent.init(this);

        // Legacy aliases point to the same objects
        macebotCtx  = bot;
        opponentCtx = opponent;
    }

    /**
     * Update both players. Returns true only if the bot is available.
     * Opponent being unavailable is normal — bot will wait.
     */
    public boolean update() {
        boolean botOk = bot.update();
        opponent.update(); // update regardless — may find a new target
        return botOk;
    }

    public Difficulty getDifficulty() {
        return Settings.getDifficulty();
    }
}