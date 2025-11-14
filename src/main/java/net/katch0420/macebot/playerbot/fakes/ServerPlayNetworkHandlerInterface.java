package net.katch0420.macebot.playerbot.fakes;

import net.minecraft.network.ClientConnection;

public interface ServerPlayNetworkHandlerInterface {
    ClientConnection getConnection();
}