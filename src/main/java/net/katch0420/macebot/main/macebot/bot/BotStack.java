package net.katch0420.macebot.main.macebot.bot;

import net.katch0420.macebot.main.macebot.control.ItemClassifier;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tracks the item in one hand of a BotPlayer.
 * Classification is handled by ItemClassifier — no if-chains here.
 */
public class BotStack {

    private static final Logger log = LoggerFactory.getLogger(BotStack.class);
    public final BotPlayer owner;
    public final Hand      hand;

    public ItemStack  itemStack  = ItemStack.EMPTY;
    public StackType  stackType  = StackType.EMPTY;
    public AttackType attackType = AttackType.FIST;
    public StackStatus stackStatus = StackStatus.NONE;

    public BotStack(BotPlayer owner, Hand hand) {
        this.owner = owner;
        this.hand  = hand;
    }

    // ── Update ────────────────────────────────────────────────────────────────

    public void update() {
        if (!owner.isAvailable()) return;

        itemStack = owner.serverPlayer.getStackInHand(hand);

        ItemClassifier.Classification c = ItemClassifier.classify(itemStack);
        stackType  = c.stackType();
        attackType = c.attackType();

        updateStatus();
    }

    private void updateStatus() {
        // On cooldown (offhand only)
        if (hand == Hand.OFF_HAND
                && owner.serverPlayer.getItemCooldownManager()
                //? if >=1.21.2 {
                /*.isCoolingDown(itemStack)) {
                *///?} else
                .isCoolingDown(itemStack.getItem())) {
            stackStatus = StackStatus.ON_COOLDOWN;
            return;
        }

        // Main hand already claimed usability
        if (hand == Hand.OFF_HAND
                && owner.mainHand.stackStatus == StackStatus.CAN_USE) {
            stackStatus = StackStatus.NONE;
            return;
        }

        // Types that are always usable from offhand
        boolean usable = switch (stackType) {
            case SHIELD, ARMOR, UTILS, CONSUMABLE, PROJECTILES -> true;
            case WEAPON -> attackType == AttackType.RANGE || attackType == AttackType.BOTH;
            default -> false;
        };

        stackStatus = usable ? StackStatus.CAN_USE : StackStatus.NONE;
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    public boolean isWeapon()     { return stackType == StackType.WEAPON;      }
    public boolean isConsumable() { return stackType == StackType.CONSUMABLE;  }
    public boolean isShield()     { return stackType == StackType.SHIELD;      }
    public boolean isArmor()      { return stackType == StackType.ARMOR;       }
    public boolean isProjectile() { return stackType == StackType.PROJECTILES; }
    public boolean isMelee()      { return attackType == AttackType.MELEE || attackType == AttackType.BOTH; }
    public boolean isRanged()     { return attackType == AttackType.RANGE || attackType == AttackType.BOTH; }

    public void setStack(ItemStack stack) {
        owner.serverPlayer.setStackInHand(hand, stack);
        update();
    }

    // ── Enums ─────────────────────────────────────────────────────────────────

    public enum StackStatus  { NONE, ON_COOLDOWN, CAN_USE }
    public enum AttackType   { MELEE, RANGE, BOTH, FIST }
    public enum StackType    { WEAPON, ARMOR, TOOL, SHIELD, UTILS, PROJECTILES, CONSUMABLE, ELSE, EMPTY }
}