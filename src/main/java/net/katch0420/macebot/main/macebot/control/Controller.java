package net.katch0420.macebot.main.macebot.control;

import net.katch0420.macebot.main.MaceBot;
import net.katch0420.macebot.main.macebot.bot.BotSlots;
import net.katch0420.macebot.main.macebot.control.actions.BotAction;
import net.katch0420.macebot.main.settings.server.Settings;
import net.katch0420.macebot.main.utils.PlayerBuffs;
import net.minecraft.server.network.ServerPlayerEntity;

import static net.katch0420.macebot.main.macebot.control.ActionContext.macebotCtx;
import static net.katch0420.macebot.main.macebot.control.ActionContext.opponentCtx;

/**
 * Main tick loop — orchestrates context, selection, and execution.
 *
 * Controller no longer knows anything about individual actions.
 * It just holds the current BotAction and calls tick() on it.
 * Actions chain to each other via TickResult.successThen().
 */
public class Controller {

    public final ActionManager  am;
    public final ActionSelector selector;
    public final ActionContext  ctx;
    public final ShieldChecker  shieldChecker;

    public BotAction currentAction = Actions.NONE;
    public static Mode mode = Mode.NPC;

    public Controller() {
        this.ctx           = new ActionContext(this);
        this.am            = new ActionManager();
        this.selector      = new ActionSelector();
        this.shieldChecker = new ShieldChecker();
    }

    // ── Main tick ─────────────────────────────────────────────────────────────

    public void tick() {
        if (!ctx.update()) {
            return;
        }

        am.update();

        if (!macebotCtx.isAvailable()) return;
        if(Settings.isMacebotBuffsEnabled() && MaceBot.server.getTicks() % 20 == 0) PlayerBuffs.applyBuffsToPlayer(macebotCtx.serverPlayer);
        switch (mode) {
            case NPC -> {
                if (currentAction != Actions.NONE) clearAction();
                if (opponentCtx.isAvailable() && Settings.isMacebotCanDoTracking())
                    am.lookAtOpponent();
            }
            case FIGHT -> tickFight();
            case PRACTICE -> {}
        }
    }

    private void tickFight() {
        // ── 1. Shield check always runs first ─────────────────────────────────
        BotAction shieldDecision = shieldChecker.check(currentAction, am);

        if (shieldDecision == Actions.BLOCK && currentAction != Actions.BLOCK) {
            // Start blocking
            setAction(Actions.BLOCK);
        } else if (shieldDecision != Actions.BLOCK && currentAction == Actions.BLOCK) {
            // Stop blocking
            clearAction();
        }

        // ── 2. Non-shield actions ─────────────────────────────────────────────
        if (currentAction != Actions.BLOCK) {
            maybeEat();

            if (currentAction == Actions.NONE) {
                setAction(selector.select(ctx));
            }
        }

        // ── 3. Execute current action ─────────────────────────────────────────
        if (currentAction == Actions.NONE) return;

        TickResult result = currentAction.tick(am, ctx);

        switch (result.status) {
            case SUCCESS -> {
                currentAction.onEnd(am, ctx);
                // Chain to next action if the action requested it
                if (result.nextAction != null) {
                    selector.setLastAction(currentAction);
                    setAction(result.nextAction);
                } else {
                    currentAction = Actions.NONE;
                }
            }
            case FAIL -> clearAction();
            case PASS -> {} // keep running
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void setAction(BotAction action) {
        currentAction = action;
        currentAction.onStart(am, ctx);
    }

    private void clearAction() {
        currentAction.onEnd(am, ctx);
        currentAction = Actions.NONE;
        am.resetAllMovements();
    }

    /**
     * Interrupt current action to eat if health is low.
     * Does NOT interrupt blocking — shield priority is handled above.
     */
    private void maybeEat() {
        if (currentAction == Actions.EAT) return;

        boolean healthLow = macebotCtx.getHealth() < 10
                || (macebotCtx.getHealth() < 15
                && opponentCtx.isAvailable()
                && opponentCtx.getHealth() >= macebotCtx.getHealth());

        boolean foodLow = macebotCtx.getFoodLevel() < 4
                || macebotCtx.getFoodLevel() < 7;

        if (healthLow || foodLow) {
            if (currentAction != Actions.NONE) clearAction();
            setAction(Actions.EAT);
        }
    }

    // ── Pause / resume ────────────────────────────────────────────────────────

    public void pauseTheBot(boolean pause) {
        if (pause) {
            pauseTheBot();
            Settings.setMacebotPaused(true);
        } else {
            mode = Mode.FIGHT;
            Settings.setMacebotPaused(false);
        }
    }

    public void pauseTheBot() {
        mode = Mode.NPC;
        if (currentAction != Actions.NONE) clearAction();
    }

    // ── Enums ─────────────────────────────────────────────────────────────────

    public enum Difficulty { NOOB, PRO, EXPERT, MASTER }

    public enum Mode { NPC, FIGHT, PRACTICE }
}