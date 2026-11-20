package net.katch0420.macebot.client.utils;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import net.katch0420.macebot.main.utils.YarnHelpers;
import net.minecraft.client.Minecraft;
//? <=1.21.11 {
import net.minecraft.client.gui.GuiGraphics;
//?} else
//import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.GameRenderer;
//? if >=1.21.2 && <1.21.6
//import net.minecraft.client.renderer.RenderType;
//? if >=1.21.6
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
//? if >=1.21.6
import net.minecraft.client.renderer.entity.state.EntityRenderState;
//? if >=1.21.11
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.awt.*;

public class RenderUtils {

    public static void drawBorder(@UnknownNullability
                                //? if >1.21.11 {
                                  /*GuiGraphicsExtractor c
                                *///?} else
                                GuiGraphics c
            , int x, int y, int width, int height, int color){
        c.fill(x, y, x + width, y + 1, color);
        c.fill(x, y + height - 1, x + width, y + height, color);
        c.fill(x, y + 1, x + 1, y + height - 1, color);
        c.fill(x + width - 1, y + 1, x + width, y + height - 1, color);
    }

    //? if >1.21.11 {
    /*public static void drawEntity(
            final GuiGraphicsExtractor graphics,
            final int x0,
            final int y0,
            final int x1,
            final int y1,
            final int size,
            final float offsetY,
            final float mouseX,
            final float mouseY,
            final LivingEntity entity
    ) {
        float centerX = (x0 + x1) / 2.0F;
        float centerY = (y0 + y1) / 2.0F;
        float xAngle = (float)Math.atan((centerX - mouseX) / 40.0F);
        float yAngle = (float)Math.atan((centerY - mouseY) / 40.0F);
        Quaternionf rotation = new Quaternionf().rotateZ((float) Math.PI);
        Quaternionf xRotation = new Quaternionf().rotateX(yAngle * 20.0F * (float) (Math.PI / 180.0));
        rotation.mul(xRotation);
        EntityRenderState renderState = extractRenderState(entity);
        if (renderState instanceof LivingEntityRenderState livingRenderState) {
            livingRenderState.bodyRot = 180.0F + xAngle * 20.0F;
            livingRenderState.yRot = xAngle * 20.0F;
            if (livingRenderState.pose != Pose.FALL_FLYING) {
                livingRenderState.xRot = -yAngle * 20.0F;
            } else {
                livingRenderState.xRot = 0.0F;
            }

            livingRenderState.boundingBoxWidth = livingRenderState.boundingBoxWidth / livingRenderState.scale;
            livingRenderState.boundingBoxHeight = livingRenderState.boundingBoxHeight / livingRenderState.scale;
            livingRenderState.scale = 1.0F;
        }

        Vector3f translation = new Vector3f(0.0F, renderState.boundingBoxHeight / 2.0F + offsetY, 0.0F);
        graphics.entity(renderState, size, translation, rotation, xRotation, x0, y0, x1, y1);
    }

    private static EntityRenderState extractRenderState(final LivingEntity entity) {
        EntityRenderDispatcher entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        EntityRenderer<? super LivingEntity, ?> renderer = entityRenderDispatcher.getRenderer(entity);
        EntityRenderState renderState = renderer.createRenderState(entity, 1.0F);
        renderState.shadowPieces.clear();
        renderState.outlineColor = 0;
        return renderState;
    }
    *///?}

    //? if =1.21.11 {
    public static void drawEntity(
            GuiGraphics guiGraphics, int i, int j, int k, int l, int m, float f, float g, float h, LivingEntity livingEntity
    ) {
        float n = (i + k) / 2.0F;
        float o = (j + l) / 2.0F;
        float p = (float)Math.atan((n - g) / 40.0F);
        float q = (float)Math.atan((o - h) / 40.0F);
        Quaternionf quaternionf = new Quaternionf().rotateZ((float) Math.PI);
        Quaternionf quaternionf2 = new Quaternionf().rotateX(q * 20.0F * (float) (Math.PI / 180.0));
        quaternionf.mul(quaternionf2);
        EntityRenderState entityRenderState = extractRenderState(livingEntity);
        if (entityRenderState instanceof LivingEntityRenderState livingEntityRenderState) {
            livingEntityRenderState.bodyRot = 180.0F + p * 20.0F;
            livingEntityRenderState.yRot = p * 20.0F;
            if (livingEntityRenderState.pose != Pose.FALL_FLYING) {
                livingEntityRenderState.xRot = -q * 20.0F;
            } else {
                livingEntityRenderState.xRot = 0.0F;
            }

            livingEntityRenderState.boundingBoxWidth = livingEntityRenderState.boundingBoxWidth / livingEntityRenderState.scale;
            livingEntityRenderState.boundingBoxHeight = livingEntityRenderState.boundingBoxHeight / livingEntityRenderState.scale;
            livingEntityRenderState.scale = 1.0F;
        }

        Vector3f vector3f = new Vector3f(0.0F, entityRenderState.boundingBoxHeight / 2.0F + f, 0.0F);
        guiGraphics.submitEntityRenderState(entityRenderState, m, vector3f, quaternionf, quaternionf2, i, j, k, l);
    }

    private static EntityRenderState extractRenderState(LivingEntity livingEntity) {
        EntityRenderDispatcher entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        EntityRenderer<? super LivingEntity, ?> entityRenderer = entityRenderDispatcher.getRenderer(livingEntity);
        EntityRenderState entityRenderState = entityRenderer.createRenderState(livingEntity, 1.0F);
        entityRenderState.lightCoords = 15728880;
        entityRenderState.shadowPieces.clear();
        entityRenderState.outlineColor = 0;
        return entityRenderState;
    }
    //?}

    //? if <1.21.6 {
    /*public static void drawEntity(GuiGraphics context, int x1, int y1, int x2, int y2, int size, float f, float mouseX, float mouseY, LivingEntity entity) {
        float g = (x1 + x2) / 2.0F;
        float h = (y1 + y2) / 2.0F;
        context.enableScissor(x1, y1, x2, y2);
        float i = (float)Math.atan((g - mouseX) / 40.0F);
        float j = (float)Math.atan((h - mouseY) / 40.0F);
        Quaternionf quaternionf = new Quaternionf().rotateZ((float) Math.PI);
        Quaternionf quaternionf2 = new Quaternionf().rotateX(j * 20.0F * (float) (Math.PI / 180.0));
        quaternionf.mul(quaternionf2);
        float k = entity.yBodyRot;
        float l = entity.getYRot();
        float m = entity.getXRot();
        float n = entity.yHeadRotO;
        float o = entity.yHeadRot;
        entity.yBodyRot = 180.0F + i * 20.0F;
        entity.setYRot(180.0F + i * 40.0F);
        entity.setXRot(-j * 20.0F);
        entity.yHeadRot = entity.getYRot();
        entity.yHeadRotO = entity.getYRot();
        float p = entity.getScale();
        Vector3f vector3f = new Vector3f(0.0F, entity.getBbHeight() / 2.0F + f * p, 0.0F);
        float q = size / p;
        drawEntity(context, g, h, q, vector3f, quaternionf, quaternionf2, entity);
        entity.yBodyRot = k;
        entity.setYRot(l);
        entity.setXRot(m);
        entity.yHeadRotO = n;
        entity.yHeadRot = o;
        context.disableScissor();
    }
    *///?}
    //? if >=1.21.6 && <1.21.11 {
    /*public static void drawEntity(GuiGraphics guiGraphics, int i, int j, int k, int l, int m, float f, float g, float h, LivingEntity livingEntity) {
        float n = (float)(i + k) / 2.0F;
        float o = (float)(j + l) / 2.0F;
        guiGraphics.enableScissor(i, j, k, l);
        float p = (float)Math.atan((double)((n - g) / 40.0F));
        float q = (float)Math.atan((double)((o - h) / 40.0F));
        Quaternionf quaternionf = (new Quaternionf()).rotateZ((float)Math.PI);
        Quaternionf quaternionf2 = (new Quaternionf()).rotateX(q * 20.0F * ((float)Math.PI / 180F));
        quaternionf.mul(quaternionf2);
        float r = livingEntity.yBodyRot;
        float s = livingEntity.getYRot();
        float t = livingEntity.getXRot();
        float u = livingEntity.yHeadRotO;
        float v = livingEntity.yHeadRot;
        livingEntity.yBodyRot = 180.0F + p * 20.0F;
        livingEntity.setYRot(180.0F + p * 40.0F);
        livingEntity.setXRot(-q * 20.0F);
        livingEntity.yHeadRot = livingEntity.getYRot();
        livingEntity.yHeadRotO = livingEntity.getYRot();
        float w = livingEntity.getScale();
        Vector3f vector3f = new Vector3f(0.0F, livingEntity.getBbHeight() / 2.0F + f * w, 0.0F);
        float x = (float)m / w;
        drawEntity(guiGraphics, i, j, k, l, x, vector3f, quaternionf, quaternionf2, livingEntity);
        livingEntity.yBodyRot = r;
        livingEntity.setYRot(s);
        livingEntity.setXRot(t);
        livingEntity.yHeadRotO = u;
        livingEntity.yHeadRot = v;
        guiGraphics.disableScissor();
    }
    *///?}
    //? >=1.21.10 && <1.21.11 {
    /*public static void drawEntity(GuiGraphics guiGraphics, int i, int j, int k, int l, float f, Vector3f vector3f, Quaternionf quaternionf, @Nullable Quaternionf quaternionf2, LivingEntity livingEntity) {
        EntityRenderDispatcher entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        EntityRenderer<? super LivingEntity, ?> entityRenderer = entityRenderDispatcher.getRenderer(livingEntity);
        EntityRenderState entityRenderState = entityRenderer.createRenderState(livingEntity, 1.0F);
        entityRenderState.lightCoords = 15728880;
        entityRenderState.hitboxesRenderState = null;
        entityRenderState.shadowPieces.clear();
        entityRenderState.outlineColor = 0;
        guiGraphics.submitEntityRenderState(entityRenderState, f, vector3f, quaternionf, quaternionf2, i, j, k, l);
    }
    *///?}
    //? >=1.21.6 && <1.21.9{
    /*public static void drawEntity(GuiGraphics guiGraphics, int i, int j, int k, int l, float f, Vector3f vector3f, Quaternionf quaternionf, @Nullable Quaternionf quaternionf2, LivingEntity livingEntity) {
        EntityRenderDispatcher entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        EntityRenderer<? super LivingEntity, ?> entityRenderer = entityRenderDispatcher.getRenderer(livingEntity);
        EntityRenderState entityRenderState = entityRenderer.createRenderState(livingEntity, 1.0F);
        entityRenderState.hitboxesRenderState = null;
        guiGraphics.submitEntityRenderState(entityRenderState, f, vector3f, quaternionf, quaternionf2, i, j, k, l);
    }
    *///?}
    //? <1.21.6 {
    /*public static void drawEntity(GuiGraphics context, float x, float y, float size, Vector3f vector3f, Quaternionf quaternionf, @Nullable Quaternionf quaternionf2, LivingEntity entity) {
        YarnHelpers.pushMatrix(context);
        context.pose().translate((double)x, (double)y, 50.0);
        context.pose().scale(size, size, -size);
        context.pose().translate(vector3f.x, vector3f.y, vector3f.z);
        context.pose().mulPose(quaternionf);
        Lighting.setupForEntityInInventory();
        EntityRenderDispatcher entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        if (quaternionf2 != null) {
            entityRenderDispatcher.overrideCameraOrientation(quaternionf2.conjugate(new Quaternionf()).rotateY((float) Math.PI));
        }



        entityRenderDispatcher.setRenderShadow(false);
        *///?}
        //? if >=1.21.2 && < 1.21.6
        //context.drawSpecial((multiBufferSource) -> entityRenderDispatcher.render(entity, (double)0.0F, (double)0.0F, (double)0.0F, 1.0F, context.pose(), multiBufferSource, 15728880));
        //? if < 1.21.2
        //RenderSystem.runAsFancy(() -> entityRenderDispatcher.render(entity, 0.0, 0.0, 0.0, 0.0F, 1.0F, context.pose(), context.bufferSource(), 15728880));
        //? if <1.21.6 {
        /*context.flush();
        entityRenderDispatcher.setRenderShadow(true);
        YarnHelpers.popMatrix(context);
        Lighting.setupFor3DItems();
    }
    *///?}
    //? if >1.21.11 {
    /*public static void setOrigin2d(GuiGraphicsExtractor c, int x, int y) {
        YarnHelpers.pushMatrix(c);
        //? if >=1.21.6 {
        /^c.pose().translate(x,y);
        ^///?} else
        c.pose().translate(x,y,0);
    }

    public static void resetOrigin(GuiGraphicsExtractor c) {
        YarnHelpers.popMatrix(c);
    }
    *///?}
    //? if <=1.21.11 {
    public static void setOrigin2d(GuiGraphics c, int x, int y) {
        YarnHelpers.pushMatrix(c);
        //? if >=1.21.6 {
        c.pose().translate(x,y);
        //?} else
        //c.pose().translate(x,y,0);
    }

    public static void resetOrigin(GuiGraphics c) {
        YarnHelpers.popMatrix(c);
    }
    //?}
    //? if >1.21.11 {
    /*public static void drawTexture(GuiGraphicsExtractor ctx,
    *///?} else
    public static void drawTexture(GuiGraphics ctx,
                                   Identifier texture, int x, int y, int u, int v, int width, int height, int textureWidth, int textureHeight) {
        ctx.blit(
                /*? if >=1.21.2 && < 1.21.6 >>*//*RenderType::guiTextured,*/ /*/^*/ /*? if >=1.21.6 >>*/RenderPipelines.GUI_TEXTURED, /**/
                texture,
                x,y,
                u,v,
                width,
                height,
                textureWidth,
                textureHeight
        );
    }
}