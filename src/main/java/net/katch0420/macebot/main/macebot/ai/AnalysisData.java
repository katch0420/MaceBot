package net.katch0420.macebot.main.macebot.ai;

import net.katch0420.macebot.main.macebot.bot.PlayerBot;
import net.katch0420.macebot.main.settings.Settings;
import net.katch0420.macebot.main.utils.RayTracer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.HitResult;


public class AnalysisData {
    public Core core;
    public PlayerBot bot;
    public ServerPlayer enemy;

    //Cache
    public float lastProcessedDistance = Float.MAX_VALUE;

    AnalysisData(Core core){
        this.core = core;
        this.bot = core.bot;
        this.enemy = getEnemy();
    }
    public double getKbResistOfEnemy(){return enemy.getAttribute(Attributes.KNOCKBACK_RESISTANCE).getValue();}

    public boolean isOpponentOnCrosshair() {
        return enemy != null && enemy == getCrossHairTarget();
    }

    public Entity getCrossHairTarget(){
        if(!isAvailable()) return null;
        return RayTracer.rayTraceEntity(bot, bot.entityInteractionRange() - getReachDecrement());
    }

    private double getReachDecrement() {
        return (1 - (double) Settings.SKILL.getValue() / 100);
    }

    public boolean isAvailable(){
        return bot != null;
    }

    public boolean aboutToReach(double extraDis) {
        return isOpponentOnCrosshair() || getDistance() <= extraDis + bot.entityInteractionRange();
    }

    public boolean aboutToReachXZ(int extraDis){
        return isOpponentOnCrosshair() || getDistanceXZ() < extraDis + bot.entityInteractionRange();
    }

    public boolean canReachInTicks(int ticks){
        return bot.entityInteractionRange() <= getDistance() -  getTravelDistanceInTicks(ticks);
    }

    private double getTravelDistanceInTicks(int ticks) {
        var dv = bot.getDeltaMovement();
        return ticks * (dv.x * dv.x + dv.y * dv.y + dv.z * dv.z);
    }

    public boolean itemLoaded(){
        return bot.getAttackStrengthScale(0.5f) > 0.9;
    }

    public float getDistance() {
        lastProcessedDistance = isAvailable() ? bot.distanceTo(enemy) : Float.MAX_VALUE;
        return lastProcessedDistance;
    }

    public float getDistanceXZ() {
        if (!isAvailable()) return Float.MAX_VALUE;
        float dx = (float)(bot.getX() - enemy.getX());
        float dz = (float)(bot.getZ() - enemy.getZ());
        return Mth.sqrt(dx * dx + dz * dz);
    }

    public float getDistanceY() {
        return isAvailable() ? (float)(bot.getY() - enemy.getY()) : Float.MAX_VALUE;
    }

    public ServerPlayer getEnemy(){
        return bot.level()
                .players().stream()
                .filter(p -> p != bot && !p.isDeadOrDying() && p.gameMode().isSurvival())
                .min((a, b) -> Float.compare(
                        bot.distanceTo(a),
                        bot.distanceTo(b)))
                .orElse(null);
    }

    public float getHealth()               { return isAvailable() ? bot.getHealth() : 0; }
    public float getFoodLevel()            { return isAvailable() ? bot.getFoodData().getFoodLevel() : 0; }
    public float getAir()                  { return isAvailable() ? bot.getAirSupply() : 0; }
    public float getAttackCooldownProgress(){ return isAvailable() ? bot.getAttackStrengthScale(0.5F) : 0; }

    public HitResult getHitResult() {
        if(!isAvailable()) return null;
        return RayTracer.rayTraceHitResult(bot, bot.entityInteractionRange() - getReachDecrement());

    }

    public double distanceToGround() {
        return RayTracer.getDistanceToGround(bot);
    }

    public float getMaxGlideAngle(){
        var v = bot.getDeltaMovement();
        double hv = Math.sqrt(v.x * v.x + v.z * v.z);
        return (float) Math.toDegrees(Math.atan2(v.y,hv));
    }
}
