package net.katch0420.macebot.main.macebot.control.actions.fight;

import net.katch0420.macebot.main.macebot.bot.BotSlots;
import net.katch0420.macebot.main.macebot.control.*;
import net.katch0420.macebot.main.macebot.control.actions.BotAction;
import net.katch0420.macebot.main.settings.server.Settings;

public class CritHitAction extends BotAction {

    @Override
    public double computeBaseWeight(ActionContext ctx, BotAction lastAction) {
        if (!Settings.isMacebotCanDoCritHit()) return 0;
        double w = 20;
        if (ActionContext.macebotCtx.getDistance(ActionContext.opponentCtx.getPlayer()) < 5) w += 20;
        if (lastAction == Actions.KNOCKBACK) w -= 10;
        if (lastAction == Actions.CRIT)      w += 20;
        return w;
    }

    @Override
    public TickResult tick(ActionManager am, ActionContext ctx) {
        if (opponentGone(ctx)) return TickResult.fail();
        am.lookAtOpponent();

        switch (getStep()) {
            case 1 -> {
                am.setSelectedSlot(BotSlots.SWORD);
                am.setMovementSpeed(1, 0);
                am.setSprint(true);
                nextStep();
                return TickResult.pass();
            }
            case 2 -> {
                if (ActionContext.macebotCtx.getDistance(ActionContext.opponentCtx.getPlayer()) < 6) {
                    am.doJump();
                    nextStep();
                    return TickResult.pass();
                }
            }
            default -> {
                // Landed back on ground before hitting — abort
                if (am.macebot.isOnGround()) return TickResult.fail();

                if (am.macebot.fallDistance > 0
                        && ActionContext.macebotCtx.getAttackCooldownProgress() > 0.9) {
                    am.setSprint(false);
                    if(!am.attackMarked){
                        if(ActionContext.macebotCtx.isTargetAvailable()) am.attackMarked = true;
                    }
                    if(am.attackMarked) {
                        am.attack();
                        am.setMovementSpeed(0, 0);
                        return TickResult.success();
                    }
                    am.setMovementSpeed(1, 0);
                    am.setSprint(true);
                }
            }
        }
        return TickResult.pass();
    }

    @Override
    protected double adjustForDifficulty(double base, Difficulty d) {
        return base * switch (d){
            case NOOB -> 0.2;
            case PRO -> 0.6;
            case MASTER -> 1;
        };
    }
}