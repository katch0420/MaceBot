package net.katch0420.macebot.main.macebot.control;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public enum Difficulty {
    NOOB(
            40,
            20,
            8,
            10,
            10, 0.6,
            Text.literal("Noob").formatted(Formatting.GREEN)
    ),
    PRO(
            70,
            60,
            4,
            40,
            6, 0.3,
            Text.literal("Pro").formatted(Formatting.YELLOW)
    ),
    MASTER(
            90,
            90,
            1,
            60,
            2, 0.1,
            Text.literal("Master").formatted(Formatting.GOLD)
    );

    public final int hitChance;
    public final int shieldChance;
    public final int actionSelectDelay;
    public final int deflectProjectilesChance;
    public final int windBoostJumpOffset;
    public final double entityDetectionRangeDecrease;
    public final Text displayText;

    Difficulty(int hitChance, int shieldChance, int actionSelectDelay, int deflectWindChargeChance, int windBoostJumpOffset, double entityDetectionRangeDecrease, Text displayText) {
        this.hitChance = hitChance;
        this.shieldChance = shieldChance;
        this.actionSelectDelay = actionSelectDelay;
        this.deflectProjectilesChance = deflectWindChargeChance;
        this.windBoostJumpOffset = windBoostJumpOffset;
        this.entityDetectionRangeDecrease = entityDetectionRangeDecrease;
        this.displayText = displayText;
    }
}
