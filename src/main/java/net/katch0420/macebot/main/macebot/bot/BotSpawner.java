package net.katch0420.macebot.main.macebot.bot;

import com.mojang.authlib.GameProfile;
//? if >=1.21.9 {
/*import com.mojang.authlib.yggdrasil.response.NameAndId;
import net.minecraft.entity.EntityPosition;
*///?}
//? if >=1.21.2 <=1.21.8
/*import net.minecraft.entity.player.PlayerPosition;*/
import net.katch0420.macebot.main.kits.server.KitGiver;
import net.katch0420.macebot.main.kits.server.KitRegistry;
import net.katch0420.macebot.main.macebot.control.Controller;
import net.katch0420.macebot.main.settings.main.SettingsKey;
import net.katch0420.macebot.main.settings.server.Settings;
import net.katch0420.macebot.main.settings.server.SettingsSyncHelper;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static net.katch0420.macebot.main.MaceBot.LOGGER;

/**
 * Handles spawning the bot entity into the world.
 * <p>
 * Pulled out of PlayerBot so the entity class stays clean.
 * All hardcoded values (name, kit, step height) come from BotConfig.
 */
public final class BotSpawner {

    /**
     * Asynchronously fetch the game profile then spawn the bot on the server thread.
     */
    public static void spawn(MinecraftServer server, ServerWorld world, BlockPos pos, ServerPlayerEntity requester) {
        Settings.setMacebotOnline(true);
        //? if >=1.21.9 {
        /*GameProfile profile = server.getApiServices().profileResolver().getProfileByName(BotConfig.BOT_NAME).orElse(new GameProfile(UUID.randomUUID(), BotConfig.BOT_NAME));
         *///?} else
        GameProfile profile = server.getUserCache().findByName(BotConfig.BOT_NAME).orElse(new GameProfile(UUID.randomUUID(), BotConfig.BOT_NAME));

        fetchProfile(server, profile, profile.getName()).whenCompleteAsync((optional, error) -> {
            try {
                if (error != null) {
                    LOGGER.error("[MaceBot] Failed to fetch game profile: {}", error.getMessage());
                    return;
                }
                // Use fetched profile if available, fall back to local
                GameProfile resolved = optional.orElse(profile);
                SyncedClientOptions options = SyncedClientOptions.createDefault();

                PlayerBot bot = new PlayerBot(server, world, resolved, options);
                bot.fixStartingPos = () -> bot.move(MovementType.PLAYER, new Vec3d(pos.getX(), pos.getY(), pos.getZ()));

                server.getPlayerManager().onPlayerConnect(new PlayerBotConnection(NetworkSide.SERVERBOUND), bot, new ConnectedClientData(resolved, 0, options, false));
                bot.teleport(world, pos.getX(), pos.getY(), pos.getZ(), Set.of(), 0f, 0f);
                bot.setHealth(BotConfig.SPAWN_HEALTH);
                bot.getHungerManager().setFoodLevel(20);
                bot.unsetRemoved(true);
                bot.getAttributes().getCustomInstance(
                        //? if >=1.21.2 {
                        /*EntityAttributes.STEP_HEIGHT)
                         *///?} else
                        EntityAttributes.GENERIC_STEP_HEIGHT).setBaseValue(BotConfig.STEP_HEIGHT);
                bot.changeGameMode(GameMode.SURVIVAL);
                bot.getAbilities().flying = false;
                bot.getDataTracker().set(PlayerBot.PLAYER_MODEL_PARTS, (byte) 0x7f);
                //? if >=1.21.9 {
                /*server.getPlayerManager().sendToAll(new EntityPositionS2CPacket(bot.getId(), EntityPosition.fromEntity(bot),Set.of(),bot.isOnGround()));
                bot.getEntityWorld().getChunkManager().updatePosition(bot);
                *///?}
                //? if >=1.21.6 <=1.21.8 {
                /*server.getPlayerManager().sendToAll(new EntityPositionS2CPacket(bot.getId(), PlayerPosition.fromEntity(bot), Set.of(), bot.isOnGround()));
                bot.getWorld().getChunkManager().updatePosition(bot);
                *///?}
                //? if >=1.21.2 <=1.21.5 {
                /*server.getPlayerManager().sendToAll(new EntityPositionS2CPacket(bot.getId(), PlayerPosition.fromEntity(bot), Set.of(), bot.isOnGround()));
                bot.getServerWorld().getChunkManager().updatePosition(bot);
                *///?}
                //? if <=1.21.1 {
                server.getPlayerManager().sendToAll(new EntityPositionS2CPacket(bot));
                bot.getServerWorld().getChunkManager().updatePosition(bot);
                //?}

                PlayerBot.playerBot = bot;
                PlayerBot.controller = new Controller();
                KitGiver.giveKit(bot, KitRegistry.get(BotConfig.DEFAULT_KIT), false);
                SettingsSyncHelper.broadcastCurrent(SettingsKey.MACEBOT_ONLINE, server);
                SettingsSyncHelper.applyAndBroadcast(SettingsKey.MACEBOT_ID, bot.getId(), server);
            } catch (Exception e) {
                LOGGER.error("[MaceBot] Spawn failed: ", e);
            }
        }, server); // execute on server thread
    }

    private static CompletableFuture<Optional<GameProfile>> fetchProfile(MinecraftServer server, GameProfile basic, String name) {
        //? if >=1.21.9 {
        /*return CompletableFuture.supplyAsync(() -> {
            // findByName checks local cache first, then queries Mojang if needed
            Optional<GameProfile> cached = server.getApiServices().profileResolver()
                    .getProfileByName(BotConfig.BOT_NAME);
            if (cached.isPresent()) return cached;

            // Fallback: fill textures via session service using the UUID we already have
            try {
                var result = server.getApiServices().sessionService().fetchProfile(basic.id(), true);
                return result != null ? Optional.of(result.profile()) : Optional.of(basic);
            } catch (Exception e) {
                LOGGER.warn("[MaceBot] Could not fetch full profile, using basic: {}", e.getMessage());
                return Optional.of(basic);
            }
        });
        *///?} else {
        return SkullBlockEntity.fetchProfileByName(name);
        //?}
    }

    private BotSpawner() {
    }
}