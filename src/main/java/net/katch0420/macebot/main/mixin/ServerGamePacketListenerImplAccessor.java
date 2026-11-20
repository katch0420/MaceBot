package net.katch0420.macebot.main.mixin;

import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerGamePacketListenerImpl.class)
public interface ServerGamePacketListenerImplAccessor {
    @Accessor("awaitingPositionFromClient")
    void setAwaitingPositionFromClient(Vec3 awaitingPosition);

    @Accessor("awaitingPositionFromClient")
    Vec3 getAwaitingPositionFromClient();
}
