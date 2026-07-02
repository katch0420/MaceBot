package net.katch0420.macebot.main.macebot.control;

import net.katch0420.macebot.main.MaceBot;
import net.katch0420.macebot.main.macebot.bot.PlayerBot;
import net.katch0420.macebot.main.macebot.control.actions.BotAction;
import net.katch0420.macebot.main.settings.main.SettingsKey;
import net.katch0420.macebot.main.settings.server.Settings;
import net.katch0420.macebot.main.settings.server.SettingsSyncHelper;
import net.katch0420.macebot.main.utils.PlayerBuffs;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static net.katch0420.macebot.main.macebot.control.ActionContext.macebotCtx;
import static net.katch0420.macebot.main.macebot.control.ActionContext.opponentCtx;

/**
 * Main tick loop — orchestrates context, selection, and execution.
 * <p>
 * Controller no longer knows anything about individual actions.
 * It just holds the current BotAction and calls tick() on it.
 * Actions chain to each other via TickResult.successThen().
 */
public class Controller {

    public final ActionManager am;
    public final ActionSelector selector;
    public final ActionContext ctx;
    public final ShieldChecker shieldChecker;

    public BotAction currentAction = Actions.NONE;
    public BotAction lastAction = null;
    public static Mode lastMode = null;

    public boolean attributeDirty = false;

    private int actionSelectDelayFlag = 0;

    public Controller() {
        this.ctx = new ActionContext(this);
        this.am = new ActionManager();
        this.selector = new ActionSelector();
        this.shieldChecker = new ShieldChecker();
    }

    // ── Main tick ─────────────────────────────────────────────────────────────

    public void tick() {
        if (!ctx.update()) {
            return;
        }

        am.update();

        if (!macebotCtx.isAvailable()) return;
        if (MaceBot.server.getTicks() % 600 == 0)
            SettingsSyncHelper.applyAndBroadcast(SettingsKey.MACEBOT_ID, am.macebot.getId(), MaceBot.server);
        if (Settings.isMacebotBuffsEnabled() && MaceBot.server.getTicks() % 20 == 0)
            PlayerBuffs.applyBuffsToPlayer(macebotCtx.serverPlayer);
        switch (Settings.getMode()) {
            case NPC -> {
                if (currentAction != Actions.NONE) clearAction();
                if (opponentCtx.isAvailable() && Settings.isMacebotCanDoTracking()) am.lookAtOpponent();
            }
            case FIGHT -> tickFight();
            case PRACTICE -> tickPractice();
        }
    }

    private void tickFight() {
        if (!opponentCtx.isAvailable()) return;
        if (lastAction == Actions.STUN_SLAM_PRACTICE) am.macebot.stopUsingItem();
        if (((PlayerBot) am.macebot).frozen()) ((PlayerBot) am.macebot).unfreeze();
        // ── 1. Shield check always runs first ─────────────────────────────────
        if (attributeDirty) resetAttributes();
        BotAction shieldDecision = shieldChecker.check(currentAction, am);

        if (shieldDecision == Actions.BLOCK && currentAction != Actions.BLOCK && am.canBlock()) {
            // Start blocking
            setAction(Actions.BLOCK);
        } else if (shieldDecision != Actions.BLOCK && currentAction == Actions.BLOCK) {
            // Stop blocking
            clearAction();
        }

        // ── 2. Non-shield actions ─────────────────────────────────────────────
        if (currentAction != Actions.BLOCK) {
            maybeEat();

            if (currentAction == Actions.NONE) {
                if (actionSelectDelayFlag >= Settings.getDifficulty().actionSelectDelay) {
                    setAction(selector.select(ctx));
                    actionSelectDelayFlag = 0;
                } else actionSelectDelayFlag++;
            }
        }

        // ── 3. Execute current action ─────────────────────────────────────────
        if (currentAction == Actions.NONE) return;

        TickResult result = currentAction.tick(am, ctx);

        switch (result.status) {
            case SUCCESS -> {
                currentAction.onEnd(am, ctx);
                // Chain to next action if the action requested it
                if (result.nextAction != null) {
                    selector.setLastAction(currentAction);
                    setAction(result.nextAction);
                } else {
                    currentAction = Actions.NONE;
                }
            }
            case FAIL -> clearAction();
            case PASS -> {
            } // keep running
        }
    }

    private void resetAttributes() {
    }

    private void tickPractice() {
        BotAction action = switch (Settings.getPracticeMode()) {
            case STUN_SLAM -> Actions.STUN_SLAM_PRACTICE;
            case STATIC_AIM -> Actions.AIM_PRACTICE;
            case GRAPPLING -> Actions.GRAPPLING_PRACTICE;
        };
        if (action != lastAction) {
            setAction(action);
        }
        if (action == Actions.STUN_SLAM_PRACTICE)
            if (((PlayerBot) am.macebot).frozen()) ((PlayerBot) am.macebot).unfreeze();

        TickResult result = currentAction.tick(am, ctx);

        switch (result.status) {
            case SUCCESS -> {
                currentAction.onEnd(am, ctx);
                // Chain to next action if the action requested it
                if (result.nextAction != null) {
                    selector.setLastAction(currentAction);
                    setAction(result.nextAction);
                } else {
                    currentAction = Actions.NONE;
                }
            }
            case FAIL -> clearAction();
            case PASS -> {
            } // keep running
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void setAction(BotAction action) {
        if (action == null) return;
        lastAction = action;
        currentAction = action;
        currentAction.onStart(am, ctx);
    }

    private void clearAction() {
        currentAction.onEnd(am, ctx);
        currentAction = Actions.NONE;
        am.resetAllMovements();
    }

    /**
     * Interrupt current action to eat if health is low.
     * Does NOT interrupt blocking — shield priority is handled above.
     */
    private void maybeEat() {
        if (currentAction == Actions.EAT) return;

        boolean healthLow = macebotCtx.getHealth() < 10 || (macebotCtx.getHealth() < 15 && opponentCtx.isAvailable() && opponentCtx.getHealth() >= macebotCtx.getHealth());

        boolean foodLow = macebotCtx.getFoodLevel() < 4 || macebotCtx.getFoodLevel() < 7;

        if (healthLow || foodLow) {
            if (currentAction != Actions.NONE) clearAction();
            setAction(Actions.EAT);
        }
    }

    // ── Pause / resume ────────────────────────────────────────────────────────

    public void pause() {
        lastMode = Settings.getMode();
        SettingsSyncHelper.applyAndBroadcast(SettingsKey.MODE, Mode.NPC, MaceBot.server);
    }

    public void startOrResume() {
        SettingsSyncHelper.applyAndBroadcast(SettingsKey.MODE, lastMode != null && lastMode != Mode.NPC ? lastMode : Mode.FIGHT, MaceBot.server);
    }

    public void pauseAndClear() {
        pause();
        clearAction();
    }

    // ── Enums ─────────────────────────────────────────────────────────────────

    public enum Mode {
        NPC(Text.literal("Npc").formatted(Formatting.GREEN)),
        FIGHT(Text.literal("Fight").formatted(Formatting.GOLD)),
        PRACTICE(Text.literal("Practice").formatted(net.minecraft.util.Formatting.YELLOW));

        public final Text displayText;

        Mode(Text displayText) {
            this.displayText = displayText;
        }
    }

    public enum PracticeMode {STUN_SLAM, STATIC_AIM, GRAPPLING}
}