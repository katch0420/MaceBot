package net.katch0420.macebot.main.macebot.ai;

import net.katch0420.macebot.main.macebot.ai.action.*;
import net.katch0420.macebot.main.macebot.ai.action.melee.CritHit;
import net.katch0420.macebot.main.macebot.ai.action.melee.KbHit;
import net.katch0420.macebot.main.macebot.ai.action.melee.MaceHit;
import net.katch0420.macebot.main.macebot.ai.action.movement.WindChargeLaunch;
import net.katch0420.macebot.main.macebot.bot.PlayerBot;
import net.katch0420.macebot.main.settings.Settings;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;

import static net.katch0420.macebot.main.settings.Settings.MODE;

public class Core {
    public PlayerBot bot;
    public ServerPlayer enemy;

    public LowLevelActions lla;
    public Analyzer analyzer;
    public AnalysisData analysisData;
    public Skill skill;
    public OpponentProfile opponentProfile;

    //Interference
    public Action<?> underInterference;
    public Interference nextInterference;
    public Interference currentInterference;
    public Interference lastInterference;
    public boolean interfering;

    //Fight
    public Action<?> current;
    public Action<?> last;
    public Action<?> followUp;
    public Action<?> optionalFollowUp;

    public CritHit critHit;
    public KbHit kbHit;
    public MaceHit maceHit;

    public WindChargeLaunch windChargeLaunch;

    public final ArrayList<Action<?>> actions = new ArrayList<>();
    public final ArrayList<Action<?>> availableActions = new ArrayList<>();
    public final ArrayList<Action<?>> sequelActions = new ArrayList<>();
    public final ArrayList<Action<?>> initialActions = new ArrayList<>();

    private Mode lastMode = Mode.NPC;

    public Core(PlayerBot bot){
        this.bot = bot;
        init();
    }

    private void init() {
        analysisData = new AnalysisData(this);
        analyzer = new Analyzer(this);
        skill = new Skill(this);
        opponentProfile = new OpponentProfile();
        lla = new LowLevelActions(this);
        changeEnemy(analysisData.enemy);
        initActions();
    }

    private void initActions() {
        actions.clear();
        sequelActions.clear();
        initialActions.clear();

        critHit = new CritHit(this);
        kbHit = new KbHit(this);
        maceHit = new MaceHit(this);

        windChargeLaunch = new  WindChargeLaunch(this);
    }

    private void updateActionLists() {
        availableActions.clear();
        sequelActions.clear();
        initialActions.clear();
    }

    private void changeEnemy(ServerPlayer enemy){
        if(enemy == this.enemy) return;
        opponentProfile.reset(enemy);
        analyzer.enemy = enemy;
        lla.enemy = enemy;
        this.enemy = enemy;
        analysisData.enemy = enemy;
        current = null;
        followUp = null;
    }

    public void tick(){
        changeEnemy(analysisData.getEnemy());
        if(isAlone()) return;
        switch(MODE.getValue()){
            case FIGHT -> tickFight();
            case PRACTICE -> tickPrac();
            case NPC -> tickNpc();
        }
    }

    private void tickNpc() {
        if(lla.input.hasMovement()) lla.inputMovement(0,0);
        if(Settings.MACEBOT_CAN_DO_TRACKING.getValue()){
            lla.lookAtOpponent();
        }
    }

    private void tickPrac() {
    }

    public void tickFight(){
        //TODO: Interference Logic
        if(isIdle()){
            if(hasFollowUp()){
                selectFollowUp();
            } else if(hasOptionalFollowUp()){
                optionalOrSelect();
            } else {
                selectSuitableInitialAction();
            }
        }
        if(!isIdle()){
            Result result = current.process();
            switch (result.s()){
                case SUCCESS, FAIL -> {
                    updateLast();
                    removeCurrent();
                }
                case MUST_DO -> {
                    updateLast();
                    removeCurrent();
                    followUp = result.n();
                }
                case MAY_DO -> {
                    updateLast();
                    removeCurrent();
                    optionalFollowUp = result.n();
                }
                case TICK -> {}
            }
        }
    }

    private void selectSuitableInitialAction() {
        current = windChargeLaunch.getFreshAction().doPreparation().setupAction();
    }

    private void optionalOrSelect() {
        selectOptionalFollowUp();
    }

    //Operations
    private void selectFollowUp() {
        current = followUp;
        removeFollowUp();
    }
    private void selectOptionalFollowUp() {
        current = optionalFollowUp;
        removeOptionalFollowUp();
    }
    private void updateLast() {
        last = current;
    }
    public void removeFollowUp(){followUp = null;}
    public void removeCurrent(){current = null;}
    public void removeOptionalFollowUp(){optionalFollowUp = null;}

    public boolean hasFollowUp() {return followUp != null;}
    public boolean hasOptionalFollowUp() {return optionalFollowUp != null;}
    public boolean isIdle() {return current == null;}
    public boolean isAlone() {return enemy == null || !enemy.gameMode().isSurvival() || enemy.isDeadOrDying();}

    //Controls
    public void pause() {
        lastMode = MODE.getValue();
        MODE.update(Mode.NPC);
    }
    public void play() {
        if(lastMode == Mode.NPC) MODE.update(Mode.FIGHT);
        else MODE.update(lastMode);
    }

    public record Interference(Action<?> interfereAction, Priority priority) {

        public static Interference getResult(Action<?> interfereAction, Priority priority) {
                return new Interference(interfereAction, priority);
        }

        public boolean isCritical() {
                return priority == Priority.CRITICAL;
        }

        public boolean isNone(){
            return priority == Priority.NONE;
        }

        public boolean hasHigherPriority(Interference prev){
            return this.priority.priority < prev.priority.priority || this.isCritical();
        }

        public enum Priority {
            CRITICAL(1),
            HIGH(2),
            MEDIUM(3),
            LOW(4),
            NONE(5);

            public final int priority;

            Priority(int priority) {
                this.priority = priority;
            }
        }
    }
}
