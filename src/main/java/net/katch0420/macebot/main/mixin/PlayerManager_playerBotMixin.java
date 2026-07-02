package net.katch0420.macebot.main.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.katch0420.macebot.main.macebot.bot.PlayerBot;
import net.katch0420.macebot.main.macebot.bot.PlayerBotNetHandler;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.MinecraftServer;
//? if >=1.21.9
/*import net.minecraft.server.PlayerConfigEntry;*/
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
//? if >=1.21.6 <=1.21.8
/*import net.minecraft.storage.ReadView;*/
import net.minecraft.util.ErrorReporter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(PlayerManager.class)
public abstract class PlayerManager_playerBotMixin
{
    @Shadow
    @Final
    private MinecraftServer server;

    @Inject(method = "loadPlayerData", at = @At(value = "RETURN", shift = At.Shift.BEFORE))
    //? if >=1.21.9 {
    /*private void fixStartingPos(PlayerConfigEntry player, CallbackInfoReturnable<Optional<NbtCompound>> cir)
    {
        if (server.getPlayerManager().getPlayer(player.id()) instanceof PlayerBot p)
    *///?}
    //? if >=1.21.6 <=1.21.8 {
    /*private void fixStartingPos(ServerPlayerEntity player, ErrorReporter errorReporter, CallbackInfoReturnable<Optional<ReadView>> cir)
    {
        if (player instanceof PlayerBot p)
    *///?}
    //? if <=1.21.5 {
    private void fixStartingPos(ServerPlayerEntity player, CallbackInfoReturnable<Optional<NbtCompound>> cir)
    {
        if (player instanceof PlayerBot p)
    //?}
        {
            p.fixStartingPos.run();
        }
    }

    @WrapOperation(
            method = "onPlayerConnect",
            at = @At(
                    value = "NEW",
                    target = "(Lnet/minecraft/server/MinecraftServer;Lnet/minecraft/network/ClientConnection;Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/server/network/ConnectedClientData;)Lnet/minecraft/server/network/ServerPlayNetworkHandler;"
            )
    )
    private ServerPlayNetworkHandler replaceNetworkHandler(MinecraftServer server, ClientConnection connection, ServerPlayerEntity player, ConnectedClientData clientData, Operation<ServerPlayNetworkHandler> original)
    {
        if (player instanceof PlayerBot playerBot)
        {
            return new PlayerBotNetHandler(this.server, connection, playerBot, clientData);
        }
        else
        {
            return original.call(this.server, connection, player, clientData);
        }
    }
}
