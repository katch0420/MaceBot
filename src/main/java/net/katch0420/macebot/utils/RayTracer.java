package net.katch0420.macebot.utils;

import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.RaycastContext;

public class RayTracer {
    public static Entity rayTraceEntity(ServerPlayerEntity source){
        float a = 0.5F;
        HitResult hr = rayTraceHitResult(source,a,false);
        Entity e;
        if(hr instanceof EntityHitResult){
            EntityHitResult ehr = (EntityHitResult) hr;
            e = ehr.getEntity();
        } else {
            e = null;
        }
        return e;
    }
    public static BlockHitResult rayTraceBlock(ServerPlayerEntity source, boolean fluid){
        float a = 0.5F;
        HitResult hr = rayTraceHitResult(source, a, fluid);
        return hr instanceof BlockHitResult bhr ? bhr : null;
    }
    public static HitResult rayTraceHitResult(ServerPlayerEntity source, float tickDelta, boolean fluid){
        double a = source.getBlockInteractionRange();
        double b = source.getEntityInteractionRange();
        Entity c = source.getCameraEntity();

        double d = Math.max(a,b);
        double e = MathHelper.square(d);
        Vec3d vec3d = c.getCameraPosVec(tickDelta);
        HitResult hitResult = c.raycast(d, tickDelta, fluid);
        double f = hitResult.getPos().squaredDistanceTo(vec3d);
        if(hitResult.getType() != HitResult.Type.MISS){
            e = f;
            d = Math.sqrt(f);
        }

        Vec3d vec3d2 = c.getRotationVec(tickDelta);
        Vec3d vec3d3 = vec3d.add(vec3d2.x * d, vec3d2.y * d, vec3d2.z * d);
        Box box = c.getBoundingBox().stretch(vec3d2.multiply(d)).expand(1.0,1.0,1.0);
        EntityHitResult entityHitResult = ProjectileUtil.raycast(c,vec3d, vec3d3,box, (entity) -> !entity.isSpectator() && entity.canHit(), e);
        return entityHitResult != null && entityHitResult.getPos().squaredDistanceTo(vec3d) < f
                ? ensureTargetInRange(entityHitResult, vec3d, b)
                : ensureTargetInRange(hitResult, vec3d, a);
    }
    private static HitResult ensureTargetInRange(HitResult hitResult, Vec3d cameraPos, double interactionRange) {
        Vec3d vec3d = hitResult.getPos();
        if (!vec3d.isInRange(cameraPos, interactionRange)) {
            Vec3d vec3d2 = hitResult.getPos();
            Direction direction = Direction.getFacing(vec3d2.x - cameraPos.x, vec3d2.y - cameraPos.y, vec3d2.z - cameraPos.z);
            return BlockHitResult.createMissed(vec3d2, direction, BlockPos.ofFloored(vec3d2));
        } else {
            return hitResult;
        }
    }
    public static double getDistanceToGround(Entity entity) {
        Box box = entity.getBoundingBox();
        double bottomY = box.minY;
        Vec3d start = new Vec3d(entity.getX(), bottomY, entity.getZ());
        Vec3d end = new Vec3d(entity.getX(), bottomY - 16, entity.getZ());
        BlockHitResult hit = entity.getWorld().raycast(new RaycastContext(
                start,
                end,
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE,
                entity
        ));
        if (hit.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = hit.getBlockPos();
            VoxelShape shape = entity.getWorld().getBlockState(pos).getCollisionShape(entity.getWorld(), pos);
            double groundY = pos.getY() + shape.getMax(Direction.Axis.Y);
            return bottomY - groundY;
        }
        return 16;
    }


}
