package net.katch0420.macebot.main.macebot.control;

import net.katch0420.macebot.main.macebot.control.actions.BotAction;

/**
 * Result of a BotAction tick.
 *
 * Replaces the old ActionManager.Status enum and also carries an optional
 * "next action" so actions can chain (e.g. RegularLaunch → MaceHit)
 * without the Controller needing to know the sequence.
 */
public final class TickResult {

    public enum Status { PASS, SUCCESS, FAIL }

    public final Status   status;
    public final BotAction nextAction; // null = clear to NONE on info

    private TickResult(Status status, BotAction nextAction) {
        this.status     = status;
        this.nextAction = nextAction;
    }

    // ── Factories ─────────────────────────────────────────────────────────────

    /** Still running — controller keeps calling tick() next tick. */
    public static TickResult pass() { return new TickResult(Status.PASS, null); }

    /** Finished successfully — controller will move to Actions.NONE. */
    public static TickResult success() { return new TickResult(Status.SUCCESS, null); }

    /**
     * Finished successfully and wants to immediately start another action.
     * e.g. RegularLaunch returns successThen(Actions.MACE_HIT)
     */
    public static TickResult successThen(BotAction next) {
        return new TickResult(Status.SUCCESS, next);
    }

    /** Failed — controller resets to NONE and calls resetAllMovements(). */
    public static TickResult fail() { return new TickResult(Status.FAIL, null); }
}