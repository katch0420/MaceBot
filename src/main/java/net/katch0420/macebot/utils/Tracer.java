package net.katch0420.macebot.utils;

import net.minecraft.entity.Entity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.util.Optional;
import java.util.function.Predicate;

public class Tracer
{
    public static HitResult rayTrace(Entity source, float partialTicks, double reach, boolean fluids)
    {
        BlockHitResult blockHit = rayTraceBlocks(source, partialTicks, reach, fluids);
        double maxSqDist = reach * reach;
        if (blockHit != null)
        {
            maxSqDist = blockHit.getBlockPos().getSquaredDistance(source.getEyePos());
        }
        EntityHitResult entityHit = rayTraceEntities(source, partialTicks, reach, maxSqDist);
        return entityHit == null ? blockHit : entityHit;
    }

    public static BlockHitResult rayTraceBlocks(Entity source, float partialTicks, double reach, boolean fluids)
    {
        Vec3d pos = source.getEyePos();
        Vec3d rotation = source.getRotationVec(partialTicks);
        Vec3d reachEnd = pos.add(rotation.x * reach, rotation.y * reach, rotation.z * reach);
        return source.getWorld().raycast(new RaycastContext(pos,reachEnd, RaycastContext.ShapeType.OUTLINE, fluids? RaycastContext.FluidHandling.ANY: RaycastContext.FluidHandling.NONE, source));
    }

    public static EntityHitResult rayTraceEntities(Entity source, float partialTicks, double reach, double maxSqDist)
    {
        Vec3d pos = source.getEyePos();
        Vec3d reachVec = source.getRotationVec(partialTicks).multiply(reach);
        Box box = source.getBoundingBox().stretch(reachVec).expand(1.0);
        return rayTraceEntities(source, pos, pos.add(reachVec), box, e -> !e.isSpectator() && e.isCollidable(), maxSqDist);
    }

    public static EntityHitResult rayTraceEntities(Entity source, Vec3d start, Vec3d end, Box box, Predicate<Entity> predicate, double maxSqDistance)
    {
        World world = source.getWorld();
        double targetDistance = maxSqDistance;
        Entity target = null;
        Vec3d targetHitPos = null;
        for (Entity current : world.getOtherEntities(source, box, predicate))
        {
            Box currentBox = current.getBoundingBox().expand(3.0);
            Optional<Vec3d> currentHit = currentBox.raycast(start, end);
            if (currentBox.contains(start))
            {
                if (targetDistance >= 0)
                {
                    target = current;
                    targetHitPos = currentHit.orElse(start);
                    targetDistance = 0;
                }
            }
            else if (currentHit.isPresent())
            {
                Vec3d currentHitPos = currentHit.get();
                double currentDistance = start.squaredDistanceTo(currentHitPos);
                if (currentDistance < targetDistance || targetDistance == 0)
                {
                    if (current.getRootVehicle() == source.getRootVehicle())
                    {
                        if (targetDistance == 0)
                        {
                            target = current;
                            targetHitPos = currentHitPos;
                        }
                    }
                    else
                    {
                        target = current;
                        targetHitPos = currentHitPos;
                        targetDistance = currentDistance;
                    }
                }
            }
        }
        return target == null ? null : new EntityHitResult(target, targetHitPos);
    }
}
