package net.katch0420.macebot.main.macebot.control.actions.fight;

import net.katch0420.macebot.main.macebot.bot.BotSlots;
import net.katch0420.macebot.main.macebot.control.*;
import net.katch0420.macebot.main.macebot.control.actions.BotAction;
import net.katch0420.macebot.main.settings.server.Settings;
import net.minecraft.util.math.Direction;

public class RegularLaunchAction extends BotAction {

    @Override
    public double computeBaseWeight(ActionContext ctx, BotAction lastAction) {
        return Settings.isMacebotCanDoMaceAttack() ? 30 : 0;
    }

    @Override
    public TickResult tick(ActionManager am, ActionContext ctx) {
        if (opponentGone(ctx)) return TickResult.fail();

        switch (getStep()) {
            case 1 -> {
                am.setMovementSpeed(1, 0);
                am.setSprint(true);
                am.lookAt(am.opponent.getCameraPosVec(0.5F));
                nextStep();
                return TickResult.pass();
            }
            case 2 -> {
                am.setSelectedSlot(BotSlots.WIND_CHARGE);
                nextStep();
                return TickResult.pass();
            }
            case 3 -> {
                am.look(Direction.DOWN);
                am.use();
                am.delayJumpInMillis((int) Math.max(0, (Math.random() - 0.5) * Settings.getDifficulty().windBoostJumpOffset + 20)
                );
                nextStep();
                return TickResult.pass();
            }
            case 4 -> {
                am.lookAtOpponent();
                // Chain directly into MaceHit — no need for Controller to know the sequence
                return TickResult.successThen(Actions.MACE_HIT);
            }
        }
        return TickResult.pass();
    }

    @Override
    protected double adjustForDifficulty(double base, Difficulty d) {
        return base * switch (d){
            case NOOB -> 0.3;
            case PRO -> 0.7;
            case MASTER -> 1;
        };
    }
}