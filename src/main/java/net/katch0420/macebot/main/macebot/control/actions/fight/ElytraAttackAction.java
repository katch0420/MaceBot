package net.katch0420.macebot.main.macebot.control.actions.fight;

import net.katch0420.macebot.main.macebot.control.*;
import net.katch0420.macebot.main.macebot.control.actions.BotAction;

public class ElytraAttackAction extends BotAction {

    private int groundCount;

    @Override public boolean canSelect() { return false; } // triggered by ElytraLaunch

    @Override
    public void onStart(ActionManager am, ActionContext ctx) {
        super.onStart(am, ctx);
        groundCount = 0;
    }

    @Override
    public TickResult tick(ActionManager am, ActionContext ctx) {
        if (opponentGone(ctx)) { am.unequipElytra(); return TickResult.fail(); }

        if (am.macebot.isOnGround()) {
            if (++groundCount > 5) { am.unequipElytra(); return TickResult.fail(); }
        }
        // Wait until we're descending
        if (am.macebot.getVelocity().y > 0) return TickResult.pass();

        am.lookAtOpponent();

        if (ActionContext.macebotCtx.isTargetOnCrossHair()) {
            am.unequipElytra();
            return TickResult.successThen(Actions.MACE_HIT);
        }

        return TickResult.pass();
    }
}