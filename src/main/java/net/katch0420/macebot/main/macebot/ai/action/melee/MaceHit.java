package net.katch0420.macebot.main.macebot.ai.action.melee;

import net.katch0420.macebot.main.macebot.ai.Core;
import net.katch0420.macebot.main.macebot.ai.Result;
import net.katch0420.macebot.main.macebot.ai.action.Action;
import net.katch0420.macebot.main.macebot.bot.BotSlots;

import java.util.List;

public class MaceHit extends Action<MaceHit>{

    boolean canCombo;

    public MaceHit canCombo(){
        canCombo = true;
        return this;
    }

    public MaceHit(Core core) {
        super(core);
    }

    @Override
    protected List<Integer> usableSlots() {
        return List.of(BotSlots.DENSITY_MACE, BotSlots.BREACH_MACE);
    }

    @Override
    public boolean canPrepare() {
        return true;
    }

    @Override
    public boolean readyForAct() {
        return bot.fallDistance >= 1.5 && !bot.isFallFlying() && analysisData.isOpponentOnCrosshair();
    }

    @Override
    public Result preparation() {
        if(bot.onGround()) return f();
        lla.lookAtOpponent();
        if(bot.isFallFlying()){
            lla.unequipElytra();
            return t();
        }
        if(!analysisData.aboutToReachXZ(7)){
            if(lla.grapple()) {
                adjustSlot();
                return t();
            }
            return f();
        }
        if(!analysisData.aboutToReach(0)){
            lla.inputMovement(1, lla.strafe());
        } else {
            if(bot.fallDistance >= 1.5 && analysisData.isOpponentOnCrosshair()){
                return s();
            }
        }
        return t();
    }

    @Override
    protected void clearProperties() {
        canCombo = false;
    }

    boolean processingCombo;
    @Override
    public Result execution() {
        if(processingCombo){
            if(bot.getDeltaMovement().y > 0) setStage(Stage.PREPARATION);
            else return f();
        }
        if(readyForAct()){
            lla.lookAtOpponent();
            lla.attack();
            if(canCombo){
                processingCombo = true;
                return t();
            } else return s();
        }
        return f();
    }

    @Override
    public int requiredSkill() {
        return 20;
    }
}
