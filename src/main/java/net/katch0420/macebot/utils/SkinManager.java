package net.katch0420.macebot.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.util.Base64;

import static net.katch0420.macebot.MaceBot.LOGGER;

public class SkinManager {

    // Directory inside .minecraft
    private static final File SKIN_DIR = new File(System.getProperty("user.dir"), "macebot/skins/");

    // Ensure directory exists
    public static void init() {
        if (!SKIN_DIR.exists()) {
            if(!SKIN_DIR.mkdir()){
                LOGGER.info("Unexpected Error occurred in creating: {}",SKIN_DIR.getAbsolutePath());
            }
            LOGGER.info("Created skin directory: {}",SKIN_DIR.getAbsolutePath());
        }
    }

    /**
     * Apply a skin from a PNG file in macebot/skins/
     * @param player The player to apply the skin to
     * @param fileName The PNG filename (e.g. "VBM.png")
     */
    public static int applySkin(ServerPlayerEntity player, String fileName) {
        File skinFile = new File(SKIN_DIR, fileName);
        if (!skinFile.exists()) {
            LOGGER.info("Skin file not found: {}",skinFile.getAbsolutePath());
            return 0;
        }

        try {
            // Read PNG bytes
            byte[] pngBytes = Files.readAllBytes(skinFile.toPath());

            // Wrap into Mojang-style JSON
            String json = "{ \"textures\": { \"SKIN\": { \"url\": \"data:image/png;base64," +
                    Base64.getEncoder().encodeToString(pngBytes) + "\" } } }";

            // Encode JSON to Base64
            String value = Base64.getEncoder().encodeToString(json.getBytes());

            // Inject into GameProfile
            GameProfile profile = player.getGameProfile();
            profile.getProperties().removeAll("textures");
            profile.getProperties().put("textures", new Property("textures", value, null));

            // Force refresh so clients see it
            player.getServer().getPlayerManager().sendToAll(new PlayerListS2CPacket(PlayerListS2CPacket.Action.ADD_PLAYER,player));

            LOGGER.info("Applied skin from file: {}" ,fileName);
            return 1;

        } catch (IOException e) {
            LOGGER.info("Couldn't apply skin to {}: {}",player.getName().getString(),e);
            return 2;
        }
    }
    public static int applySkinFromUrl(ServerPlayerEntity player, String skinUrl) {
        // 1. Validate URL format
        if (!skinUrl.startsWith("http://") && !skinUrl.startsWith("https://")) {
            return 0;
        }

        try {
            // 2. Check availability
            // 1. Create HttpClient
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(java.time.Duration.ofSeconds(3))
                    .build();

            // 2. Build HEAD request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(skinUrl))
                    .method("HEAD", HttpRequest.BodyPublishers.noBody())
                    .build();

            // 3. Send request
            HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
            int statusCode = response.statusCode();

            // 4. Validate response
            if (statusCode < 200 || statusCode >= 400) {
                return 0; // invalid URL
            }

            // 5. Build JSON + Base64 encode
            String json = "{ \"textures\": { \"SKIN\": { \"url\": \"" + skinUrl + "\" } } }";
            String value = Base64.getEncoder().encodeToString(json.getBytes());

            // 4. Inject into GameProfile
            GameProfile profile = player.getGameProfile();
            profile.getProperties().removeAll("textures");
            profile.getProperties().put("textures", new Property("textures", value, null));

            // 5. Refresh for all clients
            player.getServer().getPlayerManager().sendToAll(
                    new PlayerListS2CPacket(PlayerListS2CPacket.Action.ADD_PLAYER, player)
            );
            LOGGER.info("Applied skin from url:{}", skinUrl);
            return 1;

        } catch (Exception e) {
            LOGGER.info("Couldn't apply skin to {}: {}", player.getName().getString(), e);
            return 2;
        }
    }
    public static void applyDefaultSkin(ServerPlayerEntity player) {
        try {

            Identifier id = Identifier.of("macebot", "textures/bot/skins/default-skin.png");

            String json = "{ \"textures\": { \"SKIN\": { \"url\": \"" + id.toString() + "\" } } }";
            String value = Base64.getEncoder().encodeToString(json.getBytes());

            GameProfile profile = player.getGameProfile();
            profile.getProperties().removeAll("textures");
            profile.getProperties().put("textures", new Property("textures", value, null));

            player.getServer().getPlayerManager().sendToAll(
                    new PlayerListS2CPacket(PlayerListS2CPacket.Action.ADD_PLAYER, player)
            );
            LOGGER.info("Applied skin from resources to {}", player.getName().getString());

        } catch (Exception e) {
            LOGGER.info("Couldn't apply skin to {}: {}", player.getName().getString(), e);
        }
    }
}
