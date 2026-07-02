package net.katch0420.macebot.main.macebot.control.actions.fight;

import net.katch0420.macebot.main.macebot.bot.BotSlots;
import net.katch0420.macebot.main.macebot.control.*;
import net.katch0420.macebot.main.macebot.control.actions.BotAction;

public class MaceHitAction extends BotAction {
    int cooldown;

    @Override public boolean canSelect() { return false; } // only triggered by launch actions

    @Override
    public TickResult tick(ActionManager am, ActionContext ctx) {
        if (opponentGone(ctx) || am.macebot.isOnGround()) return TickResult.fail();

        am.lookAtOpponent();

        if (getStep() == 1) {
            am.setSelectedSlot(BotSlots.DENSITY_MACE);
            nextStep();
        }

        if (am.macebot.fallDistance > 1.5) {
            if(!am.attackMarked){
                if(ActionContext.macebotCtx.isTargetAvailable()) am.attackMarked = true;
            }
            if(am.attackMarked) {
                if(am.canHit()) am.attack();
            }
        }

        return TickResult.pass();
    }
}