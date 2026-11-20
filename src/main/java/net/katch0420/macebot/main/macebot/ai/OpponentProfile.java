package net.katch0420.macebot.main.macebot.ai;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Items;

public final class OpponentProfile {
    private ServerPlayer opponent;
    private long samples;
    private long jumps;
    private long leftStrafes;
    private long rightStrafes;
    private long attacks;
    private long slamAttempts;
    private long windChargeUses;
    private long shieldUses;
    private double distanceTotal;
    private double aggressiveScore;
    private double previousX;
    private double previousZ;
    private boolean previousGrounded = true;
    private boolean previousFalling;
    private boolean previousWindCharge;
    private boolean previousShield;
    private boolean previousSwinging;
    private boolean hasPosition;

    public void reset(ServerPlayer opponent) {
        this.opponent = opponent;
        samples = jumps = leftStrafes = rightStrafes = attacks = 0;
        slamAttempts = windChargeUses = shieldUses = 0;
        distanceTotal = aggressiveScore = 0;
        previousGrounded = true;
        previousFalling = previousWindCharge = previousShield = false;
        previousSwinging = false;
        hasPosition = false;
    }

    public void update(Core core) {
        ServerPlayer enemy = core.enemy;
        if (enemy == null || enemy != opponent) {
            reset(enemy);
            return;
        }

        samples++;
        double distance = core.analysisData.getDistance();
        if (Double.isFinite(distance)) distanceTotal += distance;

        if (previousGrounded && !enemy.onGround()) jumps++;

        double dx = enemy.getX() - previousX;
        double dz = enemy.getZ() - previousZ;
        if (hasPosition && dx * dx + dz * dz > 0.0004) {
            double yaw = Math.toRadians(core.bot.getYRot());
            double lateral = dx * Math.cos(yaw) - dz * Math.sin(yaw);
            if (lateral > 0.01) leftStrafes++;
            else if (lateral < -0.01) rightStrafes++;
        }

        boolean swinging = enemy.swinging;
        if (swinging && !previousSwinging) attacks++;

        boolean fallingWithMace = !enemy.onGround()
                && enemy.fallDistance > 1.5f
                && enemy.getMainHandItem().is(Items.MACE);
        if (fallingWithMace && !previousFalling) slamAttempts++;

        boolean usingWindCharge = enemy.isUsingItem()
                && (enemy.getUseItem().is(Items.WIND_CHARGE)
                || enemy.getMainHandItem().is(Items.WIND_CHARGE));
        usingWindCharge |= swinging && enemy.getMainHandItem().is(Items.WIND_CHARGE);
        if (usingWindCharge && !previousWindCharge) windChargeUses++;

        boolean usingShield = enemy.isUsingItem()
                && (enemy.getUseItem().is(Items.SHIELD)
                || enemy.getMainHandItem().is(Items.SHIELD)
                || enemy.getOffhandItem().is(Items.SHIELD));
        if (usingShield && !previousShield) shieldUses++;

        double aggressionSample = Math.min(1.0, attacks * 20.0 / samples)
                * 0.7 + (distance < 4.0 ? 0.3 : 0.0);
        aggressiveScore += (aggressionSample - aggressiveScore) * 0.05;

        previousX = enemy.getX();
        previousZ = enemy.getZ();
        previousGrounded = enemy.onGround();
        previousFalling = fallingWithMace;
        previousWindCharge = usingWindCharge;
        previousShield = usingShield;
        previousSwinging = swinging;
        hasPosition = true;
    }

    public double getJumpFrequency() { return frequency(jumps); }
    public double getStrafeLeftFrequency() { return frequency(leftStrafes); }
    public double getStrafeRightFrequency() { return frequency(rightStrafes); }
    public double getAttackFrequency() { return frequency(attacks); }
    public double getSlamAttemptFrequency() { return frequency(slamAttempts); }
    public double getWindChargeFrequency() { return frequency(windChargeUses); }
    public double getShieldFrequency() { return frequency(shieldUses); }
    public double getAverageEngagementDistance() { return samples == 0 ? 0 : distanceTotal / samples; }
    public double getAggressiveBehaviorScore() { return aggressiveScore; }

    private double frequency(long count) {
        return samples == 0 ? 0 : (double) count / samples;
    }
}
