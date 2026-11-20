package net.katch0420.macebot.main.macebot.ai.action;

import net.katch0420.macebot.main.macebot.ai.*;
import net.katch0420.macebot.main.macebot.bot.BotInput;
import net.katch0420.macebot.main.macebot.bot.BotSlots;
import net.katch0420.macebot.main.macebot.bot.PlayerBot;

import java.util.List;
import java.util.Objects;

public class Action<T extends Action<T>>{

    public Core core;
    public PlayerBot bot;
    public AnalysisData analysisData;
    public Analyzer analyzer;
    public BotInput input;
    public LowLevelActions lla;
    private int suggestedSlot = -1;

    public enum Stage{PREPARATION, EXECUTION, INTERFERENCE, NONE}

    public Stage stage = Stage.NONE;
    public boolean prepare = false;
    public int step = 1;

    public Action(Core core) {
        this.core = core;
        this.bot = core.bot;
        this.analysisData = core.analysisData;
        this.analyzer = core.analyzer;
        this.lla = core.lla;
        this.input = core.lla.input;
        core.actions.add(this);
        init();
    }

    protected void init() {
    }

    protected boolean adjustSlot(){
        if(!usableSlots().contains(lla.selectedSlot())){
            lla.selectSlot(suggestedSlot == -1 ? preferredSlot() : suggestedSlot);
            return true;
        }
        return false;
    }

    protected List<Integer> usableSlots(){
        return List.of(0,1,2,3,4,5,6,7,8);
    }

    protected int preferredSlot(){
        if(usableSlots().isEmpty()){
            return BotSlots.SWORD;
        } else return usableSlots().getFirst();
    }

    public void setUsageSlot(int slot){
        if(usableSlots().contains(slot)) this.suggestedSlot = slot;
        else suggestedSlot = preferredSlot();
    }

    public Result process(){
        switch (stage){
            case PREPARATION -> {
                Result r = preparation();
                if (r.s() == Result.Status.SUCCESS) {
                    setStage(Stage.EXECUTION);
                    return t();
                }
                return r;
            }
            case EXECUTION -> {
                return execution();
            }
            case INTERFERENCE -> {
                return interference();
            }
        }
        return Result.fail();
    }

    public Action<T> doPreparation() {
        prepare = true;
        return this;
    }

    public Result preparation(){
        return Result.success();
    }

    public Result execution(){
        return Result.success();
    }

    public Result interference(){
        return Result.success();
    }

    public boolean interfere(){
        return false;
    }

    public void startInterference(){
        setStage(Stage.INTERFERENCE);
    }

    public boolean readyForAct(){
        return true;
    }

    public boolean canPrepare(){
        return false;
    }

    public boolean canInterfereImmediately(Core.Interference interference){
        return true;
    }


    public boolean cannotInterfere(Core.Interference interference){
        return false;
    }

    public boolean isFollowUp(){
        return false;
    }

    public boolean canBeSelected(){
        return !isFollowUp() && hasEnoughSkill() && enabled();
    }

    public boolean enabled() {
        return true;
    }

    public boolean hasEnoughSkill(){
        return core.skill.getSkillPercentage() >= requiredSkill();
    }

    public int requiredSkill(){
        return 0;
    }

    protected void resetStep(){
        step = 1;
    }

    @SuppressWarnings("unchecked")
    public T reuseAction(){
        clearCache();
        clearMeta();
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T getFreshAction() {
        clearCache();
        clearMeta();
        clearProperties();
        clearStage();
        return (T) this;
    }

    public Action<?> setupAction(){
        setStage(prepare ? Stage.PREPARATION : Stage.EXECUTION);
        return this;
    }

    protected void clearProperties() {
    }

    protected void clearCache() {
    }

    protected void clearMeta() {
        prepare = false;
        resetStage();
    }

    protected void clearStage(){
        setStage(Stage.NONE);
    }

    protected void resetStage() {
        setStage(prepare && canPrepare() ? Stage.PREPARATION : Stage.EXECUTION);
    }

    protected void setStage(Stage stage) {
        this.stage = stage;
        resetStep();
    }

    protected Result stepUp(){
        step++;
        return t();
    }

    protected Result stepDown(){
        step--;
        return t();
    }

    protected Result stepTo(int s){
        step = s;
        return t();
    }

    protected Result t() {
        return Result.tick();
    }

    protected Result f(){
        return Result.fail();
    }

    protected Result s(){
        return Result.success();
    }

    protected Result m_d(Action<?> n){
        return Result.mayDo(n);
    }

    protected Result md(Action<?> n){
        return Result.mustDo(n);
    }
}