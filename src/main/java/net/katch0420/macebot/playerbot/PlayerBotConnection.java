package net.katch0420.macebot.playerbot;

import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.NetworkState;
import net.minecraft.network.listener.PacketListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

public class PlayerBotConnection extends ClientConnection {

    public PlayerBotConnection(NetworkSide side) {
        super(side);
    }

    private static final Logger log = LoggerFactory.getLogger(PlayerBotConnection.class);
    EmbeddedChannel embeddedChannel = new EmbeddedChannel();
    Field channelField;

    {
        try {

            channelField = ClientConnection.class.getDeclaredField("channel");
            channelField.setAccessible(true);
            channelField.set(this, embeddedChannel);
        } catch (Exception e) {
            log.error("e: ", e);
        }
    }

    @Override
    public void handleDisconnection() {
    }

    @Override
    public void setInitialPacketListener(PacketListener packetListener) {
    }

    @Override
    public void tryDisableAutoRead() {
    }

    @Override
    public <T extends PacketListener> void transitionInbound(NetworkState<T> state, T packetListener) {
    }
}
