package net.katch0420.macebot.main.mixin;

import net.katch0420.macebot.main.macebot.bot.PlayerBot;
import net.katch0420.macebot.main.macebot.bot.PlayerBotNetHandler;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(PlayerManager.class)
public abstract class PlayerManager_playerBotMixin
{
    @Shadow
    @Final
    private MinecraftServer server;

    @Inject(method = "loadPlayerData", at = @At(value = "RETURN", shift = At.Shift.BEFORE))
    private void fixStartingPos(ServerPlayerEntity player, CallbackInfoReturnable<Optional<NbtCompound>> cir)
    {
        if (player instanceof PlayerBot)
        {
            ((PlayerBot) player).fixStartingPos.run();
        }
    }

    @Redirect(
            method = "onPlayerConnect",
            at = @At(
                    value = "NEW",
                    target = "(Lnet/minecraft/server/MinecraftServer;Lnet/minecraft/network/ClientConnection;Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/server/network/ConnectedClientData;)Lnet/minecraft/server/network/ServerPlayNetworkHandler;"
            )
    )
    private ServerPlayNetworkHandler replaceNetworkHandler(MinecraftServer server, ClientConnection connection, ServerPlayerEntity player, ConnectedClientData clientData)
    {
        if (player instanceof PlayerBot playerBot)
        {
            return new PlayerBotNetHandler(this.server, connection, playerBot, clientData);
        }
        else
        {
            return new ServerPlayNetworkHandler(this.server, connection, player, clientData);
        }
    }
}
