package net.katch0420.macebot.main.macebot.ai.action.melee;

import net.katch0420.macebot.main.macebot.ai.Core;
import net.katch0420.macebot.main.macebot.ai.Result;
import net.katch0420.macebot.main.macebot.ai.action.Action;
import net.katch0420.macebot.main.macebot.bot.BotSlots;

import java.util.List;

public class CritHit extends Action<CritHit> {
    public CritHit(Core core) { super(core);}

    @Override
    protected List<Integer> usableSlots() {
        return List.of(BotSlots.SWORD, BotSlots.AXE, BotSlots.BREACH_MACE, BotSlots.DENSITY_MACE);
    }

    float minimumDis = Float.MAX_VALUE;

    @Override
    public Result preparation() {
        lla.lookAtOpponent();
        if(minimumDis < analysisData.lastProcessedDistance && analysisData.lastProcessedDistance - minimumDis > 3) return f();
        if(input.movementForward == 0) lla.inputMovement(1,0);
        if(adjustSlot()) return t();
        if(bot.onGround()){
            lla.jump();
        }
        if(analysisData.aboutToReach(0)){
            if((bot.fallDistance > 0 || (!bot.onGround() && bot.fallDistance == 0)) && analysisData.isOpponentOnCrosshair()){
                if(!analysisData.itemLoaded()) return t();
                return s();
            }
        } else if(analysisData.aboutToReach(3) && step != 2){
            step = 2;
            lla.inputMovement(1, lla.strafe());
        } else {
            step = 1;
            lla.inputMovement(1, 0);
        }
        minimumDis = Math.min(analysisData.lastProcessedDistance, minimumDis);
        return t();
    }

    @Override
    public Result execution() {
        if(readyForAct()){
            lla.lookAtOpponent();
            lla.attack();
            return s();
        }
        return f();
    }

    @Override
    public boolean canPrepare() {
        return true;
    }

    @Override
    protected void clearCache() {
        minimumDis = Float.MAX_VALUE;
    }

    @Override
    public boolean readyForAct() {
        return bot.fallDistance > 0 && analysisData.isOpponentOnCrosshair() && analysisData.itemLoaded();
    }

    @Override
    public int requiredSkill() {
        return 40;
    }
}
