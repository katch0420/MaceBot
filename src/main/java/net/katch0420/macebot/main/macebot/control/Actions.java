package net.katch0420.macebot.main.macebot.control;

import net.katch0420.macebot.main.macebot.control.actions.*;
import net.katch0420.macebot.main.macebot.control.actions.fight.*;
import net.katch0420.macebot.main.macebot.control.actions.practice.AimPracticeAction;
import net.katch0420.macebot.main.macebot.control.actions.practice.GrapplingPracticeAction;
import net.katch0420.macebot.main.macebot.control.actions.practice.StunSlamPracticeAction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Static registry of every BotAction — mirrors how Minecraft's Items class works.
 *
 * Actions that appear in the random selection pool are added via register().
 * Actions triggered by game logic (EAT, BLOCK, MACE_HIT, ELYTRA_ATTACK) are
 * declared but not registered so they never show up in ActionSelector.
 *
 * To add a new action:
 *   1. Create MyAction extends BotAction in the actions/ package
 *   2. Add a static field here
 *   3. If it should be randomly selectable, call register()
 */
public final class Actions {

    private static final List<BotAction> SELECTABLE = new ArrayList<>();

    // ── Non-selectable (triggered by Controller logic) ────────────────────────
    public static final BotAction NONE                  = new NoneAction();
    public static final BotAction EAT                   = new EatAction();
    public static final BotAction MACE_HIT              = new MaceHitAction();
    public static final BotAction ELYTRA_ATTACK         = new ElytraAttackAction();
    public static final BotAction BLOCK                 = new BlockAction();

    public static final BotAction STUN_SLAM_PRACTICE    = new StunSlamPracticeAction();
    public static final BotAction AIM_PRACTICE          = new AimPracticeAction();
    public static final BotAction GRAPPLING_PRACTICE    = new GrapplingPracticeAction();

    // ── Selectable (randomly weighted) ───────────────────────────────────────
    public static final BotAction CRIT                  = register(new CritHitAction());
    public static final BotAction KNOCKBACK             = register(new KbHitAction());
    public static final BotAction REGULAR_LAUNCH        = register(new RegularLaunchAction());
    public static final BotAction ELYTRA_LAUNCH         = register(new ElytraLaunchAction());

    // ── Registry ──────────────────────────────────────────────────────────────

    private static BotAction register(BotAction action) {
        SELECTABLE.add(action);
        return action;
    }

    /** All actions eligible for random selection by ActionSelector. */
    public static List<BotAction> getSelectable() {
        return Collections.unmodifiableList(SELECTABLE);
    }

    private Actions() {}

    // ── Built-in NoneAction ───────────────────────────────────────────────────

    private static class NoneAction extends BotAction {
        @Override public boolean canSelect() { return false; }
        @Override public double getWeight(ActionContext ctx, BotAction last) { return 0; }
        @Override public TickResult tick(ActionManager am, ActionContext ctx) { return TickResult.pass(); }
    }
}