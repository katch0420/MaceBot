package net.katch0420.macebot.main.macebot.bot;

import com.mojang.authlib.GameProfile;
import net.katch0420.macebot.main.MaceBot;
import net.katch0420.macebot.main.macebot.ai.Core;
import net.katch0420.macebot.main.settings.Settings;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
//? if <= 1.21.1
//import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Objects;
import java.util.stream.StreamSupport;

/**
 * The bot entity. Responsibilities:
 *  - Override tick/travel/fall/death behaviour
 *  - Hold the static reference and Controller
 * <p>
 * Spawning logic lives in BotSpawner.
 * Configurable values live in BotConfig.
 * Movement speed dampening (sneak, item-use) is applied in tickNewAi(),
 * mirroring ClientPlayerEntity#tickMovement() — see BotInput.
 */
public class PlayerBot extends ServerPlayer {

    public static PlayerBot playerBot;
    public Core core;
    public static EntityDataAccessor<? super Byte> PLAYER_MODEL_PARTS = Player.DATA_PLAYER_MODE_CUSTOMISATION;

    private boolean frozen = false;
    private int ticksToNextAutojump;
    private float mountJumpStrength;
    private int field_3938;
    private boolean lastSneaking;

    public static void setDisplayName(MutableComponent mutableText) {
        BotConfig.BOT_NAME = mutableText;
        BotConfig.CUSTOM_NAME = mutableText;
    }

    public static void applyNewIdentity(MutableComponent c) {
        if (playerBot == null){
            setDisplayName(c);
            return;
        }
        MinecraftServer server = playerBot
                //? if >=1.21.9 {
                .level().getServer();
                //?} else
                //.getServer();
        ServerLevel world = playerBot
                //? if >=1.21.6 {
                .level();
                //?} else
                //.serverLevel();
        BlockPos pos = playerBot.blockPosition();
        kick();
        setDisplayName(c);
        BotSpawner.spawn(server, world, pos);
    }

    public boolean frozen(){
        return frozen;
    }
    public void freeze(){ frozen = true; }
    public void unfreeze(){ frozen = false; }

    public Runnable fixStartingPos = () -> {
    };

    /**
     * Held movement intent — the bot's equivalent of a real client's Input.
     * Actions/Controller write here (see ActionManager); this entity reads
     * it back every tick in tickNewAi(), never decaying away on its own.
     */
    public final BotInput input = new BotInput();

    // ── Construction ──────────────────────────────────────────────────────────

    public PlayerBot(MinecraftServer server, ServerLevel world,
                     GameProfile profile, ClientInformation options) {
        super(server, world, profile, options);
    }

    // ── Spawn helpers ─────────────────────────────────────────────────────────

    public static void spawnMaceBot(ServerPlayer player) {
        try {
            BotSpawner.spawn(
                    Objects.requireNonNull(
                            player.level()
                                    .getServer()),
                    Objects.requireNonNull(
                            //? if >=1.21.6 {
                            player.level()),
                    //?} else
                    //player.serverLevel()),
                    player.blockPosition()
            );
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void kick() {
        if (playerBot != null) {
            //controller.pauseAndClear();
            MaceBot.server.getPlayerList().broadcastSystemMessage(Component.nullToEmpty("§e"+ playerBot.getDisplayName().getString() +" left the game"), false);
            MaceBot.server.getPlayerList().remove(playerBot);
            //controller = null;
            playerBot = null;
            Settings.MACEBOT_ONLINE.update(false);
        }
    }

    // ── Tick ──────────────────────────────────────────────────────────────────

    @Override
    public void tick() {
        super.tick();
        if (level().getServer().getTickCount() % BotConfig.POSITION_RESET_INTERVAL == 0) {
            reapplyPosition();
        } resetLastActionTime();
        if (core != null) {
            core.tick();
        }
        doTick();
        boolean bl = this.isShiftKeyDown();
        if (bl != this.lastSneaking) {
            this.setShiftKeyDown(bl);
            this.lastSneaking = bl;
        }
        doCheckFallDamage(this.getX() - this.xo, this.getY() - this.yo, this.getZ() - this.zo, this.onGround());
    }

    @Override
    public void die(DamageSource source) {
        getInventory().clearContent();
        super.die(source);
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.WITHER_SPAWN, this.getSoundSource(), 1.0F, 1.0F);
        applyNewIdentity(BotConfig.CUSTOM_NAME);
    }
    //? if >= 1.21.2 && < 1.21.5 {
    /*public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (amount > 0.0f && isDamageSourceBlocked(source)) {
            hurtCurrentlyUsedShield(amount);
            if (source.getEntity() instanceof LivingEntity le && le.canDisableShield()) {
                playSound(SoundEvents.SHIELD_BREAK, 0.8F, 0.8F + level().random.nextFloat() * 0.4F);
                disableShield(getUseItem());
            } else {
                playSound(SoundEvents.SHIELD_BLOCK, 1.0F, 0.8F + level().random.nextFloat() * 0.4F);
            }
            return false;
        }
        return super.hurtServer(level, source, amount);
    }
    *///?}

    //? if <1.21.2 {
    /*@Override
    public boolean hurt(DamageSource source, float amount) {
        if (amount > 0.0f && isDamageSourceBlocked(source)) {
            hurtCurrentlyUsedShield(amount);
            if (source.getEntity() instanceof LivingEntity le && le.canDisableShield()) {
                playSound(SoundEvents.SHIELD_BREAK, 0.8F, 0.8F + level().random.nextFloat() * 0.4F);
                disableShield();
            } else {
                playSound(SoundEvents.SHIELD_BLOCK, 1.0F, 0.8F + level().random.nextFloat() * 0.4F);
            }
            return false;
        }
        return super.hurt(source, amount);
    }
    *///?}

    @Override
    public void travel(Vec3 movementInput) {
        super.travel(movementInput);
        if (frozen) {
            setDeltaMovement(0, 0, 0);
            setRot(yRotO, xRotO);
        }
    }
    public void unsetRemoved(boolean removed){
        this.unsetRemoved();
    }
    @Override public String getIpAddress() { return BotConfig.BOT_IP; }
    @Override public Component getDisplayName() {
        return hasCustomName() ? getCustomName() : super.getDisplayName();
    }

    @Override public void aiStep() {

        boolean bl = this.input.jumping;
        boolean bl2 = this.input.sneaking;
        boolean bl3 = this.hasEnoughImpulseToStartSprinting();
        Abilities playerAbilities = this.getAbilities();
        float f = (float)this.getAttributeValue(Attributes.SNEAKING_SPEED);
        this.input.tick(this.shouldSlowDown(), f);
        if (this.isUsingItem() && !this.isPassenger()) {
            this.input.movementSideways *= 0.2F;
            this.input.movementForward *= 0.2F;
        }

        boolean bl4 = false;
        if (this.ticksToNextAutojump > 0) {
            this.ticksToNextAutojump--;
            bl4 = true;
            this.input.jumping = true;
        }

        if (!this.noPhysics) {
            this.moveTowardsClosestSpace(this.getX() - this.getBbWidth() * 0.35, this.getZ() + this.getBbWidth() * 0.35);
            this.moveTowardsClosestSpace(this.getX() - this.getBbWidth() * 0.35, this.getZ() - this.getBbWidth() * 0.35);
            this.moveTowardsClosestSpace(this.getX() + this.getBbWidth() * 0.35, this.getZ() - this.getBbWidth() * 0.35);
            this.moveTowardsClosestSpace(this.getX() + this.getBbWidth() * 0.35, this.getZ() + this.getBbWidth() * 0.35);
        }

        boolean bl5 = this.canStartSprinting();
        boolean bl6 = this.isPassenger() ? this.getVehicle().onGround() : this.onGround();
        boolean bl7 = !bl2 && !bl3;
        if ((bl6 || this.isUnderWater()) && bl7 && bl5) {
            this.setSprinting(true);
        }
        if ((!this.isInWater() || this.isUnderWater()) && bl5 && input.isPressingSprint()) {
            this.setSprinting(true);
        }

        if (this.isSprinting()) {
            boolean bl8 = !this.input.hasForwardMovement() || !this.canSprint();
            boolean bl9 = bl8 || this.horizontalCollision && !this.minorHorizontalCollision || this.isInWater() && !this.isUnderWater();
            if (this.isSwimming()) {
                if (!this.onGround() && !this.input.sneaking && bl8 || !this.isInWater()) {
                    this.setSprinting(false);
                }
            } else if (bl9) {
                this.setSprinting(false);
            }
        }

        boolean bl8 = false;
        if (playerAbilities.mayfly) {
            if (!bl && this.input.jumping && !bl4) {
                if (this.jumpTriggerTime == 0) {
                    this.jumpTriggerTime = 7;
                } else if (!this.isSwimming()) {
                    playerAbilities.flying = !playerAbilities.flying;
                    if (playerAbilities.flying && this.onGround()) {
                        this.jumpFromGround();
                    }

                    bl8 = true;
                    this.onUpdateAbilities();
                    this.jumpTriggerTime = 0;
                }
            }
        }

        if (this.input.jumping && !bl8 && !bl && !playerAbilities.flying && !this.isPassenger() && !this.onClimbable()) {
            ItemStack itemStack = this.getItemBySlot(EquipmentSlot.CHEST);
            if (itemStack.is(Items.ELYTRA) &&
                    //? if >= 1.21.2 {
                    itemStack.has(DataComponents.GLIDER)
                    //?} else
                    //ElytraItem.isFlyEnabled(itemStack)
                     && this.tryToStartFallFlying()) {
                this.startFallFlying();
            }
        }

        if (this.isInWater() && this.input.sneaking && this.isAffectedByFluids()) {
            this.goDownInWater();
        }

        if (playerAbilities.flying) {
            int i = 0;
            if (this.input.sneaking) {
                i--;
            }

            if (this.input.jumping) {
                i++;
            }

            if (i != 0) {
                this.setDeltaMovement(this.getDeltaMovement().add(0.0, i * playerAbilities.getFlyingSpeed() * 3.0F, 0.0));
            }
        }

        PlayerRideableJumping jumpingMount = this.jumpableVehicle();
        if (jumpingMount != null && jumpingMount.getJumpCooldown() == 0) {
            if (this.field_3938 < 0) {
                this.field_3938++;
                if (this.field_3938 == 0) {
                    this.mountJumpStrength = 0.0F;
                }
            }

            if (bl && !this.input.jumping) {
                this.field_3938 = -10;
                jumpingMount.onPlayerJump(Mth.floor(this.getMountJumpStrength() * 100.0F));
                this.startRidingJump();
            } else if (!bl && this.input.jumping) {
                this.field_3938 = 0;
                this.mountJumpStrength = 0.0F;
            } else if (bl) {
                this.field_3938++;
                if (this.field_3938 < 10) {
                    this.mountJumpStrength = this.field_3938 * 0.1F;
                } else {
                    this.mountJumpStrength = 0.8F + 2.0F / (this.field_3938 - 9) * 0.1F;
                }
            }
        } else {
            this.mountJumpStrength = 0.0F;
        }

        super.aiStep();
        if (this.onGround() && playerAbilities.flying) {
            playerAbilities.flying = false;
            this.onUpdateAbilities();
        }
    }
    private boolean canStartSprinting() {return !this.isSprinting() && this.hasEnoughImpulseToStartSprinting() && this.hasEnoughFoodToStartSprinting() && !this.isUsingItem() && !this.hasEffect(MobEffects.BLINDNESS) && (!this.isPassenger() || this.vehicleCanSprint(this.getVehicle())) && !this.isFallFlying();}
    private boolean vehicleCanSprint(Entity entity) {
        return entity.canSprint() &&
                //? if >=1.21.5 {
                entity.isLocalInstanceAuthoritative();
                //?} else
                //entity.isControlledByLocalInstance();
    }
    public boolean hasEnoughFoodToStartSprinting() {return this.isPassenger() || (float)this.getFoodData().getFoodLevel() > 6.0F || this.getAbilities().mayfly;}

    @Nullable
    public PlayerRideableJumping jumpableVehicle() {
        Entity var2 = this.getControlledVehicle();
        PlayerRideableJumping var10000;
        if (var2 instanceof PlayerRideableJumping playerRideableJumping) {
            if (playerRideableJumping.canJump()) {
                var10000 = playerRideableJumping;
                return var10000;
            }
        }

        var10000 = null;
        return var10000;
    }
    protected void startRidingJump() {
        if (this.getControlledVehicle() instanceof PlayerRideableJumping jumpingMount) {
            int i = Mth.floor(this.getMountJumpStrength() * 100.0F);
            if (jumpingMount.canJump() && i > 0) {
                jumpingMount.handleStartJump(i);
            }
        }
    }

    private boolean suffocatesAt(BlockPos blockPos) {
        AABB aABB = this.getBoundingBox();
        AABB aABB2 = (new AABB(blockPos.getX(), aABB.minY, blockPos.getZ(), (double)blockPos.getX() + (double)1.0F, aABB.maxY, (double)blockPos.getZ() + (double)1.0F)).deflate(1.0E-7);
        return this.level().collidesWithSuffocatingBlock(this, aABB2);
    }

    private void moveTowardsClosestSpace(double d, double e) {
        BlockPos blockPos = BlockPos.containing(d, this.getY(), e);
        if (this.suffocatesAt(blockPos)) {
            double f = d - (double)blockPos.getX();
            double g = e - (double)blockPos.getZ();
            Direction direction = null;
            double h = Double.MAX_VALUE;
            Direction[] directions = new Direction[]{Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH};

            for(Direction direction2 : directions) {
                double i = direction2.getAxis().choose(f, 0.0F, g);
                double j = direction2.getAxisDirection() == Direction.AxisDirection.POSITIVE ? (double)1.0F - i : i;
                if (j < h && !this.suffocatesAt(blockPos.relative(direction2))) {
                    h = j;
                    direction = direction2;
                }
            }

            if (direction != null) {
                Vec3 vec3 = this.getDeltaMovement();
                if (direction.getAxis() == Direction.Axis.X) {
                    this.setDeltaMovement(0.1 * (double)direction.getStepX(), vec3.y, vec3.z);
                } else {
                    this.setDeltaMovement(vec3.x, vec3.y, 0.1 * (double)direction.getStepZ());
                }
            }

        }
    }

    private float getMountJumpStrength() {
        return mountJumpStrength;
    }

    private boolean hasEnoughImpulseToStartSprinting() {
        return this.isUnderWater() ? this.input.hasForwardMovement() : (double) this.input.movementForward >= 0.8;
    }

    @Override
    public void move(MoverType moverType, Vec3 vec3) {
        double d = this.getX();
        double e = this.getZ();
        super.move(moverType, vec3);
        this.updateAutoJump((float)(this.getX() - d), (float)(this.getZ() - e));
    }

    public boolean isAutoJumpEnabled() {
        return true;
    }

    public boolean shouldSlowDown() {
        return this.isCrouching() || this.isVisuallyCrawling();
    }

    private boolean isMoving() {
        Vec2 vec2 = this.input.getMovementInput();
        return vec2.x != 0.0F || vec2.y != 0.0F;
    }

    private boolean canAutoJump() {
        return this.isAutoJumpEnabled() && this.ticksToNextAutojump <= 0 && this.onGround() && !this.isStayingOnGroundSurface() && !this.isPassenger() && this.isMoving() && (double)this.getBlockJumpFactor() >= (double)1.0F;
    }

    protected void updateAutoJump(float f, float g) {
        if (this.canAutoJump()) {
            Vec3 vec3 = this.position();
            Vec3 vec32 = vec3.add(f, 0.0F, g);
            Vec3 vec33 = new Vec3(f, 0.0F, g);
            float h = this.getSpeed();
            float i = (float)vec33.lengthSqr();
            if (i <= 0.001F) {
                Vec2 vec2 = this.input.getMovementInput();
                float j = h * vec2.x;
                float k = h * vec2.y;
                float l = Mth.sin(this.getYRot() * ((float)Math.PI / 180F));
                float m = Mth.cos(this.getYRot() * ((float)Math.PI / 180F));
                vec33 = new Vec3(j * m - k * l, vec33.y, k * m + j * l);
                i = (float)vec33.lengthSqr();
                if (i <= 0.001F) {
                    return;
                }
            }

            float n = Mth.invSqrt(i);
            Vec3 vec34 = vec33.scale(n);
            Vec3 vec35 = this.getForward();
            float l = (float)(vec35.x * vec34.x + vec35.z * vec34.z);
            if (!(l < -0.15F)) {
                CollisionContext collisionContext = CollisionContext.of(this);
                BlockPos blockPos = BlockPos.containing(this.getX(), this.getBoundingBox().maxY, this.getZ());
                BlockState blockState = this.level().getBlockState(blockPos);
                if (blockState.getCollisionShape(this.level(), blockPos, collisionContext).isEmpty()) {
                    blockPos = blockPos.above();
                    BlockState blockState2 = this.level().getBlockState(blockPos);
                    if (blockState2.getCollisionShape(this.level(), blockPos, collisionContext).isEmpty()) {
                        float p = 1.2F;
                        if (this.hasEffect(
                                //? if >=1.21.5 {
                                MobEffects.JUMP_BOOST
                                //?} else
                                //MobEffects.JUMP
                        )) {
                            p += (float)(this.getEffect(
                                    //? if >=1.21.5 {
                                    MobEffects.JUMP_BOOST
                                    //?} else
                                    //MobEffects.JUMP
                            ).getAmplifier() + 1) * 0.75F;
                        }

                        float q = Math.max(h * 7.0F, 1.0F / n);
                        Vec3 vec37 = vec32.add(vec34.scale(q));
                        float r = this.getBbWidth();
                        float s = this.getBbHeight();
                        AABB aABB = (new AABB(vec3, vec37.add(0.0F, s, 0.0F))).inflate(r, 0.0F, r);
                        Vec3 vec36 = vec3.add(0.0F, 0.51F, 0.0F);
                        vec37 = vec37.add(0.0F, 0.51F, 0.0F);
                        Vec3 vec38 = vec34.cross(new Vec3(0.0F, 1.0F, 0.0F));
                        Vec3 vec39 = vec38.scale(r * 0.5F);
                        Vec3 vec310 = vec36.subtract(vec39);
                        Vec3 vec311 = vec37.subtract(vec39);
                        Vec3 vec312 = vec36.add(vec39);
                        Vec3 vec313 = vec37.add(vec39);
                        Iterable<VoxelShape> iterable = this.level().getCollisions(this, aABB);
                        Iterator<AABB> iterator = StreamSupport.stream(iterable.spliterator(), false).flatMap((voxelShapex) -> voxelShapex.toAabbs().stream()).iterator();
                        float t = Float.MIN_VALUE;

                        while(iterator.hasNext()) {
                            AABB aABB2 = iterator.next();
                            if (aABB2.intersects(vec310, vec311) || aABB2.intersects(vec312, vec313)) {
                                t = (float)aABB2.maxY;
                                Vec3 vec314 = aABB2.getCenter();
                                BlockPos blockPos2 = BlockPos.containing(vec314);

                                for(int u = 1; (float)u < p; ++u) {
                                    BlockPos blockPos3 = blockPos2.above(u);
                                    BlockState blockState3 = this.level().getBlockState(blockPos3);
                                    VoxelShape voxelShape;
                                    if (!(voxelShape = blockState3.getCollisionShape(this.level(), blockPos3, collisionContext)).isEmpty()) {
                                        t = (float)voxelShape.max(Direction.Axis.Y) + (float)blockPos3.getY();
                                        if ((double)t - this.getY() > (double)p) {
                                            return;
                                        }
                                    }

                                    if (u > 1) {
                                        blockPos = blockPos.above();
                                        BlockState blockState4 = this.level().getBlockState(blockPos);
                                        if (!blockState4.getCollisionShape(this.level(), blockPos, collisionContext).isEmpty()) {
                                            return;
                                        }
                                    }
                                }
                                break;
                            }
                        }

                        if (t != Float.MIN_VALUE) {
                            float v = (float)((double)t - this.getY());
                            if (!(v <= 0.5F) && !(v > p)) {
                                this.ticksToNextAutojump = 1;
                            }
                        }
                    }
                }
            }
        }
    }
    @Override
    public boolean isShiftKeyDown() {
        return this.input != null && this.input.sneaking;
    }
    public void serverAiStep() {
        super.serverAiStep();
        this.xxa = this.input.movementSideways;
        this.zza = this.input.movementForward;
        this.jumping = this.input.jumping;

    }
}