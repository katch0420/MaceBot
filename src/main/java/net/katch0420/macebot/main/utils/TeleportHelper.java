package net.katch0420.macebot.main.utils;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.Set;

public class TeleportHelper {
    public static void teleportAboveGround(float distance, ServerPlayerEntity entity){
        double currentDis = RayTracer.getDistanceToGround(entity);
        entity.teleport(entity.getServerWorld(), entity.getX(), entity.getY() + distance - currentDis, entity.getZ(), Set.of(), entity.getYaw(), entity.getPitch());
    }
}
