package net.katch0420.macebot.main.macebot.bot;

import net.katch0420.macebot.main.mixin.ServerGamePacketListenerImplAccessor;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

public class PlayerBotNetHandler extends ServerGamePacketListenerImpl {

    private static final Logger log = LoggerFactory.getLogger(PlayerBotNetHandler.class);

    // requestedTeleportPos is private with no setter, and is only ever
    // cleared by onTeleportConfirm() — which only fires when a real client
    // sends back TeleportConfirmC2SPacket. Our bot has no real client, so
    // without this, the first teleport() call (e.g. BotSpawner's initial
    // placement) would leave requestedTeleportPos set forever, and
    // onPlayerMove()'s handlePendingTeleport() guard would silently no-op
    // every movement replay from that point on.

    public PlayerBotNetHandler(MinecraftServer server, Connection connection, ServerPlayer serverPlayer, CommonListenerCookie clientData) {
        super(server, connection, serverPlayer, clientData);
    }

    @Override
    public void send(Packet<?> packet) {
        super.send(packet);
    }

    @Override
    public void disconnect(Component reason) {
        super.disconnect(reason);
        if (reason.getContents() instanceof TranslatableContents text && (text.getKey().equals("multiplayer.kick.idling") || text.getKey().equals("multiplayer.kick.duplicate_login")))
        {
            player.kill(
                    //? if >=1.21.6
                    player.level()
                    //? if >=1.21.2 && <1.21.6
                    //player.serverLevel()
            );
        }
    }

    // NetHandlerPlayServerFake
    // PlayerBotNetHandler.java
    @Override
    public void teleport(double x, double y, double z, float yaw, float pitch) {
        super.teleport(x, y, z, yaw, pitch);
        resetPosition();
        selfConfirmTeleport();
    }

    private void selfConfirmTeleport() {
        try {
            ((ServerGamePacketListenerImplAccessor) this).setAwaitingPositionFromClient(null);
        } catch (Exception e) {
            log.error("[MaceBot] Failed to self-confirm teleport", e);
        }
    }
}