package net.katch0420.macebot.main.macebot.control;

import net.katch0420.macebot.main.macebot.bot.BotSlots;
import net.katch0420.macebot.main.settings.server.Settings;
import net.minecraft.block.BlockState;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.InteractionEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import static net.katch0420.macebot.main.macebot.control.ActionContext.macebotCtx;
import static net.katch0420.macebot.main.macebot.control.ActionContext.opponentCtx;

/**
 * Low-level bot operations — movement, combat, item use, etc.
 *
 * This class contains ONLY primitives. All multistep action sequences have
 * moved to their own BotAction subclasses (see actions/ package).
 * Action classes call these methods; they don't call each other.
 */
public class ActionManager {

    public ServerPlayerEntity macebot;
    public ServerPlayerEntity opponent;
    public PlayerInventory    macebotInv;

    public boolean attackMarked;
    public boolean elytraEquipped;

    private int blockHitDelay;
    private BlockPos currentBlock;
    private int curBlockDamageMP;

    // ── Update ────────────────────────────────────────────────────────────────

    public void update() {
        if (macebot    != macebotCtx.serverPlayer)    macebot    = macebotCtx.serverPlayer;
        if (opponent   != opponentCtx.serverPlayer)   opponent   = opponentCtx.serverPlayer;
        if (macebotInv != macebotCtx.getInventory())  macebotInv = macebotCtx.getInventory();
    }

    // ── Movement ──────────────────────────────────────────────────────────────

    public void setMovementSpeed(float forward, float sideways) {
        macebot.forwardSpeed  = forward;
        macebot.sidewaysSpeed = sideways;
    }

    public void setSprint(boolean sprint) {
        macebot.setSprinting(sprint);
        if (sprint && macebot.isSneaking()) macebot.setSneaking(false);
    }

    public void setSneak(boolean sneak) {
        macebot.setSneaking(sneak);
        if (sneak && macebot.isSprinting()) macebot.setSprinting(false);
    }

    public void resetAllMovements() {
        setSprint(false);
        setSneak(false);
        setMovementSpeed(0, 0);
    }

    // ── Inventory ─────────────────────────────────────────────────────────────

    public void setSelectedSlot(int slot) {
        macebotInv.selectedSlot = slot;
    }

    /**
     * Swap between elytra (chest slot) and whatever is in the elytra hotbar slot.
     * Must have elytra slot selected first.
     */
    public void swapElytra() {
        if (macebotInv.selectedSlot == BotSlots.ELYTRA) {
            ItemStack chestArmor = macebotInv.getArmorStack(2);
            ItemStack heldStack  = macebotInv.getMainHandStack();
            elytraEquipped = !chestArmor.isOf(Items.ELYTRA);
            macebotInv.armor.set(2, heldStack);
            macebotCtx.mainHand.setStack(chestArmor);
        }
    }

    public void equipElytra(){
        if (!elytraEquipped) {
            setSelectedSlot(BotSlots.ELYTRA);
            swapElytra();
        }
    }

    public void unequipElytra() {
        if (elytraEquipped) {
            setSelectedSlot(BotSlots.ELYTRA);
            swapElytra();
        }
    }

    // ── Item use ──────────────────────────────────────────────────────────────

    /**
     * Use the item in main or offhand (whichever isn't on cooldown).
     * @return true if an item was used
     */
    public boolean use(){
        if (macebot.isUsingItem())
        {
            return true;
        }
        HitResult hit = macebotCtx.getTarget();
        for (Hand hand : Hand.values())
        {
            switch (hit.getType())
            {
                case BLOCK:
                {
                    macebot.updateLastActionTime();
                    ServerWorld world = macebot.getServerWorld();
                    BlockHitResult blockHit = (BlockHitResult) hit;
                    BlockPos pos = blockHit.getBlockPos();
                    Direction side = blockHit.getSide();
                    if (pos.getY() < macebot.getWorld().getHeight() - (side == Direction.UP ? 1 : 0) && world.canPlayerModifyAt(macebot, pos))
                    {
                        ActionResult result = macebot.interactionManager.interactBlock(macebot, world, macebot.getStackInHand(hand), hand, blockHit);
                    }
                    break;
                }
                case ENTITY:
                {
                    macebot.updateLastActionTime();
                    EntityHitResult entityHit = (EntityHitResult) hit;
                    Entity entity = entityHit.getEntity();
                    boolean handWasEmpty = macebot.getStackInHand(hand).isEmpty();
                    boolean itemFrameEmpty = (entity instanceof ItemFrameEntity) && ((ItemFrameEntity) entity).getHeldItemStack().isEmpty();
                    Vec3d relativeHitPos = entityHit.getPos().subtract(entity.getX(), entity.getY(), entity.getZ());
                    if (entity.interactAt(macebot, relativeHitPos, hand).isAccepted())
                    {
                        return true;
                    }
                    // fix for SS itemframe always returns CONSUME even if no action is performed
                    if (macebot.interact(entity, hand).isAccepted() && !(handWasEmpty && itemFrameEmpty))
                    {
                        return true;
                    }
                    break;
                }
            }
            ItemStack handItem = macebot.getStackInHand(hand);
            if (macebot.interactionManager.interactItem(macebot, macebot.getWorld(), handItem, hand).isAccepted())
            {
                return true;
            }
        }
        return false;
    }

    public void startBlocking() {
        setSelectedSlot(BotSlots.SHIELD);
        use();
    }

    public void stopBlocking() {
        macebot.stopUsingItem();
    }


    public boolean attack(){
        return attack(0);
    }
    public boolean attack(int millis){
        HitResult hit = macebotCtx.getTarget();
        switch (hit.getType()) {
            case ENTITY: {
                EntityHitResult entityHit = (EntityHitResult) hit;
                if(Settings.isMacebotCanDoAttack()) {
                    if (millis <= 0) {
                        macebot.attack(entityHit.getEntity());
                    } else {
                        new Thread(() -> {
                            try {
                                Thread.sleep(millis);
                                macebot.attack(entityHit.getEntity());
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        }).start();
                    }
                }
                macebot.swingHand(Hand.MAIN_HAND);
                macebot.resetLastAttackedTicks();
                attackMarked = false;
                return true;
            }
            case BLOCK: {
                if (blockHitDelay > 0)
                {
                    blockHitDelay--;
                    return false;
                }
                BlockHitResult blockHit = (BlockHitResult) hit;
                BlockPos pos = blockHit.getBlockPos();
                Direction side = blockHit.getSide();
                if (macebot.isBlockBreakingRestricted(macebot.getWorld(), pos, macebot.interactionManager.getGameMode())) return false;
                if (currentBlock != null && macebot.getWorld().getBlockState(currentBlock).isAir())
                {
                    currentBlock = null;
                    return false;
                }
                BlockState state = macebot.getWorld().getBlockState(pos);
                boolean blockBroken = false;
                if (macebot.interactionManager.getGameMode().isCreative())
                {
                    macebot.interactionManager.processBlockBreakingAction(pos, PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, side, macebot.getWorld().getHeight(), -1);
                    blockHitDelay = 5;
                    blockBroken = true;
                }
                else  if (currentBlock == null || !currentBlock.equals(pos))
                {
                    if (currentBlock != null)
                    {
                        macebot.interactionManager.processBlockBreakingAction(currentBlock, PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, side, macebot.getWorld().getHeight(), -1);
                    }
                    macebot.interactionManager.processBlockBreakingAction(pos, PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, side, macebot.getWorld().getHeight(), -1);
                    boolean notAir = !state.isAir();
                    if (notAir && curBlockDamageMP == 0)
                    {
                        state.onBlockBreakStart(macebot.getWorld(), pos, macebot);
                    }
                    if (notAir && state.calcBlockBreakingDelta(macebot, macebot.getWorld(), pos) >= 1)
                    {
                        currentBlock = null;
                        //instamine??
                        blockBroken = true;
                    }
                    else
                    {
                        currentBlock = pos;
                        curBlockDamageMP = 0;
                    }
                }
                else
                {
                    curBlockDamageMP += (int) state.calcBlockBreakingDelta(macebot, macebot.getWorld(), pos);
                    if (curBlockDamageMP >= 1)
                    {
                        macebot.interactionManager.processBlockBreakingAction(pos, PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, side, macebot.getWorld().getHeight(), -1);
                        currentBlock = null;
                        blockHitDelay = 5;
                        blockBroken = true;
                    }
                    macebot.getWorld().setBlockBreakingInfo(-1, pos, (int) (curBlockDamageMP * 10));

                }
                macebot.updateLastActionTime();
                macebot.swingHand(Hand.MAIN_HAND);
                return blockBroken;
            }
        }
        return false;
    }

    // ── Locomotion ────────────────────────────────────────────────────────────

    public void doJump() {
        if (macebot.isOnGround()) macebot.jump();
    }

    /**
     * Schedule a jump on a background thread after a short delay.
     * Mimics the human reflex between wind charge shot and jump.
     */
    public void delayJumpInMillis(int millis) {
        int delay = millis > 0 ? millis : 20;
        new Thread(() -> {
            try {
                Thread.sleep(delay);
                doJump();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    // ── Look ──────────────────────────────────────────────────────────────────

    public void lookAtOpponent() {
        if (opponent != null) lookAt(opponent.getCameraPosVec(0.5f));
    }

    public void lookAt(Vec3d position) {
        macebot.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, position);
    }

    public void look(float yaw, float pitch) {
        macebot.setYaw(yaw % 360);
        macebot.setPitch(Math.clamp(pitch, -90, 90));
    }

    public void look(Direction direction) {
        switch (direction) {
            case NORTH -> look(180, 0);
            case SOUTH -> look(0,   0);
            case EAST  -> look(-90, 0);
            case WEST  -> look(90,  0);
            case UP    -> look(macebot.getYaw(), -90);
            case DOWN  -> look(macebot.getYaw(),  90);
        }
    }
}