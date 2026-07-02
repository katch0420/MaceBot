package net.katch0420.macebot.main.macebot.bot;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;

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
            player.kill(
                    //? if >=1.21.9
                    /*player.getEntityWorld()*/
                    //? if >=1.21.6 <=1.21.8
                    /*player.getWorld()*/
                    //? if >=1.21.2 <=1.21.5
                    /*player.getServerWorld()*/
            );
        }
    }

    // NetHandlerPlayServerFake
    // PlayerBotNetHandler.java
    @Override
    public void requestTeleport(double x, double y, double z, float yaw, float pitch) {
        super.requestTeleport(x, y, z, yaw, pitch);
        syncWithPlayerPosition(); // equivalent of resetPosition() in your version
    }
}