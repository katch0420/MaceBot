package net.katch0420.macebot.main.utils;

import net.minecraft.server.level.ServerPlayer;

import java.util.Set;

public class TeleportHelper {
    public static void teleportAboveGround(float distance, ServerPlayer entity){
        double currentDis = RayTracer.getDistanceToGround(entity);
        entity.teleportTo(entity
                        //? if >=1.21.6 {
                        .level()
                        //?} else
                        //.serverLevel()
                , entity.getX(), entity.getY() + distance - currentDis, entity.getZ(), Set.of(), entity.getYRot(), entity.getXRot()
                //? if >=1.21.2
                , false
        );
    }
}
