package net.katch0420.macebot.main.macebot.control;

import net.katch0420.macebot.main.macebot.control.actions.BotAction;

import java.util.List;

/**
 * Picks the next action by weighted random selection.
 *
 * Each BotAction provides its own weight via getWeight() — no hardcoded
 * probability tables here. To tune an action's likelihood, edit that action.
 */
public class ActionSelector {

    private BotAction lastAction = Actions.NONE;

    /**
     * Select the next action to run.
     * Actions with weight ≤ 0 are skipped automatically.
     */
    public BotAction select(ActionContext ctx) {
        List<BotAction> pool = Actions.getSelectable();

        // Sum total weight
        double total = 0;
        for (BotAction action : pool) {
            total += Math.max(0, action.getWeight(ctx, lastAction));
        }

        if (total <= 0) return Actions.NONE;

        // Weighted pick
        double roll       = Math.random() * total;
        double cumulative = 0;

        for (BotAction action : pool) {
            double w = Math.max(0, action.getWeight(ctx, lastAction));
            if (w <= 0) continue;
            cumulative += w;
            if (roll <= cumulative) {
                lastAction = action;
                return action;
            }
        }

        return Actions.NONE;
    }

    public BotAction getLastAction() { return lastAction; }

    public void setLastAction(BotAction lastAction) { this.lastAction = lastAction; }
}