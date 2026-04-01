package net.katch0420.macebot.main.macebot.control.actions;

import net.katch0420.macebot.main.macebot.bot.BotSlots;
import net.katch0420.macebot.main.macebot.control.*;

public class BlockAction extends BotAction {

    @Override public boolean canSelect() { return false; } // triggered by ShieldChecker

    @Override
    public double getWeight(ActionContext ctx, BotAction lastAction) { return 0; }

    @Override
    public TickResult tick(ActionManager am, ActionContext ctx) {
        // Keep looking at opponent while blocking
        if (ActionContext.opponentCtx.isAvailable()) {
            am.lookAt(am.opponent.getCameraPosVec(0.5f));
        }
        am.setSelectedSlot(BotSlots.SHIELD);
        am.use();
        return TickResult.pass();
    }

    @Override
    public void onEnd(ActionManager am, ActionContext ctx) {
        am.stopBlocking();
    }
}