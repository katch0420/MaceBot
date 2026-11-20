package net.katch0420.macebot.main.macebot.ai.action.movement;

import net.katch0420.macebot.main.macebot.ai.Core;
import net.katch0420.macebot.main.macebot.ai.Result;
import net.katch0420.macebot.main.macebot.ai.action.Action;
import net.katch0420.macebot.main.macebot.bot.BotSlots;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Items;

import java.util.List;

public class WindChargeLaunch extends Action<WindChargeLaunch> {
    public WindChargeLaunch(Core core) {
        super(core);
    }

    @Override
    public boolean canPrepare() {
        return true;
    }

    @Override
    protected List<Integer> usableSlots() {
        return List.of(BotSlots.WIND_CHARGE);
    }

    @Override
    public Result preparation() {
        if(readyForAct()) return s();
        if(adjustSlot()) return t();
        if(!bot.onGround()){
            if(bot.getDeltaMovement().y > 0) return f();
        }
        return t();
    }

    @Override
    public Result execution() {
        if(readyForAct()){
            if(step == 1){
                lla.look(Direction.DOWN);
                return stepUp();
            } else if(step == 2){
                lla.use();
                return md(core.maceHit.getFreshAction().canCombo());
            }
        }
        return f();
    }

    @Override
    public boolean readyForAct() {
        return bot.onGround();
    }

    @Override
    public int requiredSkill() {
        return 25;
    }
}
