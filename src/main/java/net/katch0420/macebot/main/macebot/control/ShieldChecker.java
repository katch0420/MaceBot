package net.katch0420.macebot.main.macebot.control;

import net.katch0420.macebot.main.macebot.bot.BotSlots;
import net.katch0420.macebot.main.macebot.control.actions.BotAction;
import net.katch0420.macebot.main.settings.server.Settings;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import static net.katch0420.macebot.main.macebot.control.ActionContext.macebotCtx;
import static net.katch0420.macebot.main.macebot.control.ActionContext.opponentCtx;

/**
 * Checks each tick whether the bot should start or stop blocking.
 * Returns the appropriate BotAction for Controller to apply.
 * <p>
 * Renamed from ShieldAction to ShieldChecker since it doesn't execute
 * actions itself — it just decides and Controller acts.
 */
public class ShieldChecker {

    private int shieldDuration = 10;

    /**
     * @param currentAction the action currently running
     * @return Actions.BLOCK if bot should be blocking, currentAction otherwise
     */
    public BotAction check(BotAction currentAction, ActionManager am) {

        boolean isBlocking = currentAction == Actions.BLOCK;
        boolean shouldBlock = shouldBlock();

        if(isShieldUnUsable() && !isBlocking) return currentAction;

        if (!isBlocking && shouldBlock) {
            shieldDuration = 10;
            return Actions.BLOCK;
        }

        if(!isBlocking){
            return currentAction;
        }

        if(isShieldUnUsable()){
            return Actions.NONE;
        }

        if(!shouldBlock){
            if(shieldDuration > 0) {
                shieldDuration--;
                return Actions.BLOCK;
            } else {
                return Actions.NONE;
            }
        }

        return currentAction;
    }

    // ── Internal ──────────────────────────────────────────────────────────────

    private boolean shouldBlock() {
        Snapshot s = snapshot();
        if(s.canMace) return true;
        return s.health < 11 && (s.canDoMelee || s.canDoRange);
    }

    private boolean isShieldUnUsable(){
        if (!opponentCtx.isAvailable() || !Settings.isMacebotCanUseShield()) return true;

        ItemStack shield = macebotCtx.getInventory().getStack(BotSlots.SHIELD);
        if (!shield.isOf(Items.SHIELD)) return true;
        if (macebotCtx.serverPlayer.getItemCooldownManager()
                //? if >=1.21.2 {
                /*.isCoolingDown(shield)) return true;
                *///?} else
                .isCoolingDown(shield.getItem())) return true;
        return false;
    }

    /** Snapshot of combat conditions — computed once per check to avoid inconsistency. */
    private Snapshot snapshot() {
        double health = macebotCtx.getHealth();
        boolean isNearInXZ    = macebotCtx.getDistanceXZ(opponentCtx.getPlayer()) < 5;
        boolean isNear        = macebotCtx.getDistance(opponentCtx.getPlayer()) < 3;
        boolean isHoldingWeapon = opponentCtx.isHoldingWeapon();
        boolean weaponCharged = opponentCtx.getAttackCooldownProgress() > 0.9;

        boolean canMace    = opponentCtx.serverPlayer.fallDistance > 2 && isNearInXZ;
        boolean canDoMelee = isHoldingWeapon && isNear && opponentCtx.isMeleeWeapon() && weaponCharged;
        boolean canDoRange = isHoldingWeapon && opponentCtx.isRangedWeapon()
                && opponentCtx.isRangedWeaponCanShoot();

        return new Snapshot(health, canMace, canDoMelee, canDoRange);
    }

    private record Snapshot(double health,
                            boolean canMace,
                            boolean canDoMelee,
                            boolean canDoRange) {}
}