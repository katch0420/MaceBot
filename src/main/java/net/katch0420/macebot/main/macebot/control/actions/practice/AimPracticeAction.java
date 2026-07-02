package net.katch0420.macebot.main.macebot.control.actions.practice;

import net.katch0420.macebot.main.macebot.bot.PlayerBot;
import net.katch0420.macebot.main.macebot.control.ActionContext;
import net.katch0420.macebot.main.macebot.control.ActionManager;
import net.katch0420.macebot.main.macebot.control.TickResult;
import net.katch0420.macebot.main.macebot.control.actions.BotAction;
import net.katch0420.macebot.main.utils.TeleportHelper;

public class AimPracticeAction extends BotAction
{
    @Override
    public double getWeight(ActionContext ctx, BotAction lastAction) {
        return 0;
    }

    @Override
    public void onStart(ActionManager am, ActionContext ctx) {
        super.onStart(am, ctx);
        ((PlayerBot) am.macebot).freeze();
        TeleportHelper.teleportAboveGround(4, am.macebot);
        am.macebot.startFallFlying();
    }

    @Override
    public void onEnd(ActionManager am, ActionContext ctx) {
        ((PlayerBot) am.macebot).unfreeze();
    }

    @Override
    public TickResult tick(ActionManager am, ActionContext ctx) {
        if (opponentGone(ctx)) return TickResult.fail();
        return TickResult.pass();
    }
}
