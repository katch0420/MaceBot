package net.katch0420.macebot.main.mixin;

//? if <26.2
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
//? if >=1.21.2
//? if >=1.21.9 && <1.21.11
//import net.minecraft.client.renderer.entity.state.HitboxesRenderState;
//? if >=1.21.5 && <1.21.9
//import net.minecraft.client.renderer.entity.state.HitboxRenderState;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EntityRenderDispatcher.class)
public abstract class CombatHitBoxMixin {
    //? if >=1.21.5 && <1.21.9{
    /*@Unique
    private static boolean bot$highlightCurrent = false;

    // ── 1. Runs once per entity, right as its render pass starts. Decides
    //      whether THIS entity's upcoming hitbox draw calls should be recolored. ──
    @Inject(
            method = "render(Lnet/minecraft/world/entity/Entity;DDDFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("HEAD")
    )
    private void bot$onRenderEntity(
            Entity entity, double d, double e, double f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, CallbackInfo ci) {
        EntityRenderDispatcher self = (EntityRenderDispatcher) (Object) this;
        bot$highlightCurrent = entity == self.crosshairPickEntity
                && entity instanceof LivingEntity
                Settings.COMBAT_HITBOX.getValue()
                && Settings.CLIENT.getValue();
    }

    @ModifyArgs(
            method = "renderHitbox(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/client/renderer/entity/state/HitboxRenderState;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/ShapeRenderer;renderLineBox(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;DDDDDDFFFF)V"
            )
    )
    private static void bot$modifyHitboxColor(Args args) {
        if (!bot$highlightCurrent) return;
        int c = Themes.CURRENT.custom_hitbox();
        // index 8/9/10 = red/green/blue based on renderLineBox's param order
        // (poseStack, vertexConsumer, x0,y0,z0,x1,y1,z1, red, green, blue, alpha)
        args.set(8, ColorHelper.getRedF(c));
        args.set(9, ColorHelper.getGreenF(c));
        args.set(10, ColorHelper.getBlueF(c));
    }
    *///?}
    //? if >=1.21.2 <1.21.5{
    /*@ModifyArgs(
            method = "render(Lnet/minecraft/world/entity/Entity;DDDFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/renderer/entity/EntityRenderer;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;renderHitbox(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/world/entity/Entity;FFFF)V"
            )
    )
    private void modifyHitboxColor(Args args) {
        EntityRenderDispatcher self = (EntityRenderDispatcher)(Object)this;
        Entity entity = args.get(2);

        if (entity == self.crosshairPickEntity && entity instanceof LivingEntity && Settings.COMBAT_HITBOX.getValue() && Settings.CLIENT.getValue()) {
            int c = Themes.CURRENT.custom_hitbox();
            args.set(4, ColorHelper.getRedF(c));
            args.set(5, ColorHelper.getGreenF(c));
            args.set(6, ColorHelper.getBlueF(c));
        }
    }
    *///?}
    //? if <1.21.2 {
    /*@ModifyArgs(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;renderHitbox(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/world/entity/Entity;FFFF)V"
            )
    )
    private void modifyHitboxColor(Args args,
                                   Entity entity, double d, double e, double h, float g, float f, PoseStack p, MultiBufferSource m, int i) {
        EntityRenderDispatcher self = (EntityRenderDispatcher)(Object)this;

        if (entity == self.crosshairPickEntity && entity instanceof LivingEntity && Settings.COMBAT_HITBOX.getValue() && Settings.CLIENT.getValue()) {
            int c = Themes.CURRENT.custom_hitbox();
            args.set(4, ColorHelper.getRedF(c));
            args.set(5, ColorHelper.getGreenF(c));
            args.set(6, ColorHelper.getBlueF(c));
        }
    }
    *///?}
}
