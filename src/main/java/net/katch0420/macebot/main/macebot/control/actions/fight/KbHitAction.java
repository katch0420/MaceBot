package net.katch0420.macebot.main.macebot.control.actions.fight;

import net.katch0420.macebot.main.macebot.bot.BotSlots;
import net.katch0420.macebot.main.macebot.control.*;
import net.katch0420.macebot.main.macebot.control.actions.BotAction;
import net.katch0420.macebot.main.settings.server.Settings;

public class KbHitAction extends BotAction {

    @Override
    public double computeBaseWeight(ActionContext ctx, BotAction lastAction) {
        if (!Settings.isMacebotCanDoKbHit()) return 0;
        double w = 20;
        if (ActionContext.macebotCtx.getDistance(ActionContext.opponentCtx.getPlayer()) < 6) w += 20;
        if (lastAction == Actions.CRIT)      w -= 10;
        if (lastAction == Actions.KNOCKBACK) w += 20;
        return w;
    }

    @Override
    public TickResult tick(ActionManager am, ActionContext ctx) {
        if (opponentGone(ctx)) return TickResult.fail();
        am.lookAtOpponent();

        // Step 1: select slot. Steps 2-5: brief delay. Step 6+: attack loop.
        switch (getStep()) {
            case 1 -> { am.setSelectedSlot(BotSlots.SWORD); nextStep(); return TickResult.pass(); }
            case 2 -> {am.setMovementSpeed(1, 0); am.setSprint(true); nextStep(); return TickResult.pass();}
            case 3 -> nextStep();
            default -> {
                if (am.macebot.getAttackCooldownProgress(0.5F) > 0.9) {
                    if(!am.attackMarked){
                        if(ActionContext.macebotCtx.isTargetAvailable()) am.attackMarked = true;
                    }
                    if(am.attackMarked){
                        am.attack();
                        am.setSprint(false);
                        am.setMovementSpeed(0, 0);
                        return TickResult.success();
                    }
                }
            }
        }
        return TickResult.pass();
    }

    @Override
    protected double adjustForDifficulty(double base, Difficulty d) {
        return base * switch (d){
            case NOOB -> 0.5;
            case PRO -> 0.8;
            case MASTER -> 1;
        };
    }
}