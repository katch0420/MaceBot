package net.katch0420.macebot.main.mixin;

import net.katch0420.macebot.client.gui.themes.Themes;
import net.katch0420.macebot.main.settings.Settings;import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
//? >=1.21.11 {
import net.minecraft.client.renderer.debug.EntityHitboxDebugRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(EntityHitboxDebugRenderer.class)
public abstract class EntityHitboxDebugRendererMixin {
    @ModifyVariable(
            method = "showHitboxes",
            at = @At("STORE"),
            // first int stored
            name = "i")
    private int modifyHitboxColor(int i, Entity entity, float f, boolean bl) {
        if (!bl && Minecraft.getInstance().crosshairPickEntity == entity && entity instanceof LivingEntity
                && Settings.COMBAT_HITBOX.getValue()
                && Settings.CLIENT.getValue()) {
            return Themes.CURRENT.custom_hitbox(); // your custom color
        }
        return i;
    }
}
//?} else {
/*@Mixin(Minecraft.class)
public class EntityHitboxDebugRendererMixin {}
*///?}



