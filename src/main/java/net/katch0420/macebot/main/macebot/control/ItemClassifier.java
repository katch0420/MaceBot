package net.katch0420.macebot.main.macebot.control;

import net.katch0420.macebot.main.macebot.bot.BotStack.AttackType;
import net.katch0420.macebot.main.macebot.bot.BotStack.StackType;
import net.minecraft.item.*;
import net.minecraft.item.consume.UseAction;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Data-driven item classifier — replaces the hardcoded if-chain in the old Stack class.
 *
 * Each entry is a (predicate → StackType, AttackType) pair checked in order.
 * To add support for a new item, add one line to the CLASSIFIERS list.
 *
 * Uses Minecraft's own UseAction for items like bows/shields/food, so new
 * items that use standard actions are classified automatically.
 */
public final class ItemClassifier {

    public record Classification(StackType stackType, AttackType attackType) {}

    private record Entry(Predicate<ItemStack> test, Classification result) {}

    private static final List<Entry> CLASSIFIERS = new ArrayList<>();

    // ── Registration ──────────────────────────────────────────────────────────

    static {
        // Melee weapons
        add(s -> s.getItem() instanceof SwordItem || s.getItem() instanceof AxeItem,
                StackType.WEAPON, AttackType.MELEE);

        // Armor
        add(s -> s.getItem() instanceof ArmorItem,
                StackType.ARMOR, AttackType.FIST);

        // Mining tools
        add(s -> s.getItem() instanceof PickaxeItem
                        || s.getItem() instanceof ShovelItem
                        || s.getItem() instanceof HoeItem,
                StackType.TOOL, AttackType.MELEE);

        // Non-combat tools
        add(s -> s.getItem() instanceof ShearsItem
                        || s.getItem() instanceof FlintAndSteelItem,
                StackType.TOOL, AttackType.FIST);

        // Throwable projectiles
        add(s -> s.getItem() instanceof WindChargeItem
                        || s.getItem() instanceof EnderPearlItem
                        || s.getItem() instanceof EggItem
                        || s.getItem() instanceof SnowballItem,
                StackType.PROJECTILES, AttackType.RANGE);

        // UseAction-based (handles bows, crossbows, shields, food, tridents, etc.)
        add(s -> s.getUseAction() == UseAction.BOW || s.getUseAction() == UseAction.CROSSBOW,
                StackType.WEAPON, AttackType.RANGE);
        add(s -> s.getUseAction() == UseAction.EAT || s.getUseAction() == UseAction.DRINK,
                StackType.CONSUMABLE, AttackType.FIST);
        add(s -> s.getUseAction() == UseAction.SPEAR,
                StackType.WEAPON, AttackType.BOTH);
        add(s -> s.getUseAction() == UseAction.BLOCK,
                StackType.SHIELD, AttackType.FIST);
        add(s -> s.getUseAction() == UseAction.SPYGLASS,
                StackType.UTILS, AttackType.FIST);
    }

    private static void add(Predicate<ItemStack> test, StackType stackType, AttackType attackType) {
        CLASSIFIERS.add(new Entry(test, new Classification(stackType, attackType)));
    }

    // ── Classification ────────────────────────────────────────────────────────

    private static final Classification EMPTY = new Classification(StackType.EMPTY, AttackType.FIST);
    private static final Classification ELSE  = new Classification(StackType.ELSE,  AttackType.FIST);

    /**
     * Classify an ItemStack.
     * Returns EMPTY for empty stacks, ELSE for unrecognized items.
     */
    public static Classification classify(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return EMPTY;
        for (Entry entry : CLASSIFIERS) {
            if (entry.test().test(stack)) return entry.result();
        }
        return ELSE;
    }

    private ItemClassifier() {}
}