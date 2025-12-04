package net.katch0420.macebot.ai;

import net.katch0420.macebot.utils.RayTracer;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActionManager {
    public ServerPlayerEntity player;
    public PlayerInventory inventory;
    private final ServerWorld serverWorld;
    private final World world;

    public ServerPlayerEntity nearestPlayer;
    public Entity targetedEntity;

    private final int swordSlot;
    private final int enderPearlSlot;
    private final int shieldSlot;
    private final int breachMaceSlot;
    private final int densityMaceSlot;
    private final int elytraSlot;
    private final int windChargeSlot;
    private final int foodSlot;
    private final int axeSlot;

    private int tickTimer;
    private int executeStep = 1;
    public double distanceToNearbyPlayer;

    public boolean nearestPlayerIsNull;
    public boolean targetedEntityIsNull;
    public boolean attackMarked;
    public boolean elytraEquipped = false;


    private static final Map<Integer, Runnable> regularLaunch = new HashMap<>();
    private static final Map<Integer, Runnable> elytraLaunch = new HashMap<>();

    public ActionManager(ServerPlayerEntity player){
        this.player = player;
        inventory = player.getInventory();
        serverWorld = player.getServerWorld();
        world = player.getServerWorld();

        swordSlot = 0;
        enderPearlSlot = 1;
        shieldSlot = 2;
        breachMaceSlot = 3;
        densityMaceSlot = 4;
        elytraSlot = 5;
        windChargeSlot = 6;
        foodSlot = 7;
        axeSlot = 8;
        update();
        loadHashMaps();
    }
    //Core
    public void update(){
        //CoolDowns & Timers Based on Ticks
        if(tickTimer > 0) tickTimer--;

        //Player Entity Updates
        nearestPlayer = getNearestPlayer();
        nearestPlayerIsNull = nearestPlayer == null;
        distanceToNearbyPlayer = nearestPlayerIsNull ? 100 : player.distanceTo(nearestPlayer);

        targetedEntity = RayTracer.rayTraceEntity(player);
        targetedEntityIsNull = targetedEntity == null;
    }
    //Packed Actions
    public Status eat(){
        if(executeStep == 1){
            resetValues();
            unequipElytra();
            inventory.selectedSlot = foodSlot;
            boolean bl = use();
            if(!bl){
                System.out.println("eat.fail.use");
                resetValues();
                return Status.FAIL;
            }
            tickTimer = player.getItemUseTime();
            executeStep++;
            System.out.println(executeStep);
            return Status.PASS;
        }
        if(tickTimer <= 0 && !player.isUsingItem()){
            System.out.println("eat.success");
            resetValues();
            return Status.SUCCESS;
        }
        System.out.println("eat.pass");
        return Status.PASS;
    }
    public Status elytraLaunch(){
        if(commonCancelers()){
            resetValues();
            unequipElytra();
            return Status.FAIL;
        }
        if(executeStep == 1){
            inventory.selectedSlot = elytraSlot;
            player.forwardSpeed = 1;
            setSprint(true);
            executeStep++;
            return Status.PASS;
        }
        if(executeStep == 2){
            swapElytra();
            doJump();
            look(player.getYaw(), 40F);
            executeStep++;
            return Status.PASS;
        }
        if(executeStep == 3){
            if(!player.isOnGround() && Math.abs(player.getVelocity().y) < 0.05){
                player.startFallFlying();
                inventory.selectedSlot = windChargeSlot;
                executeStep++;
                return Status.PASS;
            } else if (player.isOnGround()){
                resetValues();
                unequipElytra();
                return Status.FAIL;
            }
        }
        if(executeStep == 4){
            if(RayTracer.getDistanceToGround(player) < 0.25){
                look(Direction.DOWN);
                use();
                executeStep++;
                delayJumpInMillis(20);
                return Status.PASS;
            }
        }
        if(executeStep == 5){
            look(player.getYaw(), -30F);
            doJump();
            executeStep++;
            return Status.PASS;
        }
        if(executeStep == 6){
            player.startFallFlying();
            inventory.selectedSlot = elytraSlot;
            resetValues();
            return Status.SUCCESS;
        }
        return Status.PASS;
    }
    public Status elytraAttack(){
        if(commonCancelers()){
            resetValues();
            unequipElytra();
            return Status.FAIL;
        }
        if(player.isOnGround()){
            if(executeStep > 5){
                resetValues();
                unequipElytra();
                return Status.FAIL;
            } else {
                executeStep++;
            }
        }
        if(player.getVelocity().y > 0){
            return Status.PASS;
        }
        if(targetedEntityIsNull){
            lookAt(nearestPlayer.getCameraPosVec(0.5F));
        } else {
            unequipElytra();
            return Status.SUCCESS;
        }
        return Status.PASS;
    }
    public Status regularLaunch(boolean sprint){
        if(commonCancelers()){
            resetValues();
            return Status.FAIL;
        }
        if(executeStep == 1){
            player.forwardSpeed = sprint ? 1 : 0;
            setSprint(sprint);
        }
        if(executeStep <= regularLaunch.size()){
            if(executeStep == 2) look(player.getYaw(),sprint ? 75.0F : 89.9F);
            regularLaunch.get(executeStep).run();
            if(executeStep == regularLaunch.size()){
                resetValues();
                return Status.SUCCESS;
            } else {
                executeStep++;
            }
        }
        return Status.PASS;
    }
    public Status maceHit(boolean density){
        //checks whether cancel or not
        if(commonCancelers() || player.isOnGround()){
            resetValues();
            return Status.FAIL;
        }
        lookAt(nearestPlayer.getCameraPosVec(0.5F));
        if(player.fallDistance > 1){
            inventory.selectedSlot = density? densityMaceSlot : breachMaceSlot;
            attack();
        }
        if(!attackMarked && !player.isOnGround()){
            attackMarked = true;
        }
        return Status.PASS;
    }
    public Status critHit(int slot){
        if(commonCancelers()){
            resetValues();
            return Status.FAIL;
        }
        lookAt(nearestPlayer.getCameraPosVec(0.5F));
        if(executeStep == 1){
            inventory.selectedSlot = slot;
            player.forwardSpeed = 1;
            setSprint(true);
            executeStep++;
            return Status.PASS;
        }
        if(distanceToNearbyPlayer < 6 && executeStep == 2){
            doJump();
            executeStep++;
        }
        if(player.isOnGround() && executeStep > 2){
            resetValues();
            return Status.FAIL;
        }
        if(player.fallDistance > 0 && player.getAttackCooldownProgress(0.5F) > 0.9 && !targetedEntityIsNull){
            attackMarked = true;
        }
        if(attackMarked){
            setSprint(false);
            attack();
            if(!attackMarked){
                resetValues();
                return Status.SUCCESS;
            } else {
                player.forwardSpeed =  1;
                setSprint(true);
            }
        }
        return Status.PASS;
    }

    //Utils
    private ServerPlayerEntity getNearestPlayer(){
        List<ServerPlayerEntity> a = serverWorld.getPlayers();
        ServerPlayerEntity b = null;
        float c = 100;
        for(ServerPlayerEntity d : a){
            if(player.distanceTo(d) < c){
                if(d != player) {
                    c = player.distanceTo(d);
                    b = d;
                }
            }
        }
        return b;
    }
    private boolean commonCancelers(){
        return nearestPlayerIsNull;
    }
    public void resetValues(){
        executeStep = 1;
        tickTimer = 0;
    }
    public void unequipElytra(){
        if(elytraEquipped){
            inventory.selectedSlot = elytraSlot;
            swapElytra();
        }
    }
    public void resetAllMovements(){
        setSprint(false);
        setSneak(false);
        player.forwardSpeed = 0;
        player.sidewaysSpeed = 0;
    }
    private void loadHashMaps(){

        regularLaunch.put(1, ()-> lookAt(nearestPlayer.getCameraPosVec(0.5F)));
        regularLaunch.put(2, ()-> inventory.selectedSlot = windChargeSlot);
        regularLaunch.put(3, ()-> {use();delayJumpInMillis(20);});
        regularLaunch.put(4, ()-> lookAt(nearestPlayer.getCameraPosVec(0.5F)));

        elytraLaunch.put(1, ()-> {inventory.selectedSlot = elytraSlot; player.forwardSpeed = 1; setSprint(true);});
        elytraLaunch.put(2, ()-> {swapElytra();doJump();});
        elytraLaunch.put(3, ()-> look(player.getYaw(), 40.0F));
        elytraLaunch.put(4, ()-> {player.startFallFlying();tickTimer = 10;});
        elytraLaunch.put(5, ()-> {look(player.getYaw(), 89.9f);inventory.selectedSlot = windChargeSlot;});
        elytraLaunch.put(6, ()-> {use();delayJumpInMillis(20);});
        elytraLaunch.put(7, ()-> {look(player.getYaw(), -35.0F);player.startFallFlying();});
    }
    public enum Status{
        SUCCESS,
        FAIL,
        PASS
    }

    //All Individual Actions (executes in 1 tick mostly)
    public void swapElytra(){
        if(inventory.selectedSlot == elytraSlot) {
            ItemStack a = inventory.getArmorStack(2);
            ItemStack b = inventory.getMainHandStack();
            elytraEquipped = !a.isOf(Items.ELYTRA);
            inventory.armor.set(2, b);
            player.setStackInHand(Hand.MAIN_HAND, a);
        }
    }
    public boolean use(){
        ItemStack mainHandStack = player.getMainHandStack();
        ItemStack offHandStack = player.getOffHandStack();

        if(!player.getItemCooldownManager().isCoolingDown(mainHandStack.getItem())) {
            mainHandStack.use(world, player, Hand.MAIN_HAND);
            return true;
        }
        if(!player.getItemCooldownManager().isCoolingDown(offHandStack.getItem())) {
            offHandStack.use(world, player, Hand.OFF_HAND);
            return true;
        }
        return false;
    }
    public void attack(){
        if(!targetedEntityIsNull && attackMarked) {
            player.attack(targetedEntity);
            player.swingHand(Hand.MAIN_HAND);
            attackMarked = false;
        }
    }
    public void doJump(){
        if (player.isOnGround()) {
            player.jump();
        }
    }
    public void setSprint(boolean bl){
        player.setSprinting(bl);
        if(player.isSneaking() && player.isSprinting()){
            player.setSneaking(false);
        }
    }
    public void setSneak(boolean bl){
        player.setSneaking(bl);
        if(player.isSprinting() && player.isSneaking()){
            player.setSprinting(false);
        }
    }
    public void lookAt(Vec3d position) {
        player.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, position);

    }
    public void look(float yaw, float pitch){
        player.setYaw(yaw % 360);
        player.setPitch(Math.clamp(pitch, -90, 90));
    }
    public void look(Direction direction) {
        switch (direction)
        {
            case NORTH -> look(180, 0);
            case SOUTH -> look(0, 0);
            case EAST  -> look(-90, 0);
            case WEST  -> look(90, 0);
            case UP    -> look(player.getYaw(), -90);
            case DOWN  -> look(player.getYaw(), 90);
        }
    }
    public void delayJumpInMillis(int millis){
        if(millis == 0){
            millis = 20;
        }
        int finalMillis = millis;
        new Thread(() -> {
            try {
                Thread.sleep(finalMillis);
                doJump();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }
}
