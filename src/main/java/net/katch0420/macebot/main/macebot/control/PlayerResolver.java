package net.katch0420.macebot.main.macebot.control;

import net.katch0420.macebot.main.macebot.bot.BotPlayer;
import net.katch0420.macebot.main.macebot.bot.PlayerBot;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Resolves which ServerPlayerEntity a BotPlayer context should track.
 *
 * Replaces the hardcoded `isMaceBot` boolean in the old Player class.
 * Two built-in implementations:
 *   - BotResolver     → always returns PlayerBot.playerBot
 *   - NearestResolver → finds the nearest player to the bot each update cycle
 *
 * To add a new resolution strategy (e.g. target by name, by team),
 * implement this interface and pass it to BotPlayer's constructor.
 */
@FunctionalInterface
public interface PlayerResolver {

    /**
     * @param ctx the full ActionContext (for cross-referencing bot position, world, etc.)
     * @return the resolved player, or null if unavailable
     */
    ServerPlayerEntity resolve(ActionContext ctx);

    // ── Built-in resolvers ────────────────────────────────────────────────────

    /** Resolves to the bot entity itself. */
    static PlayerResolver bot() {
        return ctx -> PlayerBot.playerBot;
    }

    /**
     * Resolves to the nearest ServerPlayerEntity to the bot.
     * Re-resolves every 100 ticks or when current target is gone.
     */
    static PlayerResolver nearest() {
        return ctx -> {
            BotPlayer bot = ctx.bot;
            if (!bot.isAvailable()) return null;

            return bot.serverPlayer
                    //? if >=1.21.9
                    /*.getEntityWorld()*/
                    //? if >=1.21.6 <=1.21.8
                    /*.getWorld()*/
                    //? if <=1.21.5
                    .getServerWorld()
                    .getPlayers().stream()
                    .filter(p -> p != bot.serverPlayer)
                    .min((a, b) -> Float.compare(
                            bot.serverPlayer.distanceTo(a),
                            bot.serverPlayer.distanceTo(b)))
                    .orElse(null);
        };
    }
}