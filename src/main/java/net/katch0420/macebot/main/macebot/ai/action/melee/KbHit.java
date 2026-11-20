package net.katch0420.macebot.main.macebot.ai.action.melee;

import net.katch0420.macebot.main.macebot.ai.Core;
import net.katch0420.macebot.main.macebot.ai.Result;
import net.katch0420.macebot.main.macebot.ai.action.Action;
import net.katch0420.macebot.main.macebot.bot.BotSlots;

import java.util.List;

public class KbHit extends Action<KbHit> {
    public KbHit(Core core) { super(core);}

    @Override
    protected List<Integer> usableSlots() {
        return List.of(BotSlots.SWORD, BotSlots.AXE);
    }

    float minimumDis = Float.MAX_VALUE;
    int wtapDelay = 0;
    @Override
    public Result preparation() {
        lla.lookAtOpponent();
        if(wtapDelay > 0){
            wtapDelay--;
            return t();
        }
        if(minimumDis < analysisData.lastProcessedDistance && analysisData.lastProcessedDistance - minimumDis > 4) return f();
        if(adjustSlot()) return t();
        if(!bot.isSprinting()){
            if(input.hasForwardMovement()) {
                lla.inputMovement(0, 0);
                wtapDelay = 20;
                return t();
            }
            else lla.inputMovement(1, lla.strafe());
        }
        if(analysisData.isOpponentOnCrosshair() && bot.isSprinting()){
            if(analysisData.itemLoaded()) return s();
        } else if(analysisData.aboutToReach(2) && step != 2){
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
        return bot.isSprinting() && analysisData.isOpponentOnCrosshair() && analysisData.itemLoaded();
    }

    @Override
    public int requiredSkill() {
        return 50;
    }
}
