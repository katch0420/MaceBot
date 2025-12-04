package net.katch0420.macebot.ai;

import net.katch0420.macebot.player.Kits;
import net.katch0420.macebot.playerbot.PlayerBotSettings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Controller {
    public ActionManager actionManager;
    public Action action = Action.NONE;
    public Action lastAction = Action.NONE;
    public ActionManager.Status status;
    public static Difficulty difficulty = Difficulty.NPC;

    public Map<Action, Double> probabilityMap = new HashMap<>();
    public Map<Action, List<Runnable>> probabilityFactors = new HashMap<>();

    private double probability = 0;
    private int delayBetweenActions;

    public Controller(ActionManager actionManager){
        this.actionManager = actionManager;
        loadDefaultProbabilityMap();
        loadProbabilityFactors();
    }
    public void tick(){
        actionManager.update();
        if(delayBetweenActions > 0) delayBetweenActions--;
        if(difficulty == Difficulty.NPC){
            action = Action.NONE;
            if(!actionManager.nearestPlayerIsNull) actionManager.lookAt(actionManager.nearestPlayer.getCameraPosVec(0.5f));
            return;
        }
        updateProbabilityMap();
        checkForEating();
        selectAction();
        executeAction();

    }

    private void selectAction(){
        if(action == Action.NONE && delayBetweenActions <= 0) {
            if (Math.random() < probabilityMap.get(Action.CRIT) / 100) action = Action.CRIT;
            else if (Math.random() < probabilityMap.get(Action.ELYTRA_LAUNCH) / 100) action = Action.ELYTRA_LAUNCH;
            else if (Math.random() < probabilityMap.get(Action.REGULAR_LAUNCH) / 100) action = Action.REGULAR_LAUNCH;
            else selectAction();
        }
    }

    private void executeAction(){
        switch (action){
            case NONE -> actionManager.resetValues();
            case EAT -> {
                status = actionManager.eat();
                if(status == ActionManager.Status.SUCCESS) {
                    action = Action.NONE;
                }
                else if(status == ActionManager.Status.FAIL) {
                    Kits.refillItems(actionManager.player);
                }
            }
            case CRIT -> {
                status = actionManager.critHit(0);
                if(status != ActionManager.Status.PASS) clearActions();
            }
            case REGULAR_LAUNCH -> {
                status = actionManager.regularLaunch(true);
                if(status == ActionManager.Status.SUCCESS) action = Action.MACE_HIT;
                else if(status == ActionManager.Status.FAIL) clearActions();
            }
            case MACE_HIT -> {
                status = actionManager.maceHit(true);
                if(status != ActionManager.Status.PASS) clearActions();
            }
            case ELYTRA_LAUNCH -> {
                status = actionManager.elytraLaunch();
                if(status == ActionManager.Status.SUCCESS) action = Action.ELYTRA_ATTACK;
                else if(status == ActionManager.Status.FAIL) clearActions();
            }
            case ELYTRA_ATTACK -> {
                status = actionManager.elytraAttack();
                if(status == ActionManager.Status.SUCCESS) action = Action.MACE_HIT;
                else if(status == ActionManager.Status.FAIL) clearActions();
            }
        }
    }

    private void checkForEating(){
        if(!actionManager.nearestPlayerIsNull){
            if(!(actionManager.nearestPlayer.getHealth() < actionManager.player.getHealth())){
                if(actionManager.player.getHealth() < 10 || actionManager.player.getHungerManager().getFoodLevel() < 15){
                    action = Action.EAT;
                }
            }
        }
        if(actionManager.player.getHealth() < 4 || actionManager.player.getHungerManager().getFoodLevel() < 7){
            action = Action.EAT;
        }
    }
    private void updateProbabilityMap(){
        List<Action> actionList = List.of(Action.CRIT, Action.REGULAR_LAUNCH, Action.ELYTRA_LAUNCH);
        for (Action action : actionList){
            updateProbabilityMap(action);
        }
    }
    private void updateProbabilityMap(Action action){
        List<Runnable> runnableList = probabilityFactors.get(action);
        for (Runnable r: runnableList){
            r.run();
            System.out.println(action +" : "+ probabilityMap.get(action));
        }
        System.out.println(lastAction);
    }
    private void clearActions(){
        lastAction = action;
        action = Action.NONE;
        delayBetweenActions = 2;
    }

    public void loadProbabilityFactors(){
        probabilityFactors.put(Action.CRIT, List.of(
                ()-> probability = 20,
                ()-> probability += actionManager.distanceToNearbyPlayer < 8 ? 20d : 0d,
                ()-> probability += lastAction == Action.CRIT ? 40d : 0d,
                ()-> probability = PlayerBotSettings.crits ? probability : 0d,
                ()-> probabilityMap.replace(Action.CRIT, probability)
        ));

        probabilityFactors.put(Action.REGULAR_LAUNCH, List.of(
                ()-> probability = PlayerBotSettings.mace ? 40d : 0d,
                ()-> probabilityMap.replace(Action.REGULAR_LAUNCH, probability)
        ));

        probabilityFactors.put(Action.ELYTRA_LAUNCH, List.of(
                ()-> probability = PlayerBotSettings.elytra ? 40d : 0d,
                ()-> probabilityMap.replace(Action.ELYTRA_LAUNCH, probability)
        ));
    }
    public void loadDefaultProbabilityMap(){
        probabilityMap.clear();
        probabilityMap.put(Action.CRIT, 30d);
        probabilityMap.put(Action.ELYTRA_LAUNCH, 30d);
        probabilityMap.put(Action.REGULAR_LAUNCH, 40d);
    }
    public enum Difficulty{
        NPC,
        EASY
    }
    public enum Action{
        NONE,
        EAT,
        CRIT,
        MACE_HIT,
        REGULAR_LAUNCH,
        ELYTRA_LAUNCH,
        ELYTRA_ATTACK
    }
    public void pauseTheBot(){
        difficulty = Difficulty.NPC;
        actionManager.resetValues();
        actionManager.unequipElytra();
        actionManager.resetAllMovements();
    }
}
