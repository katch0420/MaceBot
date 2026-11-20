package net.katch0420.macebot.main.macebot.bot;

import com.mojang.authlib.GameProfile;
//? if >=1.21.9 {
import com.mojang.authlib.yggdrasil.response.NameAndId;
import net.katch0420.macebot.main.macebot.ai.Core;
import net.minecraft.world.entity.PositionMoveRotation;
//?}
import net.katch0420.macebot.main.kits.server.KitGiver;
import net.katch0420.macebot.main.kits.server.KitRegistry;
import net.katch0420.macebot.main.settings.Settings;
//? if >=1.21.2
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.GameType;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static net.katch0420.macebot.main.MaceBot.LOGGER;

/**
 * Handles spawning the bot entity into the world.
 * <p>
 * Pulled out of PlayerBot so the entity class stays clean.
 * All hardcoded values (name, kit, step height) come from BotConfig.
 */
public final class BotSpawner {
    private static boolean spawning;

    /**
     * Asynchronously fetch the game profile then spawn the bot on the server thread.
     */
    public static void spawn(MinecraftServer server, ServerLevel world, BlockPos pos) {
        Settings.MACEBOT_ONLINE.update(true);
        if (spawning || PlayerBot.playerBot != null) return;
        spawning = true;
        String botName = BotConfig.BOT_NAME.getString();
        GameProfile offlineProfile = new GameProfile(
                UUID.nameUUIDFromBytes(("OfflinePlayer:" + botName).getBytes(StandardCharsets.UTF_8)), botName);
        CompletableFuture<GameProfile> profileFuture = fetchProfile(server, offlineProfile, botName)
                .thenApply(profile -> profile.orElse(offlineProfile))
                .completeOnTimeout(offlineProfile, 10, TimeUnit.SECONDS);
        profileFuture.whenCompleteAsync((resolved, error) -> {
            try {
                var kit = KitRegistry.get(BotConfig.DEFAULT_KIT);
                if (kit == null) {
                    LOGGER.error("[MaceBot] Default kit '{}' is not registered; bot was not spawned", BotConfig.DEFAULT_KIT);
                    return;
                }
                GameProfile profile = resolved;
                if (error != null) {
                    LOGGER.warn("[MaceBot] Failed to fetch game profile; using an offline profile", error);
                    profile = offlineProfile;
                }
                ClientInformation options = ClientInformation.createDefault();

                PlayerBot bot = new PlayerBot(server, world, profile, options);
                server.getPlayerList().placeNewPlayer(new PlayerBotConnection(PacketFlow.SERVERBOUND), bot, new CommonListenerCookie(profile, 0, options, false));
                bot.teleportTo(world, pos.getX(), pos.getY(), pos.getZ(), Set.of(), 0f, 0f
                        //?if >=1.21.2
                        , false
                );
                bot.unsetRemoved(true);
                bot.fixStartingPos = () -> bot.move(MoverType.PLAYER, new Vec3(pos.getX(), pos.getY(), pos.getZ()));
                if(BotConfig.CUSTOM_NAME != null){
                    bot.setCustomName(BotConfig.CUSTOM_NAME);
                    bot.setCustomNameVisible(true);
                }
                bot.setHealth(BotConfig.SPAWN_HEALTH);
                bot.getFoodData().setFoodLevel(20);
                bot.getAttributes().getInstance(Attributes.STEP_HEIGHT)
                        .setBaseValue(BotConfig.STEP_HEIGHT);
                bot.setGameMode(GameType.SURVIVAL);
                bot.getAbilities().flying = false;
                bot.getEntityData().set(PlayerBot.PLAYER_MODEL_PARTS, (byte) 0x7f);
                KitGiver.giveKit(bot, kit, false);
                server.getPlayerList().broadcastAll(new ClientboundTeleportEntityPacket(
                        //? if >=1.21.2 {
                        bot.getId(), PositionMoveRotation.of(bot), Set.of(), bot.onGround()
                        //?} else
                        //bot
                ));
                bot
                        //? >=1.21.6 {
                        .level()
                        //?} else
                        //.serverLevel()
                        .getChunkSource().move(bot);
                PlayerBot.playerBot = bot;
                bot.core = new Core(bot);
                Settings.MACEBOT_ID.update(bot.getId());
                System.out.println("Spawned " + bot.getName().getString());
            } catch (Exception e) {
                LOGGER.error("[MaceBot] Spawn failed: ", e);
                Settings.MACEBOT_ONLINE.update(false);
            } finally {
                spawning = false;
            }
        }, server); // execute on server thread
    }

    private static CompletableFuture<Optional<GameProfile>> fetchProfile(MinecraftServer server, GameProfile basic, String name) {
        //? if >=1.21.9 {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Optional<NameAndId> found = server.services().profileRepository().findProfileByName(name);
                if (found.isEmpty()) return Optional.of(basic);
                GameProfile profile = new GameProfile(found.get().id(), found.get().name());
                try {
                    var result = server.services().sessionService().fetchProfile(profile.id(), true);
                    return Optional.of(result == null ? profile : result.profile());
                } catch (Exception e) {
                    LOGGER.warn("[MaceBot] Could not fetch profile properties; using basic profile: {}", e.getMessage());
                    return Optional.of(profile);
                }
            } catch (Exception e) {
                LOGGER.warn("[MaceBot] Could not look up profile; using offline profile: {}", e.getMessage());
                return Optional.of(basic);
            }
        });
        //?} else
        //return SkullBlockEntity.fetchGameProfile(name);
    }

    private BotSpawner() {
    }
}