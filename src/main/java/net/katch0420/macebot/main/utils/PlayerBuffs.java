package net.katch0420.macebot.main.utils;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.katch0420.macebot.main.settings.server.Settings;
import net.katch0420.macebot.main.settings.server.SettingsSyncHelper;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import static net.katch0420.macebot.main.settings.server.Settings.hasAccess;

public class PlayerBuffs {
    static int duration = 40;
    public static void applyBuffsToPlayer(ServerPlayerEntity player){
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH,duration,1));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED,duration,1));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE,duration,0));
    }

    public static void tick(MinecraftServer s){
        if(s.getTicks() % 20 == 0 && Settings.isPlayerBuffsEnabled()){
            PlayerLookup.all(s).forEach(
                    p -> {
                        if(hasAccess(p)) PlayerBuffs.applyBuffsToPlayer(p);
                    }
            );
        }
    }
}
