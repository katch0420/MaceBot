package net.katch0420.macebot.main.mixin;

import net.katch0420.macebot.main.macebot.bot.PlayerBot;
import net.katch0420.macebot.main.settings.Settings;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import static net.katch0420.macebot.main.settings.Settings.hasAccess;


@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Redirect(
            method = "checkTotemDeathProtection",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"
            )
    )
    private void onTotemUse(ItemStack instance, int amount){
        LivingEntity self = (LivingEntity)(Object)this;
        if(self instanceof ServerPlayer sp){
            if(self instanceof PlayerBot){
                if(!Settings.MACEBOT_AUTO_REFILL.getValue()) instance.shrink(amount);
            } else {
                if(!Settings.PLAYER_AUTO_REFILL.getValue() && hasAccess(sp, false) && Settings.SERVER.getValue()) instance.shrink(amount);
            }
        } else {
            instance.shrink(amount);
        }
    }
}
