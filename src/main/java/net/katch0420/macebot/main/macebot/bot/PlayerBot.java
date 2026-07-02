package net.katch0420.macebot.main.macebot.bot;

import com.mojang.authlib.GameProfile;
import net.katch0420.macebot.main.MaceBot;
import net.katch0420.macebot.main.macebot.control.Controller;
import net.katch0420.macebot.main.settings.main.SettingsKey;
import net.katch0420.macebot.main.settings.server.Settings;
import net.katch0420.macebot.main.settings.server.SettingsSyncHelper;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
import net.minecraft.registry.tag.TagGroupLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Objects;

/**
 * The bot entity. Responsibilities:
 *  - Override tick/travel/fall/death behaviour
 *  - Hold the static reference and Controller
 *
 * Spawning logic lives in BotSpawner.
 * Configurable values live in BotConfig.
 * Movement multipliers live in MovementModifier.
 */
public class PlayerBot extends ServerPlayerEntity {

    public static PlayerBot playerBot;
    public static Controller controller;
    public static TrackedData<? super Byte> PLAYER_MODEL_PARTS =
            //? if >=1.21.9 {
            /*PlayerEntity.PLAYER_MODE_CUSTOMIZATION_ID;
             *///?} else
            PlayerEntity.PLAYER_MODEL_PARTS;

    /**
     * Set by BotSpawner after construction to fix the starting position.
     */

    private boolean frozen = false;
    public boolean frozen(){
        return frozen;
    }
    public void freeze(){ frozen = true; }
    public void unfreeze(){ frozen = false; }

    public Runnable fixStartingPos = () -> {
    };

    // ── Construction ──────────────────────────────────────────────────────────

    public PlayerBot(MinecraftServer server, ServerWorld world,
                     GameProfile profile, SyncedClientOptions options) {
        super(server, world, profile, options);
    }

    // ── Spawn helpers ─────────────────────────────────────────────────────────

    public static void spawnMaceBot(ServerPlayerEntity player) {
        BotSpawner.spawn(
                Objects.requireNonNull(
                        //? if >=1.21.9 {
                        /*player.getEntityWorld()
                         *///?} else
                        player.getWorld()
                                .getServer()),
                Objects.requireNonNull(
                        //? if >=1.21.9
                        /*player.getEntityWorld()),*/
                        //? if >=1.21.6 <=1.21.8
                        /*player.getWorld()),*/
                        //? if <=1.21.5
                        player.getServerWorld()),
                player.getBlockPos(),
                player
        );
    }

    public static void disconnect() {
        if (playerBot != null) {
            controller.pauseAndClear();
            MaceBot.server.getPlayerManager().broadcast(Text.of("§eMaceBot left the game"), false);
            MaceBot.server.getPlayerManager().remove(playerBot);
            controller = null;
            playerBot = null;
            Settings.setMacebotOnline(false);
            SettingsSyncHelper.broadcastCurrent(SettingsKey.MACEBOT_ONLINE, MaceBot.server);
        }
    }

    // ── Tick ──────────────────────────────────────────────────────────────────

    @Override
    public void tick() {
        if (
            //? if >=1.21.9 {
            /*getEntityWorld()
             *///?} else
                getWorld()
                        .getServer().getTicks() % BotConfig.POSITION_RESET_INTERVAL == 0) {
            resetPosition();
        }
        if (controller != null) controller.tick();
        super.tick();
        playerTick();
        //? if >=1.21.5
        /*this.handleFall(this.getX() - this.lastX,this.getY() - this.lastY,this.getZ() - this.lastZ, this.isOnGround());*/
        //? if 1.21.4
        //this.handleFall(this.getX() - this.prevX,this.getY() - this.prevY,this.getZ() - this.prevZ, this.isOnGround());
    }

    // ── Death ─────────────────────────────────────────────────────────────────

    @Override
    public void onDeath(DamageSource source) {
        super.onDeath(source);
        dead = false;
        setHealth(BotConfig.SPAWN_HEALTH);
        getHungerManager().setFoodLevel(20);
    }

    // ── Fall ──────────────────────────────────────────────────────────────────
    //? if <=1.21.3 {
    @Override
    protected void fall(double heightDifference, boolean onGround, BlockState state, BlockPos landedPosition) {
        if(frozen) return;
        // Suppress fall damage — bot is unkillable by falls
        handleFall(0.0, heightDifference, 0.0, onGround);
    }
    //?}

    // ── Travel ────────────────────────────────────────────────────────────────
    @Override
    public boolean damage(DamageSource source, float amount) {
        if (amount > 0.0f && blockedByShield(source)) {
            damageShield(amount);
            if (source.getAttacker() instanceof LivingEntity le && le.disablesShield()) {
                playSound(SoundEvents.ITEM_SHIELD_BREAK, 0.8F, 0.8F + getWorld().random.nextFloat() * 0.4F);
                disableShield();
            } else {
                playSound(SoundEvents.ITEM_SHIELD_BLOCK, 1.0F, 0.8F + getWorld().random.nextFloat() * 0.4F);
            }
            return false; // no super = no knockback
        }
        return super.damage(source, amount);
    }
    @Override
    public void travel(Vec3d movementInput) {
        float x = forwardSpeed;
        float z = sidewaysSpeed;

        if (isSneaking()) {
            x *= BotConfig.SNEAK_MULTIPLIER;
            z *= BotConfig.SNEAK_MULTIPLIER;
        } else if (isSprinting()) {
            x *= BotConfig.SPRINT_FORWARD;
            z *= BotConfig.SPRINT_SIDEWAYS;
        }

        if (isUsingItem()) {
            MovementModifier.Modifier mod = MovementModifier.getModifier(getActiveItem());
            if (mod != null) {
                x *= mod.forward();
                z *= mod.sideways();
            }
        }

        forwardSpeed  = x;
        sidewaysSpeed = z;
        super.travel(movementInput);
        if(frozen) {
            setVelocity(0,0,0);
            setRotation(prevYaw, prevPitch);
        }
    }

    public void unsetRemoved(boolean bool){
        this.unsetRemoved();
    }

    // ── Network ───────────────────────────────────────────────────────────────

    @Override
    public String getIp() { return BotConfig.BOT_IP; }
}