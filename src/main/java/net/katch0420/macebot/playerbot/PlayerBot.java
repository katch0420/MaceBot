package net.katch0420.macebot.playerbot;

import com.mojang.authlib.GameProfile;
import net.katch0420.macebot.ai.ActionManager;
import net.katch0420.macebot.ai.Controller;
import net.katch0420.macebot.player.Kits;
import net.katch0420.macebot.utils.SkinManager;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.*;
import net.minecraft.world.GameMode;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class PlayerBot extends ServerPlayerEntity {

    public Runnable fixStartingPos = () -> {
    };

    public static boolean botOnline;
    public static Controller controller;
    public static ActionManager actionManager;

    public static void createBot(MinecraftServer server, ServerWorld world, BlockPos blockPos, ServerCommandSource source) {

        GameProfile gameProfile = server.getUserCache().findByName("MaceBot").orElse(new GameProfile(UUID.randomUUID(), "MaceBott"));
        String name = gameProfile.getName();

        fetchGP(name).whenCompleteAsync((p, t) -> {
            if (t != null) {
                return;
            }

            SyncedClientOptions clientOptions = SyncedClientOptions.createDefault();

            PlayerBot playerBot = new PlayerBot(server, world, gameProfile, clientOptions);
            playerBot.fixStartingPos = () -> playerBot.move(MovementType.PLAYER, new Vec3d(blockPos.getX(), blockPos.getY(), blockPos.getZ()));
            server.getPlayerManager().onPlayerConnect(new PlayerBotConnection(NetworkSide.SERVERBOUND), playerBot, new ConnectedClientData(gameProfile, 0, clientOptions, false));
            playerBot.teleport(world, blockPos.getX(), blockPos.getY(), blockPos.getZ(), 0.0f, 0.0f);

            playerBot.setHealth(20.0F);
            playerBot.unsetRemoved();
            playerBot.getAttributes().getCustomInstance(EntityAttributes.GENERIC_STEP_HEIGHT).setBaseValue(0.6f);
            playerBot.changeGameMode(GameMode.SURVIVAL);
            server.getPlayerManager().sendToAll(new EntityPositionS2CPacket(playerBot));
            playerBot.getServerWorld().getChunkManager().updatePosition(playerBot);
            playerBot.dataTracker.set(PLAYER_MODEL_PARTS, (byte) 0x7f);
            playerBot.getAbilities().flying = false;
            actionManager = new ActionManager(playerBot);
            controller = new Controller(actionManager);
            Kits.giveKit(Objects.requireNonNull(playerBot.getServer()).getCommandSource(), Kits.Kit.MACE_NETHERITE, false, "MaceBot");
            SkinManager.applyDefaultSkin(playerBot);
        }, server);
    }

    private static CompletableFuture<Optional<GameProfile>> fetchGP(final String name) {
        return SkullBlockEntity.fetchProfileByName(name);
    }

    public PlayerBot(MinecraftServer server, ServerWorld world, GameProfile profile, SyncedClientOptions clientOptions) {
        super(server, world, profile, clientOptions);
    }

    @Override
    protected void fall(double heightDifference, boolean onGround, BlockState state, BlockPos landedPosition) {
        handleFall(0.0, heightDifference, 0.0, onGround);
    }

    @Override
    public void tick() {
        if (this.getServer().getTicks() % 10 == 0) {
            this.resetPosition();
        }
        controller.tick();
        super.tick();
        this.playerTick();
        //this.fall(player.getY() - player.prevY, player.isOnGround(),player.getBlockStateAtPos(),player.getBlockPos());

    }

    @Override
    public void onDeath(DamageSource damageSource) {
        super.onDeath(damageSource);
        this.dead = false;
        this.setHealth(20f);
    }

    @Override
    public String getIp() {
        return "127.0.0.1";
    }

    @Override
    public void travel(Vec3d movementInput) {
        float x = this.forwardSpeed;
        float z = this.sidewaysSpeed;

        if (this.isSneaking()) {
            x *= 0.3F;
            z *= 0.3F;
        } else if (this.isSprinting()){
            x *= 1.3F;
            z *= 9.8F;
        }
        if (this.isUsingItem()) {
            ItemStack stack = this.getActiveItem();
            if (stack.getUseAction().equals(UseAction.EAT) || stack.getUseAction().equals(UseAction.DRINK)) {
                x *= 0.2F;
                z *= 0.2F;
            } else if (stack.isOf(Items.SHIELD)) {
                x *= 0.3F;
                z *= 0.3F;
            } else if (stack.isOf(Items.SPYGLASS)) {
                x *= 0.1F;
                z *= 0.1F;
            }
        }
        this.forwardSpeed = x;
        this.sidewaysSpeed = z;
        super.travel(movementInput);
    }
}

