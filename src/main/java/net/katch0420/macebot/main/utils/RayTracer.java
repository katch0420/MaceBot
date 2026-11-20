package net.katch0420.macebot.main.utils;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.VoxelShape;

public class RayTracer {

    public static Entity rayTraceEntity(ServerPlayer source){
        return rayTraceEntity(source,source.entityInteractionRange());
    }

    public static Entity rayTraceEntity(ServerPlayer source, double entityInteractionRange){
        HitResult hr = rayTraceHitResult(source, entityInteractionRange);
        Entity e;
        if(hr instanceof EntityHitResult ehr){
            e = ehr.getEntity();
        } else {
            e = null;
        }
        return e;
    }
    public static BlockHitResult rayTraceBlock(ServerPlayer source){
        HitResult hr = rayTraceHitResult(source, source.entityInteractionRange());
        return hr instanceof BlockHitResult bhr ? bhr : BlockHitResult.miss(null,null,null);
    }
    public static HitResult rayTraceHitResult(ServerPlayer source, double entityInteractionRange){
        double d = source.blockInteractionRange();
        double e = entityInteractionRange;
        float f = 0.5f;
        Entity entity = source.getCamera();

        double g = Math.max(d, e);
        double h = Mth.square(g);
        Vec3 vec3 = entity.getEyePosition(f);
        HitResult hitResult = entity.pick(g, f, false);
        double i = hitResult.getLocation().distanceToSqr(vec3);
        if (hitResult.getType() != net.minecraft.world.phys.HitResult.Type.MISS) {
            h = i;
            g = Math.sqrt(i);
        }

        Vec3 vec32 = entity.getViewVector(f);
        Vec3 vec33 = vec3.add(vec32.x * g, vec32.y * g, vec32.z * g);
        float j = 1.0F;
        AABB aABB = entity.getBoundingBox().expandTowards(vec32.scale(g)).inflate(1.0F, 1.0F, 1.0F);
        EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(entity, vec3, vec33, aABB, (entityx) -> !entityx.isSpectator() && entityx.isPickable(), h);
        return entityHitResult != null && entityHitResult.getLocation().distanceToSqr(vec3) < i ? filterHitResult(entityHitResult, vec3, e) : filterHitResult(hitResult, vec3, d);
    }
    private static HitResult filterHitResult(HitResult hitResult, Vec3 vec3, double d) {
        Vec3 vec32 = hitResult.getLocation();
        if (!vec32.closerThan(vec3, d)) {
            Vec3 vec33 = hitResult.getLocation();
            Direction direction = Direction
                    //? if >=1.21.2 {
                    .getApproximateNearest(vec33.x - vec3.x, vec33.y - vec3.y, vec33.z - vec3.z);
                    //?} else
                    //.getNearest(vec33.x - vec3.x, vec33.y - vec3.y, vec33.z - vec3.z);
            return BlockHitResult.miss(vec33, direction, BlockPos.containing(vec33));
        } else {
            return hitResult;
        }
    }
    public static double getDistanceToGround(Entity entity) {
        AABB box = entity.getBoundingBox();
        double bottomY = box.minY;
        Vec3 start = new Vec3(entity.getX(), bottomY, entity.getZ());
        Vec3 end = new Vec3(entity.getX(), bottomY - 16, entity.getZ());
        BlockHitResult hit =
                entity.level().clip(new ClipContext(
                start,
                end,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                entity
        ));
        if (hit.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = hit.getBlockPos();
            VoxelShape shape = entity.level().getBlockState(pos).getCollisionShape(entity.level(), pos);
            double groundY = pos.getY() + shape.max(Direction.Axis.Y);
            return bottomY - groundY;
        }
        return 16;
    }
}
