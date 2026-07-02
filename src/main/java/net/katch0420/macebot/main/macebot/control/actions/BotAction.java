package net.katch0420.macebot.main.macebot.control.actions;

import net.katch0420.macebot.main.macebot.control.ActionContext;
import net.katch0420.macebot.main.macebot.control.ActionManager;
import net.katch0420.macebot.main.macebot.control.Difficulty;
import net.katch0420.macebot.main.macebot.control.TickResult;

public abstract class BotAction {

    private int step = 1;

    public boolean canSelect() { return true; }

    public double getWeight(ActionContext ctx, BotAction last) {
        double base = computeBaseWeight(ctx, last);
        if (base == 0) return 0; // hard-disabled actions skip difficulty math
        return adjustForDifficulty(base, ctx.getDifficulty());
    }

    // Actions define their full context logic here — untouched
    protected double computeBaseWeight(ActionContext ctx, BotAction last){
        return 0;
    };

    // Actions override this to shape their own weight per difficulty
    // Default: no change — actions that don't care skip it
    protected double adjustForDifficulty(double base, Difficulty d) {
        return base;
    }
    public void onStart(ActionManager am, ActionContext ctx) {
        step = 1;
    }

    public abstract TickResult tick(ActionManager am, ActionContext ctx);

    /** Called when the action ends for any reason (info or fail). */
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