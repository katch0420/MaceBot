package net.katch0420.macebot.main.macebot.control.actions;

import net.katch0420.macebot.main.macebot.bot.BotSlots;
import net.katch0420.macebot.main.macebot.control.*;
import net.katch0420.macebot.main.utils.RayTracer;
import net.katch0420.macebot.main.settings.server.Settings;
import net.minecraft.util.math.Direction;

public class ElytraLaunchAction extends BotAction {

    @Override
    public double getWeight(ActionContext ctx, BotAction lastAction) {
        return Settings.isMacebotCanUseElytra() ? 30 : 0;
    }

    @Override
    public TickResult tick(ActionManager am, ActionContext ctx) {
        if (opponentGone(ctx)) { am.unequipElytra(); return TickResult.fail(); }

        switch (getStep()) {
            case 1 -> {
                am.setSelectedSlot(BotSlots.ELYTRA);
                am.setMovementSpeed(1, 0);
                am.setSprint(true);
                nextStep();
                return TickResult.pass();
            }
            case 2 -> {
                am.equipElytra();
                am.doJump();
                am.look(am.macebot.getYaw(), 40F);
                nextStep();
                return TickResult.pass();
            }
            case 3 -> {
                if (!am.macebot.isOnGround()
                        && Math.abs(am.macebot.getVelocity().y) < 0.05) {
                    am.macebot.startGliding();
                    am.setSelectedSlot(BotSlots.WIND_CHARGE);
                    nextStep();
                    return TickResult.pass();
                }
                if (am.macebot.isOnGround()) {
                    am.unequipElytra();
                    return TickResult.fail();
                }
            }
            case 4 -> {
                if (RayTracer.getDistanceToGround(am.macebot) < 0.25) {
                    am.look(Direction.DOWN);
                    am.use();
                    am.delayJumpInMillis(20);
                    nextStep();
                    return TickResult.pass();
                }
            }
            case 5 -> {
                am.look(am.macebot.getYaw(), -30F);
                am.doJump();
                nextStep();
                return TickResult.pass();
            }
            case 6 -> {
                am.macebot.startGliding();
                am.setSelectedSlot(BotSlots.ELYTRA);
                return TickResult.successThen(Actions.ELYTRA_ATTACK);
            }
        }
        return TickResult.pass();
    }
}