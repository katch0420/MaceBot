package net.katch0420.macebot.main.macebot.bot;

import io.netty.channel.embedded.EmbeddedChannel;
import net.katch0420.macebot.main.mixin.ConnectionAccessor;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.ProtocolInfo;
import net.minecraft.network.protocol.PacketFlow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

public class PlayerBotConnection extends Connection {

    private static final Logger log = LoggerFactory.getLogger(PlayerBotConnection.class);
    private final EmbeddedChannel embeddedChannel = new EmbeddedChannel();
    public PlayerBotConnection(PacketFlow side) {
        super(side);
        try {
            ((ConnectionAccessor) this).setChannel(embeddedChannel);
        } catch (Exception e) {
            log.error("Failed to inject channel", e);
        }
    }

    @Override
    public void handleDisconnection() {
    }

    @Override
    public void setListenerForServerboundHandshake(PacketListener packetListener) {
    }

    @Override
    public void setReadOnly() {
    }

    @Override
    public boolean isConnected() {
        return true;
    }

    @Override
    public <T extends PacketListener> void setupInboundProtocol(ProtocolInfo<T> state, T packetListener) {
    }
}
