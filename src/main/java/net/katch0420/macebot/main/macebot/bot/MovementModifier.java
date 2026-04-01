package net.katch0420.macebot.main.macebot.bot;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.consume.UseAction;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Data-driven movement speed modifiers for the bot's travel() method.
 *
 * Each entry is a (condition → forward multiplier, sideways multiplier) pair.
 * Checked in order — first match wins.
 *
 * To add new item-based movement modifiers, add one entry to MODIFIERS.
 */
public final class MovementModifier {

    public record Modifier(float forward, float sideways) {}

    private record Entry(Predicate<ItemStack> condition, Modifier modifier) {}

    private static final List<Entry> MODIFIERS = new ArrayList<>();

    static {
        // Food / drink — very slow
        add(s -> s.getUseAction() == UseAction.EAT || s.getUseAction() == UseAction.DRINK,
                BotConfig.EAT_MULTIPLIER, BotConfig.EAT_MULTIPLIER);

        // Shield — slow
        add(s -> s.isOf(Items.SHIELD),
                BotConfig.SHIELD_MULTIPLIER, BotConfig.SHIELD_MULTIPLIER);

        // Spyglass — very slow
        add(s -> s.isOf(Items.SPYGLASS),
                BotConfig.SPYGLASS_MULTIPLIER, BotConfig.SPYGLASS_MULTIPLIER);
    }

    private static void add(Predicate<ItemStack> condition, float forward, float sideways) {
        MODIFIERS.add(new Entry(condition, new Modifier(forward, sideways)));
    }

    /**
     * Return the movement modifier for the active item, or null if no entry matches
     * (meaning no multiplier should be applied).
     */
    public static Modifier getModifier(ItemStack activeItem) {
        if (activeItem == null || activeItem.isEmpty()) return null;
        for (Entry entry : MODIFIERS) {
            if (entry.condition().test(activeItem)) return entry.modifier();
        }
        return null;
    }

    private MovementModifier() {}
}