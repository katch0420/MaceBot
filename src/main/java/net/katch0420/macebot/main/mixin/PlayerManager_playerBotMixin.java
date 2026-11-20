package net.katch0420.macebot.main.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.katch0420.macebot.main.macebot.bot.PlayerBot;
import net.katch0420.macebot.main.macebot.bot.PlayerBotNetHandler;
//? if >=1.21.9 {
import net.minecraft.server.players.NameAndId;
import net.minecraft.nbt.CompoundTag;
//?}
import net.minecraft.network.Connection;
import net.minecraft.network.chat.contents.NbtContents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.ProblemReporter;
//? if >=1.21.6
import net.minecraft.world.level.storage.ValueInput;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(PlayerList.class)
public abstract class PlayerManager_playerBotMixin
{
    @Shadow
    @Final
    private MinecraftServer server;
    //? if >= 1.21.9 {
    @Inject(method = "loadPlayerData", at = @At(value = "RETURN", shift = At.Shift.BEFORE))
    private void fixStartingPos(NameAndId nameAndId, CallbackInfoReturnable<Optional<CompoundTag>> cir) {
        if (PlayerBot.playerBot != null && nameAndId.id().equals(PlayerBot.playerBot.getUUID())) {
            PlayerBot.playerBot.fixStartingPos.run();
        }
    }
    //?}
    //? if >=1.21.6 && <1.21.9 {
    /*@Inject(method = "load", at = @At(value = "RETURN", shift = At.Shift.BEFORE))
    private void fixStartingPos(ServerPlayer serverPlayer, ProblemReporter problemReporter, CallbackInfoReturnable<Optional<ValueInput>> cir)
    {
        if (serverPlayer instanceof PlayerBot p)
        {
            p.fixStartingPos.run();
        }
    }
    *///?}
    //? if <1.21.6 {
    /*@Inject(method = "load", at = @At(value = "RETURN", shift = At.Shift.BEFORE))
    private void fixStartingPos(ServerPlayer player, CallbackInfoReturnable<Optional<NbtContents>> cir)
    {
        if (player instanceof PlayerBot p)
        {
            p.fixStartingPos.run();
        }
    }
    *///?}

    @WrapOperation(
            method = "placeNewPlayer",
            at = @At(
                    value = "NEW",
                    target = "(Lnet/minecraft/server/MinecraftServer;Lnet/minecraft/network/Connection;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/server/network/CommonListenerCookie;)Lnet/minecraft/server/network/ServerGamePacketListenerImpl;"
            )
    )
    private ServerGamePacketListenerImpl replaceNetworkHandler(MinecraftServer minecraftServer, Connection connection, ServerPlayer serverPlayer, CommonListenerCookie commonListenerCookie, Operation<ServerGamePacketListenerImpl> original)
    {
        if (serverPlayer instanceof PlayerBot playerBot)
        {
            return new PlayerBotNetHandler(this.server, connection, playerBot, commonListenerCookie);
        }
        else
        {
            return original.call(this.server, connection, serverPlayer, commonListenerCookie);
        }
    }
}
