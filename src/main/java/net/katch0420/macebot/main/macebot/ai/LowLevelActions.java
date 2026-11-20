package net.katch0420.macebot.main.macebot.ai;

import net.katch0420.macebot.main.macebot.bot.BotInput;
import net.katch0420.macebot.main.macebot.bot.BotSlots;
import net.katch0420.macebot.main.macebot.bot.PlayerBot;
import net.katch0420.macebot.main.settings.Settings;
//? if >=1.21.11 {
//?} else
//import net.minecraft.world.entity.projectile.windcharge.WindCharge;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.player.Inventory;

public class LowLevelActions {

    public final Core core;
    public BotInput input;

    public Skill skill;

    public PlayerBot bot;
    public ServerPlayer enemy;

    public boolean attackMarked;
    public boolean elytraEquipped;

    private int blockHitDelay;
    private BlockPos currentBlock;
    private int curBlockDamageMP;
    private int delayedJumpTicks = -1;

    LowLevelActions(Core core){
        this.core = core;
        bot = core.bot;
        enemy = core.enemy;
        input = bot.input;
        this.skill = core.skill;
    }
    public void setBot(PlayerBot bot) {this.bot = bot;}

    public void inputMovement(float forward, float sideways) {
        input.moveForward(forward > 0);
        input.moveBackward(forward < 0);
        input.strafeLeft(sideways < 0);
        input.strafeRight(sideways > 0);
    }
    public void setSneak(boolean sneak) {
        input.sneak(sneak);
    }
    public void resetAllMovements() {
        input.sneak(false);
        inputMovement(0, 0);
    }
    public void selectSlot(int slot) {
        //? if >=1.21.5 {
        inv().setSelectedSlot(slot);
        //?} else
        //macebotInv.selected = slot;
    }

    private Inventory inv() {
        return bot.getInventory();
    }

    public int selectedSlot() {
        //? if >=1.21.5 {
        return inv().getSelectedSlot();
        //?} else
        //return macebotInv.selected;
    }
    public void swapElytra() {
        if (selectedSlot() == BotSlots.ELYTRA) {
            ItemStack chestStack = inv().getItem(38);
            ItemStack handStack = bot.getMainHandItem();
            elytraEquipped = !chestStack.is(Items.ELYTRA);
            inv().setItem(38, handStack);
        }
    }

    public void equipElytra() {
        if (!elytraEquipped) {
            selectSlot(BotSlots.ELYTRA);
            swapElytra();
        }
    }

    public void unequipElytra() {
        if (elytraEquipped) {
            selectSlot(BotSlots.ELYTRA);
            swapElytra();
        }
    }

    public boolean use() {
        if (bot.isUsingItem()) {
            return true;
        }
        HitResult hit = core.analysisData.getHitResult();
        for (InteractionHand hand : InteractionHand.values()) {
            switch (hit.getType()) {
                case BLOCK: {
                    bot.resetLastActionTime();
                    ServerLevel world = bot
                            //? >=1.21.6 {
                            .level();
                    //?} else
                    //.serverLevel();
                    BlockHitResult blockHit = (BlockHitResult) hit;
                    BlockPos pos = blockHit.getBlockPos();
                    Direction side = blockHit.getDirection();
                    if (pos.getY() < bot.level().getHeight() - (side == Direction.UP ? 1 : 0) && world.mayInteract(bot, pos)) {
                        InteractionResult result = bot.gameMode.useItemOn(bot, world, bot.getItemInHand(hand), hand, blockHit);
                    }
                    break;
                }
                case ENTITY: {
                    bot.resetLastActionTime();
                    EntityHitResult entityHit = (EntityHitResult) hit;
                    Entity entity = entityHit.getEntity();
                    boolean handWasEmpty = bot.getItemInHand(hand).isEmpty();
                    boolean itemFrameEmpty = (entity instanceof ItemFrame) && ((ItemFrame) entity).getItem().isEmpty();
                    Vec3 relativeHitPos = entityHit.getLocation().subtract(entity.getX(), entity.getY(), entity.getZ());
                    //? if <=1.21.11 {
                    if (entity.interactAt(bot, relativeHitPos, hand).consumesAction()) {
                        return true;
                    }
                    //?} else {
                    /*if (entity.interact(bot, hand, relativeHitPos).consumesAction()) {
                        return true;
                    }
                    *///?}

                    //? if <=1.21.11 {
                    if (bot.interactOn(entity, hand).consumesAction() && !(handWasEmpty && itemFrameEmpty)) return true;
                    //?} else
                    //if (bot.interactOn(entity, hand, relativeHitPos).consumesAction() && !(handWasEmpty && itemFrameEmpty)) return true;
                    break;
                }
            }
            ItemStack handItem = bot.getItemInHand(hand);
            if (bot.gameMode.useItem(bot, bot.level(), handItem, hand).consumesAction()) {
                return true;
            }
        }
        return false;
    }

    public void startBlocking() {
        selectSlot(BotSlots.SHIELD);
        use();
    }
    public void stopBlocking() {
        bot.stopUsingItem();
    }


    public boolean attack() {
        HitResult hit = core.analysisData.getHitResult();
        switch (hit.getType()) {
            case ENTITY: {
                EntityHitResult entityHit = (EntityHitResult) hit;
                if (Settings.MACEBOT_CAN_DO_ATTACK.getValue()) bot.attack(entityHit.getEntity());
                bot.swing(InteractionHand.MAIN_HAND);
                attackMarked = false;
                return true;
            }
            case BLOCK: {
                if (blockHitDelay > 0) {
                    blockHitDelay--;
                    return false;
                }
                BlockHitResult blockHit = (BlockHitResult) hit;
                BlockPos pos = blockHit.getBlockPos();
                Direction side = blockHit.getDirection();
                if (bot.blockActionRestricted(bot.level(), pos, bot.gameMode.getGameModeForPlayer()))
                    return false;
                if (currentBlock != null && bot.level().getBlockState(currentBlock).isAir()) {
                    currentBlock = null;
                    return false;
                }
                BlockState state = bot.level().getBlockState(pos);
                boolean blockBroken = false;
                if (bot.gameMode.getGameModeForPlayer().isCreative()) {
                    bot.gameMode.handleBlockBreakAction(pos, ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, side, bot.level().getHeight(), -1);
                    blockHitDelay = 5;
                    blockBroken = true;
                } else if (currentBlock == null || !currentBlock.equals(pos)) {
                    if (currentBlock != null) {
                        bot.gameMode.handleBlockBreakAction(currentBlock, ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK, side, bot.level().getHeight(), -1);
                    }
                    bot.gameMode.handleBlockBreakAction(pos, ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, side, bot.level().getHeight(), -1);
                    boolean notAir = !state.isAir();
                    if (notAir && curBlockDamageMP == 0) {
                        state.attack(bot.level(), pos, bot);
                    }
                    if (notAir && state.getDestroyProgress(bot, bot.level(), pos) >= 1) {
                        currentBlock = null;
                        blockBroken = true;
                    } else {
                        currentBlock = pos;
                        curBlockDamageMP = 0;
                    }
                } else {
                    curBlockDamageMP += (int) state.getDestroyProgress(bot, bot.level(), pos);
                    if (curBlockDamageMP >= 1) {
                        bot.gameMode.handleBlockBreakAction(pos, ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, side, bot.level().getHeight(), -1);
                        currentBlock = null;
                        blockHitDelay = 5;
                        blockBroken = true;
                    }
                    bot.level().destroyBlockProgress(-1, pos, (int) (curBlockDamageMP * 10));

                }
                bot.resetLastActionTime();
                bot.swing(InteractionHand.MAIN_HAND);
                return blockBroken;
            }
        }
        return false;
    }

    public void jump() {
        input.jump();
    }

    public void delayJumpsInTicks(int ticks) {
        if (delayedJumpTicks >= 0) return;
        delayedJumpTicks = ticks;
    }

    public void lookAtOpponent() {
        if(!core.isAlone()) lookAt(enemy.getEyePosition());
    }

    public void lookAt(Vec3 position) {
        bot.lookAt(EntityAnchorArgument.Anchor.EYES, position);
    }

    public void look(float yaw, float pitch) {
        bot.setYRot(yaw % 360);
        bot.setXRot(Math.clamp(pitch, -90, 90));
    }

    public void look(Direction direction) {
        switch (direction) {
            case NORTH -> look(180, 0);
            case SOUTH -> look(0, 0);
            case EAST -> look(-90, 0);
            case WEST -> look(90, 0);
            case UP -> look(bot.getYRot(), -90);
            case DOWN -> look(bot.getYRot(), 90);
        }
    }

    public float strafe(){
        return strafe(0);
    }

    public float strafe(float s) {
        if(s != 0) return s;
        if(!Settings.MACEBOT_CAN_DO_STRAFE.getValue()) return 0;
        if(skill.getSkillFloat() > 0.4 && Math.random() + 0.05 <= skill.getSkillFloat()) {
            boolean b = Math.random() > 0.5;
            return b ? 1 : -1;
        }
        return 0;
    }

    public boolean grapple(){
        return grapple(true);
    }

    public boolean grapple(boolean may){
        boolean bl = (Math.random() <= skill.getSkillFloat() || !may) && Settings.MACEBOT_CAN_GRAPPLING.getValue() && skill.getSkillFloat() > 0.4;
        if(bl){
            this.selectSlot(BotSlots.ENDER_PEARL);
            this.lookAtOpponent();
            this.use();
        }
        return bl;
    }
}