package net.katch0420.macebot.main.macebot.control.actions;

import net.katch0420.macebot.main.macebot.bot.BotSlots;
import net.katch0420.macebot.main.macebot.control.*;

public class EatAction extends BotAction {

    private int tickTimer;

    @Override public boolean canSelect() { return false; } // triggered by Controller health check

    @Override
    public double getWeight(ActionContext ctx, BotAction lastAction) { return 0; }

    @Override
    public void onStart(ActionManager am, ActionContext ctx) {
        super.onStart(am, ctx);
        tickTimer = 0;
    }

    @Override
    public TickResult tick(ActionManager am, ActionContext ctx) {
        switch (getStep()) {
            case 1 -> {
                am.unequipElytra();
                am.setSelectedSlot(BotSlots.FOOD);
                if (!am.use()) return TickResult.fail();
                tickTimer = ctx.macebotCtx.serverPlayer.getItemUseTime();
                nextStep();
                return TickResult.pass();
            }
            case 2 -> {
                if (tickTimer > 0) tickTimer--;
                if (tickTimer <= 0 && !ctx.macebotCtx.serverPlayer.isUsingItem()) {
                    return TickResult.success();
                }
            }
        }
        return TickResult.pass();
    }
}