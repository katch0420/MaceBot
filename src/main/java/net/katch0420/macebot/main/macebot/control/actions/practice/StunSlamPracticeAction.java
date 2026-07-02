package net.katch0420.macebot.main.macebot.control.actions.practice;

import net.katch0420.macebot.main.macebot.bot.BotSlots;
import net.katch0420.macebot.main.macebot.bot.PlayerBot;
import net.katch0420.macebot.main.macebot.control.ActionContext;
import net.katch0420.macebot.main.macebot.control.ActionManager;
import net.katch0420.macebot.main.macebot.control.TickResult;
import net.katch0420.macebot.main.macebot.control.actions.BotAction;
import net.minecraft.item.Items;

public class StunSlamPracticeAction extends BotAction {
    @Override
    public double getWeight(ActionContext ctx, BotAction lastAction) {
        return 0;
    }

    @Override
    public void onStart(ActionManager am, ActionContext ctx) {
        super.onStart(am, ctx);
        PlayerBot.controller.attributeDirty = true;
    }

    @Override
    public TickResult tick(ActionManager am, ActionContext ctx) {
        if (opponentGone(ctx)) return TickResult.fail();
        am.lookAtOpponent();

        if(am.macebot.getItemCooldownManager().isCoolingDown(Items.SHIELD)){
            if(getStep() % 40 == 0) am.macebot.getItemCooldownManager().remove(Items.SHIELD);
            nextStep();
        }

        am.setSelectedSlot(BotSlots.SHIELD);
        am.use();
        return TickResult.pass();
    }


}
