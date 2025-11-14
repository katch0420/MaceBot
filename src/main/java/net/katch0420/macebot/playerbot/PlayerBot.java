package net.katch0420.macebot.playerbot;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.katch0420.macebot.MaceBot;
import net.minecraft.block.BlockState;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.*;

public class PlayerBot extends ServerPlayerEntity {
    ///player bot main vars
    public static ServerPlayerEntity player;
    public static ServerPlayerEntity commandSrc;
    public static MinecraftServer minecraftServer;
    public static final Logger LOGGER = MaceBot.LOGGER;
    public Runnable fixStartingPos = () -> {};
    public static PlayerInventory inventory;
    public static boolean logged;

    public static void createBot(MinecraftServer server, ServerWorld world, BlockPos blockPos, ServerCommandSource source){
        if(!logged) {
            GameProfile gameProfile = new GameProfile(UUID.fromString("bf6e5d99-812f-4d93-8172-7cb97db14567"), "MaceBot");
            PlayerBotConnection clientConnection = new PlayerBotConnection(NetworkSide.SERVERBOUND);
            SyncedClientOptions clientOptions = SyncedClientOptions.createDefault();
            ConnectedClientData clientData = new ConnectedClientData(gameProfile, 0, clientOptions, false);
            PlayerBot playerBot = new PlayerBot(server, world, gameProfile, clientOptions);
            player = playerBot;
            inventory = playerBot.getInventory();
            playerBot.fixStartingPos = () -> playerBot.move(MovementType.PLAYER, new Vec3d(blockPos.getX(), blockPos.getY(), blockPos.getZ()));
            server.getPlayerManager().onPlayerConnect(clientConnection, playerBot, clientData);
            playerBot.teleport(world, blockPos.getX(), blockPos.getY(), blockPos.getZ(), 0, 0);
            playerBot.setInvulnerable(false);
            playerBot.setHealth(20.0F);
            playerBot.unsetRemoved();
            playerBot.changeGameMode(GameMode.SURVIVAL);
            playerBot.getAbilities().flying = false;
            playerBot.getAbilities().allowFlying = false;
            playerBot.getHungerManager().setFoodLevel(20);
            playerBot.getServerWorld().getChunkManager().updatePosition(playerBot);
            playerBot.networkHandler = new PlayerBotNetHandler(server, clientConnection, playerBot, clientData);
            minecraftServer = server;
            commandSrc = source.getPlayer();
            logged = true;
            BotAI.loadActionSequences();
            BotAI.refillInventory("full", "netherite", "MaceBot");
        } else{
            source.sendFeedback(()-> Text.literal("Bot Already Spawned!").withColor(16733525),false);
        }
    }
    public PlayerBot(MinecraftServer server, ServerWorld world, GameProfile profile, SyncedClientOptions clientOptions) {
        super(server, world, profile, clientOptions);
    }
    @Override
    public void tick()
    {
        this.resetPosition();
        this.getServerWorld().getChunkManager().updatePosition(this);
        BotAI.update();
        BotAI.AI();
        super.tick();
        this.playerTick();
    }

    @Override
    protected void dropLoot(DamageSource damageSource, boolean causedByPlayer) {
    }

    @Override
    public void onDeath(DamageSource damageSource) {
        super.onDeath(damageSource);
        this.dead = false;
        this.setHealth(20f);
    }

    @Override
    public void onDamaged(DamageSource damageSource) {
        super.onDamaged(damageSource);
    }

    @Override
    protected void fall(double heightDifference, boolean onGround, BlockState state, BlockPos landedPosition) {
        super.handleFall(0.0,heightDifference,0.0,onGround);
    }

    @Override
    public void kill() {
    }

    @Override
    public void onDisconnect() {
        logged = false;
        super.onDisconnect();
    }

    public static class BotAI{
        public static Entity targetedEntity;
        public static HitResult target;
        public static ServerPlayerEntity currentPlayerTarget;
        public static ServerPlayerEntity user;

        private static boolean nextGroundType;
        private static float forwardSpeed = 0.0f;
        private static float sidewaysSpeed = 0.0f;
        private static int waitTicks;
        private static boolean delayed = false;
        private static int attackCoolDown;
        private static int windchargeCoolDown;
        private static int actionStep = 1;
        //Inventory Slots
        private static final int swordSlot = 0;
        private static final int enderPearlSlot = 1;
        private static final int shieldSlot = 2;
        private static final int breechMaceSlot = 3;
        private static final int densityMaceSlot = 4;
        private static final int elytraSlot = 5;
        private static final int windChargeSlot = 6;
        private static final int foodSlot = 7;
        private static final int axeSlot = 8;

        public static Difficulty difficulty = Difficulty.TIER_5;
        private static CurrentAction currentAction = CurrentAction.NONE;
        private static ActionSequence active = ActionSequence.NONE;

        public static boolean refillInvofuser = false;
        public static boolean currentMaterialofPlayerisDia = false;

        public static boolean elytra = false;
        public static boolean refreshInv = true;
        public static boolean currentMaterialisDia = true;
        public static boolean idle = true;
        public static boolean attributeSwap = true;
        public static boolean aerialOnly = false;

        private static final Map<Integer, Runnable> reg_mace_atk = new HashMap<>();

        public enum Difficulty{
            TIER_5,
            TIER_4,
            TIER_3,
            TIER_2,
            TIER_1
        }
        public enum CurrentAction{
            NONE,
            DENSITY_ATTACK,//A 40
            DENSITY_SWAP,//A 20
            BREECH_ATTACK,//A 10
            BREECH_SWAP,//B 30
            SWORD_ATTACK, //G 50
            AXE_ATTACK, //B C 20
            STUN_SLAM, // A C 30
            ELYTRA_MACE_ATTACK, // A C 40
            EATING,// B C
            SHIELD //B C
        }
        public enum Actions {
            USE_WIND_CHARGE
            {
                @Override
                public void execute(){
                    if(windchargeCoolDown <= 0) {
                        changeSlot(windChargeSlot);
                        doAction(Actions.USE);
                        windchargeCoolDown = 10;
                    }
                }
            },
            EAT
            {
                @Override
                public void execute(){
                    changeSlot(foodSlot);
                    doAction(Actions.USE);
                }
            },
            USE_SHIELD
            {
                @Override
                public void execute(){
                    changeSlot(shieldSlot);
                    doAction(USE);
                }
            },
            JUMP
            {
                @Override
                public void execute(){
                    if(player.isOnGround()){
                        player.jump();
                    }
                }
            },
            ATTACK
            {
                @Override
                public void execute(){
                    if(targetedEntity != null && attackCoolDown <= 0){
                        player.attack(targetedEntity);
                        player.swingHand(Hand.MAIN_HAND);
                        attackCoolDown = 3;
                    }
                }
            },
            USE
            {
                @Override
                public void execute(){
                    player.getStackInHand(Hand.MAIN_HAND).use(player.getWorld(),player,Hand.MAIN_HAND);
                }
            },
            LAUNCH_JUMP
            {
                @Override
                public void execute(){
                        new Thread(() -> {
                            try {
                                Thread.sleep(20);
                                doAction(JUMP);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }).start();

                }
            },
            SPRINT
            {
                @Override
                public void execute(){
                    if(player.isSneaking()){
                        player.setSneaking(false);
                    }
                    player.setSprinting(true);
                }
            },
            SNEAK
            {
                @Override
                public void execute(){
                    if(player.isSprinting()){
                        player.setSprinting(false);
                    }
                    player.setSneaking(true);
                }
            },
            WAIT
                    {
                        @Override
                        public void execute() {
                            //skipping tick
                        }
                    };
            public abstract void execute();
        }
        public enum ActionSequence{
            NONE,
            MACE_HIT,
            REG_MACE_ATK
        }
        public static void AI(){
            if(idle){
                active = ActionSequence.NONE;
                forwardSpeed = 0;
                sidewaysSpeed = 0;
                return;
            }
            if(active == ActionSequence.NONE){
                active = ActionSequence.REG_MACE_ATK;
            }
            doNextAction();
        }
        private static void doNextAction(){
            switch(active){
                case NONE -> {}
                case REG_MACE_ATK -> {
                    if(actionStep == 1){
                        reg_mace_atk.get(actionStep).run();
                        return;
                    }
                    if(actionStep == 2){
                        reg_mace_atk.get(actionStep).run();
                        return;
                    }
                    if(actionStep == 3){
                        reg_mace_atk.get(actionStep).run();
                        return;
                    }
                    if(actionStep == 4){
                        reg_mace_atk.get(actionStep).run();
                        return;
                    }
                    if(actionStep == 5){
                        reg_mace_atk.get(actionStep).run();
                        actionStep = 1;
                        active = ActionSequence.MACE_HIT;
                    }
                }
                case MACE_HIT -> {
                    if(currentPlayerTarget != null) {
                        lookAt(currentPlayerTarget.getCameraPosVec(1));
                        doAction(Actions.ATTACK);
                    }
                }
            }
        }
        public static void refillInventory(String refillType, String materialType, String name){
            currentMaterialisDia = !materialType.equals("netherite");
            if(refillType.equals("refill")) {
                commandExecute("item replace entity "+name+" container.1 with minecraft:ender_pearl 16");
                commandExecute("item replace entity "+name+" container.6 with minecraft:wind_charge 64");
                commandExecute("item replace entity "+name+" container.7 with minecraft:golden_apple 64");
                if(user != null && refillInvofuser){
                    user.setStackInHand(Hand.OFF_HAND, new ItemStack(Items.TOTEM_OF_UNDYING));
                }
            } else if (refillType.equals("full")){
                commandExecute("item replace entity "+name+" container.0 with minecraft:"+materialType+"_sword[minecraft:unbreakable={},minecraft:enchantments={sharpness:5,sweeping_edge:3}]");
                commandExecute("item replace entity "+name+" container.1 with minecraft:ender_pearl 16");
                commandExecute("item replace entity "+name+" container.2 with minecraft:shield[minecraft:unbreakable={}]");
                commandExecute("item replace entity "+name+" container.3 with minecraft:mace[minecraft:unbreakable={},minecraft:enchantments={breach:4}]");
                commandExecute("item replace entity "+name+" container.4 with minecraft:mace[minecraft:unbreakable={},minecraft:enchantments={density:5,wind_burst:1}]");
                commandExecute("item replace entity "+name+" container.5 with minecraft:elytra[minecraft:unbreakable={}]");
                commandExecute("item replace entity "+name+" container.6 with minecraft:wind_charge 64");
                commandExecute("item replace entity "+name+" container.7 with minecraft:golden_apple 64");
                commandExecute("item replace entity "+name+" container.8 with minecraft:"+materialType+"_axe[minecraft:unbreakable={},minecraft:enchantments={sharpness:5}]");
                commandExecute("item replace entity "+name+" armor.head with minecraft:"+materialType+"_helmet[minecraft:unbreakable={},minecraft:enchantments={protection:4}]");
                commandExecute("item replace entity "+name+" armor.chest with minecraft:"+materialType+"_chestplate[minecraft:unbreakable={},minecraft:enchantments={protection:4}]");
                commandExecute("item replace entity "+name+" armor.legs with minecraft:"+materialType+"_leggings[minecraft:unbreakable={},minecraft:enchantments={protection:4}]");
                commandExecute("item replace entity "+name+" armor.feet with minecraft:"+materialType+"_boots[minecraft:unbreakable={},minecraft:enchantments={protection:4,feather_falling:4}]");
                player.setStackInHand(Hand.OFF_HAND, new ItemStack(Items.TOTEM_OF_UNDYING));
                if(user != null){
                    user.setStackInHand(Hand.OFF_HAND, new ItemStack(Items.TOTEM_OF_UNDYING));
                }
            }
        }
        public static void commandExecute(String input){
            try {
                Objects.requireNonNull(player.getServer()).getCommandManager().getDispatcher().execute(input, player.getServer().getCommandSource().withSilent());
            } catch (CommandSyntaxException e) {
                throw new RuntimeException(e);
            }
        }
        private static void getRandomAttackType(boolean groundType){
            double val = Math.random();
            if(groundType){
                if(val < 0.2){
                    currentAction = CurrentAction.AXE_ATTACK; //20%
                } else if (val < 0.5){
                    currentAction = CurrentAction.BREECH_SWAP; //30%
                } else {
                    currentAction = CurrentAction.SWORD_ATTACK; //50%
                }
            }
        }

        private static boolean randomizeActionType(){
            return Math.random() <= 0.3;
        }

        public static void update(){
            updateCooldown();
            player.setStackInHand(Hand.OFF_HAND, new ItemStack(Items.TOTEM_OF_UNDYING));
            currentPlayerTarget = getNearbyPlayer();
            checkMaceHitMissed();
            updateActionStep();
            updateTarget();
            float vel;
            if(!player.isOnGround()){
                vel = 0.3f;
            }else{
                vel = player.isSneaking()?0.3f:1.0f;
            }
            player.forwardSpeed = forwardSpeed * vel;
            player.sidewaysSpeed = sidewaysSpeed * vel;
            if(refreshInv) {
                refillInventory("refill", currentMaterialisDia?"diamond":"netherite", "MaceBot");
            }
            if(refillInvofuser && user != null){
                refillInventory("refill", currentMaterialofPlayerisDia?"diamond":"netherite",user.getName().getString());
            }
        }
        private static void updateCooldown(){
            windchargeCoolDown--;
            attackCoolDown--;
        }
        private static void checkMaceHitMissed(){
            if(active == ActionSequence.MACE_HIT && player.isOnGround()){
                active = ActionSequence.NONE;
            }
        }
        private static void updateActionStep(){
            if(active != ActionSequence.NONE && active != ActionSequence.MACE_HIT){
                actionStep++;
            }
        }
        private static void setForwardSpeed(float v){
            forwardSpeed = v;
        }
        private static void setSidewaysSpeed(float v){
            sidewaysSpeed = v;
        }
        private  static ServerPlayerEntity getNearbyPlayer(){
            List<ServerPlayerEntity> a = player.getServerWorld().getPlayers();
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
        private static void doAction(Actions actions){
            actions.execute();
        }
        public static void changeSlot(int slot){
            inventory.selectedSlot = slot;
        }
        public static void look(float yaw, float pitch){
            player.setYaw(yaw % 360); //setYaw
            player.setPitch(Math.clamp(pitch, -90, 90));
        }
        public static void look(Direction direction) {
            switch (direction)
            {
                case NORTH -> look(180, 0);
                case SOUTH -> look(0, 0);
                case EAST  -> look(-90, 0);
                case WEST  -> look(90, 0);
                case UP    -> look(player.getYaw(), -90);
                case DOWN  -> look(player.getPitch(), 90);
            }
        }
        public static void lookAt(Vec3d position) {
            player.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, position);
        }
        public static void updateTarget() {
            float tickDelta = 1.0f;
            double a = player.getBlockInteractionRange();
            double b = player.getEntityInteractionRange();
            Entity camera = player.getCameraEntity();
            HitResult hitResult = getTarget(camera, a, b, tickDelta);
            target = hitResult;
            targetedEntity = hitResult instanceof EntityHitResult entityHitResult ? entityHitResult.getEntity() : null;
        }
        private static @NotNull HitResult getTarget(@NotNull Entity camera, double a, double b, float tickDelta){
            double c = Math.max(a,b);
            double d = MathHelper.square(c);
            Vec3d vec3d = camera.getCameraPosVec(tickDelta);
            HitResult hitResult = camera.raycast(c,tickDelta,false);
            double e = hitResult.getPos().squaredDistanceTo(vec3d);
            if(hitResult.getType() != HitResult.Type.ENTITY){
                d = e;
                c = Math.sqrt(e);
            }

            Vec3d vec3d2 = camera.getRotationVec(tickDelta);
            Vec3d vec3d3 = vec3d.add(vec3d2.x * c, vec3d2.y * c, vec3d2.z * c);
            float f = 1.0F;
            Box box = camera.getBoundingBox().stretch(vec3d2.multiply(c)).expand(1.0, 1.0, 1.0);
            EntityHitResult entityHitResult = ProjectileUtil.raycast(camera, vec3d, vec3d3,box, entity -> !entity.isSpectator() && entity.canHit(), d);
            return entityHitResult != null && entityHitResult.getPos().squaredDistanceTo(vec3d) < e
                    ? ensureTargetInRange(entityHitResult, vec3d, b)
                    : ensureTargetInRange(hitResult, vec3d, a);
        }
        private static HitResult ensureTargetInRange(HitResult hitResult, Vec3d cameraPos, double interactionRange) {
            Vec3d vec3d = hitResult.getPos();
            if (!vec3d.isInRange(cameraPos, interactionRange)) {
                Vec3d vec3d2 = hitResult.getPos();
                Direction direction = Direction.getFacing(vec3d2.x - cameraPos.x, vec3d2.y - cameraPos.y, vec3d2.z - cameraPos.z);
                return BlockHitResult.createMissed(vec3d2, direction, BlockPos.ofFloored(vec3d2));
            } else {
                return hitResult;
            }
        }
        public static void loadActionSequences(){
            //reg_mace_atk
            reg_mace_atk.put(1, ()-> {forwardSpeed = 0;look(Direction.DOWN);});
            reg_mace_atk.put(2, ()-> doAction(Actions.WAIT));
            reg_mace_atk.put(3, ()-> doAction(Actions.WAIT));
            reg_mace_atk.put(4, ()-> {doAction(Actions.USE_WIND_CHARGE);doAction(Actions.LAUNCH_JUMP);});
            reg_mace_atk.put(5, ()-> {inventory.selectedSlot = 4;forwardSpeed = 1;});
        }
        public static void ctrlInvMgmt(ServerCommandSource source, boolean dia, boolean refill, boolean bl){
            user = source.getPlayer();
            currentMaterialofPlayerisDia = dia;
            refillInvofuser = refill;
            if(user != null && bl) {
                refillInventory("full", currentMaterialofPlayerisDia ? "diamond" : "netherite", user.getName().getString());
            }
        }
    }
}
