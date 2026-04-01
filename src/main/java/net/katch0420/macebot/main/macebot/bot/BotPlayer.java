package net.katch0420.macebot.main.macebot.bot;

import net.katch0420.macebot.main.macebot.control.ActionContext;
import net.katch0420.macebot.main.macebot.control.PlayerResolver;
import net.katch0420.macebot.main.utils.RayTracer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import static net.katch0420.macebot.main.MaceBot.server;

/**
 * Tracks one participant in the fight — either the bot or its opponent.
 * <p>
 * Replaces the old Player inner class. No hardcoded `isMaceBot` boolean —
 * a PlayerResolver decides how to find the ServerPlayerEntity each update.
 */
public class BotPlayer {

    // Set by ActionContext so BotPlayers can reference each other
    public BotPlayer opponent;

    // Back-reference to ActionContext so resolvers can query bot position etc.
    ActionContext ctx;

    public ServerPlayerEntity serverPlayer;

    public BotStack mainHand;
    public BotStack offHand;

    /** How often (in ticks) to re-resolve the tracked player. */
    private final int resolveInterval;
    private final PlayerResolver resolver;

    // ── Construction ──────────────────────────────────────────────────────────

    public BotPlayer(PlayerResolver resolver, int resolveInterval) {
        this.resolver        = resolver;
        this.resolveInterval = resolveInterval;
    }

    /** Called once after ctx and opponent are linked. */
    public void init(ActionContext ctx) {
        this.ctx  = ctx;
        mainHand  = new BotStack(this, Hand.MAIN_HAND);
        offHand   = new BotStack(this, Hand.OFF_HAND);
        resolvePlayer();
    }

    // ── Update ────────────────────────────────────────────────────────────────

    /**
     * @return true if this player is available (bot can act)
     */
    public boolean update() {
        if (!isAvailable() || server.getTicks() % resolveInterval == 0) {
            resolvePlayer();
        }
        if (isAvailable()) {
            mainHand.update();
            offHand.update();
        }
        return isAvailable();
    }

    private void resolvePlayer() {
        serverPlayer = resolver.resolve(ctx);
    }

    // ── Availability ──────────────────────────────────────────────────────────

    public boolean isAvailable()       { return serverPlayer != null; }
    public boolean isTargetAvailable() { return getTargetEntity() != null; }

    public boolean isTargetOnCrossHair() {
        return isAvailable() && RayTracer.rayTraceEntity(serverPlayer, 3.5) != null;
    }

    // ── Combat queries ────────────────────────────────────────────────────────

    public boolean isHoldingWeapon() { return mainHand.isWeapon() || offHand.isWeapon(); }
    public boolean isMeleeWeapon()   { return mainHand.isMelee()  || offHand.isMelee();  }
    public boolean isRangedWeapon()  { return mainHand.isRanged() || offHand.isRanged(); }

    public boolean isRangedWeaponCanShoot() {
        // Crossbow — check if charged
        if (mainHand.itemStack.isOf(Items.CROSSBOW) || offHand.itemStack.isOf(Items.CROSSBOW)) {
            return CrossbowItem.isCharged(mainHand.itemStack)
                    || CrossbowItem.isCharged(offHand.itemStack);
        }
        // Bow / trident — check if currently drawing
        UseStatus s = getUseStatus();
        return s == UseStatus.LOADING_BOW || s == UseStatus.LOADING_TRIDENT;
    }

    // ── Use status ────────────────────────────────────────────────────────────

    public UseStatus getUseStatus() {
        if (!isAvailable() || !serverPlayer.isUsingItem()) return UseStatus.NONE;

        return switch (serverPlayer.getActiveItem().getUseAction()) {
            case EAT, DRINK -> UseStatus.CONSUMING;
            case BLOCK      -> UseStatus.BLOCKING;
            case CROSSBOW   -> UseStatus.LOADING_CROSSBOW;
            case BOW        -> UseStatus.LOADING_BOW;
            case SPYGLASS   -> UseStatus.SPYGLASS;
            case SPEAR      -> UseStatus.LOADING_TRIDENT;
            default         -> UseStatus.NONE;
        };
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public ServerPlayerEntity getPlayer()    { return serverPlayer; }
    public BotPlayer          getOpponent()  { return opponent; }
    public BotStack           getMainHand()  { return mainHand; }
    public BotStack           getOffHand()   { return offHand; }

    public PlayerInventory getInventory() {
        return isAvailable() ? serverPlayer.getInventory() : null;
    }

    public ServerWorld getServerWorld() {
        return isAvailable() ? serverPlayer.getServerWorld() : null;
    }

    public World getWorld() {
        return isAvailable() ? serverPlayer.getWorld() : null;
    }

    public HitResult getTarget() {return RayTracer.rayTraceHitResult(serverPlayer, 0.5f, false, serverPlayer.getEntityInteractionRange());}

    public Entity getTargetEntity() {
        return isAvailable() ? RayTracer.rayTraceEntity(serverPlayer) : null;
    }

    public BlockPos getTargetBlock() {
        return isAvailable() ? RayTracer.rayTraceBlock(serverPlayer, false).getBlockPos() : null;
    }

    public BlockPos getTargetFluid() {
        return isAvailable() ? RayTracer.rayTraceBlock(serverPlayer, true).getBlockPos() : null;
    }

    // ── Stats ─────────────────────────────────────────────────────────────────

    public float getHealth()               { return isAvailable() ? serverPlayer.getHealth() : 0; }
    public float getFoodLevel()            { return isAvailable() ? serverPlayer.getHungerManager().getFoodLevel() : 0; }
    public float getAir()                  { return isAvailable() ? serverPlayer.getAir() : 0; }
    public float getAttackCooldownProgress(){ return isAvailable() ? serverPlayer.getAttackCooldownProgress(0.5F) : 0; }

    public float getDistance(Entity entity) {
        return isAvailable() ? serverPlayer.distanceTo(entity) : Float.MAX_VALUE;
    }

    public float getDistanceXZ(Entity entity) {
        if (!isAvailable()) return Float.MAX_VALUE;
        float dx = (float)(serverPlayer.getX() - entity.getX());
        float dz = (float)(serverPlayer.getZ() - entity.getZ());
        return MathHelper.sqrt(dx * dx + dz * dz);
    }

    public float getDistanceY(Entity entity) {
        return isAvailable() ? (float)(serverPlayer.getY() - entity.getY()) : Float.MAX_VALUE;
    }

    // ── Enum ──────────────────────────────────────────────────────────────────

    public enum UseStatus {
        CONSUMING, BLOCKING, SPYGLASS,
        LOADING_BOW, LOADING_CROSSBOW, LOADING_TRIDENT,
        NONE
    }
}