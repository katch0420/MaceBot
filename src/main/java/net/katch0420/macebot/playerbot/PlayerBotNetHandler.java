package net.katch0420.macebot.playerbot;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;

import java.util.Objects;
import java.util.Set;

public class PlayerBotNetHandler extends ServerPlayNetworkHandler {
    public PlayerBotNetHandler(MinecraftServer server, ClientConnection connection, ServerPlayerEntity serverPlayer, ConnectedClientData clientData) {
        super(server, connection, serverPlayer, clientData);
    }

    @Override
    public void sendPacket(Packet<?> packet) {
        super.sendPacket(packet);
    }

    @Override
    public void disconnect(Text reason) {
        super.disconnect(reason);
        if (reason.getContent() instanceof TranslatableTextContent text && (text.getKey().equals("multiplayer.disconnect.idling") || text.getKey().equals("multiplayer.disconnect.duplicate_login")))
        {
            player.kill();
        }
    }

    @Override
    public void requestTeleport(double x, double y, double z, float yaw, float pitch, Set<PositionFlag> flags) {
        super.requestTeleport(x, y, z, yaw, pitch, flags);
        if(null != Objects.requireNonNull(player.getServer()).getPlayerManager().getPlayer(player.getUuid())){

        }
    }
}