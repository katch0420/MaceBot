package net.katch0420.macebot.main.macebot.control.actions.practice;

import net.katch0420.macebot.main.macebot.bot.BotSlots;
import net.katch0420.macebot.main.macebot.bot.PlayerBot;
import net.katch0420.macebot.main.macebot.control.ActionContext;
import net.katch0420.macebot.main.macebot.control.ActionManager;
import net.katch0420.macebot.main.macebot.control.TickResult;
import net.katch0420.macebot.main.macebot.control.actions.BotAction;
import net.katch0420.macebot.main.utils.TeleportHelper;
import net.minecraft.util.math.Direction;

public class GrapplingPracticeAction extends BotAction
{
    @Override
    public double getWeight(ActionContext ctx, BotAction lastAction) {
        return 0;
    }

    @Override
    public void onStart(ActionManager am, ActionContext ctx) {
        super.onStart(am, ctx);
        am.setSelectedSlot(BotSlots.WIND_CHARGE);
        am.look(Direction.DOWN);
    }

    @Override
    public TickResult tick(ActionManager am, ActionContext ctx) {
        if (opponentGone(ctx)) return TickResult.fail();
        if(am.macebot.isOnGround()){
            am.use();
            am.delayJumpInMillis(10 + (int) (10 * Math.random()));
        }
        return TickResult.pass();
    }
}
