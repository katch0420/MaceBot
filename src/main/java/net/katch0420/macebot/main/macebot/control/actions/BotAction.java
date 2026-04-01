package net.katch0420.macebot.main.macebot.control.actions;

import net.katch0420.macebot.main.macebot.control.ActionContext;
import net.katch0420.macebot.main.macebot.control.ActionManager;
import net.katch0420.macebot.main.macebot.control.TickResult;

/**
 * Base class for every bot action — mirrors how Minecraft structures Item.
 *
 * Each subclass owns:
 *  - its probability weight + conditions (getWeight / canSelect)
 *  - its full execution logic (tick)
 *  - its step counter so Controller doesn't track that
 *
 * Actions are registered as singletons in Actions.java.
 * Step state is on the instance, so only one action runs at a time.
 */
public abstract class BotAction {

    private int step = 1;

    // ── Selection ─────────────────────────────────────────────────────────────

    /**
     * Whether this action can appear in the random selection pool.
     * Override to return false for EAT, BLOCK, MACE_HIT, ELYTRA_ATTACK —
     * those are triggered by game logic, not random selection.
     */
    public boolean canSelect() { return true; }

    /**
     * Weight for random selection. 0 or negative = won't be selected.
     * Conditions (Settings flags, distance checks, etc.) go here.
     *
     * @param ctx        live game context
     * @param lastAction the action that ran just before this selection
     */
    public abstract double getWeight(ActionContext ctx, BotAction lastAction);

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    /** Called once when the controller picks this action. Reset state here. */
    public void onStart(ActionManager am, ActionContext ctx) {
        step = 1;
    }

    /**
     * Called every server tick while this action is current.
     * Return TickResult.pass() to keep running, .success() / .fail() to stop.
     */
    public abstract TickResult tick(ActionManager am, ActionContext ctx);

    /** Called when the action ends for any reason (success or fail). */
    public void onEnd(ActionManager am, ActionContext ctx) {}

    // ── Step helpers ──────────────────────────────────────────────────────────

    protected int  getStep()  { return step;  }
    protected void nextStep() { step++;       }
    protected void resetStep(){ step = 1;     }

    // ── Shared guard ─────────────────────────────────────────────────────────

    /** Common early-exit: opponent disappeared mid-action. */
    protected boolean opponentGone(ActionContext ctx) {
        return !ActionContext.opponentCtx.isAvailable();
    }
}