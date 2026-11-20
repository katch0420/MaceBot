package net.katch0420.macebot.main.utils;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.katch0420.macebot.main.macebot.bot.PlayerBot;
import net.katch0420.macebot.main.settings.Settings;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

import static net.katch0420.macebot.main.settings.Settings.hasAccess;

public class PlayerBuffs {
    static int duration = 40;
    public static void applyBuffsToPlayer(ServerPlayer player, boolean saturation){
        player.addEffect(new MobEffectInstance(
                //? if >=1.21.5 {
                MobEffects.STRENGTH
                //?} else
                //MobEffects.DAMAGE_BOOST
                ,duration,1));
        player.addEffect(new MobEffectInstance(
                //? if >=1.21.5 {
                MobEffects.SPEED
                //?} else
                //MobEffects.MOVEMENT_SPEED
                ,duration,1));
        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE,duration,0));
        if(saturation) player.addEffect(new MobEffectInstance(MobEffects.SATURATION,duration,255));
    }

    public static void tick(MinecraftServer s){
        if(s.getTickCount() % 20 == 0){
            PlayerLookup.all(s).forEach(
                    p -> {
                        if(p instanceof PlayerBot && Settings.MACEBOT_BUFFS.getValue()){
                            applyBuffsToPlayer(p, true);
                        } else if(Settings.PLAYER_BUFFS.getValue()){
                            applyBuffsToPlayer(p, false);
                        }
                    }
            );
        }
    }
}
