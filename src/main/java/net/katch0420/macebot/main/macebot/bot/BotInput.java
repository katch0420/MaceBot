package net.katch0420.macebot.main.macebot.bot;

import net.minecraft.world.phys.Vec2;

/**
 * Mirrors vanilla's client-side net.minecraft.client.input.Input struct —
 * held movement intent, the server-side equivalent of WASD/jump key state.
 * <p>
 * Actions/Controller should only ever write raw movement intent here —
 * never touch forwardSpeed/sidewaysSpeed on the entity directly.
 * from this every tick, in the same spot ClientPlayerEntity#tickNewAi()
 * does for a real client — right before LivingEntity's own unconditional
 * *0.98F decay and the travel() call, both later in the same
 * tickMovement(). Without that per-tick refresh, plain PlayerEntity never
 * touches these fields on its own (that override is client-only), so a
 * value set once here would otherwise erode 2% every tick with nothing to
 * counteract it — same as if the raw fields were set directly.
 * <p>
 * Sneak/sprint dampening still happens exactly where it always did — inside
 * PlayerBot#travel(), reading the entity's own isSneaking()/isPressingSprint()/
 * isUsingItem() state directly. Those are plain flags, not decaying fields,
 * so they don't need to be routed through this input object at all; only
 * the continuously-decaying forwardSpeed/sidewaysSpeed pair did.
 */
public class BotInput {

    boolean W,A,S,D,SHIFT,CTRL,SPACE;
    int jumpOnce;

    public boolean isMovingForward() {
        return W;
    }

    public void moveForward(boolean move) {
        W = move;
    }

    public boolean isStrafingLeft() {
        return A;
    }

    public void strafeLeft(boolean strafe) {
        A = strafe;
    }

    public boolean isMovingBackward() {
        return S;
    }

    public void moveBackward(boolean move) {
        S = move;
    }

    public boolean isStrafingRight() {
        return D;
    }

    public void strafeRight(boolean strafe) {
        D = strafe;
    }

    public boolean isSneaking() {
        return SHIFT;
    }

    public void sneak(boolean shift) {
        this.SHIFT = shift;
    }

    public boolean isPressingSprint() {
        return true; //Indicating Toggle Sprint
    }

    public void sprint(boolean sprint) {
        this.CTRL = sprint;
    }

    public boolean isJumping() {
        return SPACE;
    }

    public void jump(){
        this.jumpOnce = 1;
    }

    public void jump(boolean jump) {
        this.SPACE = jump;
    }

    public float movementSideways;
    public float movementForward;
    public boolean pressingForward;
    public boolean pressingBack;
    public boolean pressingLeft;
    public boolean pressingRight;
    public boolean jumping;
    public boolean sneaking;

    public void tick(boolean slowDown, float slowDownFactor) {
        this.pressingForward = this.isMovingForward();
        this.pressingBack = this.isMovingBackward();
        this.pressingLeft = this.isStrafingLeft();
        this.pressingRight = this.isStrafingRight();
        this.movementForward = getMovementMultiplier(this.pressingForward, this.pressingBack);
        this.movementSideways = getMovementMultiplier(this.pressingLeft, this.pressingRight);
        this.jumping = this.isJumping() || jumpOnce-- > 0;
        this.sneaking = this.isSneaking();
        if (slowDown) {
            this.movementSideways *= slowDownFactor;
            this.movementForward *= slowDownFactor;
        }
    }

    public Vec2 getMovementInput() {
        return new Vec2(this.movementSideways, this.movementForward);
    }

    public boolean hasForwardMovement() {
        return this.movementForward > 1.0E-5F;
    }

    private static float getMovementMultiplier(boolean positive, boolean negative) {
        if (positive == negative) {
            return 0.0F;
        } else {
            return positive ? 1.0F : -1.0F;
        }
    }

    public boolean hasMovement() {
        return movementForward != 0 || movementSideways != 0;
    }

    public boolean hasSideMovement() {
        return movementSideways != 0;
    }
}