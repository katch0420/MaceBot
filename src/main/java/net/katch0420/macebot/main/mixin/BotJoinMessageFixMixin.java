package net.katch0420.macebot.main.mixin;

import net.katch0420.macebot.main.macebot.bot.BotConfig;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public class BotJoinMessageFixMixin {
    //? if >=1.21.9 {
    @Inject(
            method = "placeNewPlayer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/players/UserNameToIdResolver;add(Lnet/minecraft/server/players/NameAndId;)V",
                    shift = At.Shift.AFTER
            )
    )
    //?} else {
    /*@Inject(
            method = "placeNewPlayer",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/authlib/GameProfile;getName()Ljava/lang/String;",
                    shift = At.Shift.AFTER
            )
    )
    *///?}
    private void afterNameSet(Connection connection, ServerPlayer player, CommonListenerCookie commonListenerCookie, CallbackInfo ci) {
        player.setCustomName(BotConfig.CUSTOM_NAME);
        player.setCustomNameVisible(true);
    }
}

