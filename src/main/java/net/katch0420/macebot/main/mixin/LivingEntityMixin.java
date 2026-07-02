package net.katch0420.macebot.main.mixin;

import net.katch0420.macebot.main.macebot.bot.PlayerBot;
import net.katch0420.macebot.main.settings.server.Settings;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import static net.katch0420.macebot.main.settings.server.Settings.hasAccess;


@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Redirect(
            //? if >=1.21.2 {
            /*method = "tryUseDeathProtector",
            *///?} else
            method = "tryUseTotem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemStack;decrement(I)V"
            )
    )
    private void onTotemUse(ItemStack instance, int amount){
        LivingEntity self = (LivingEntity)(Object)this;
        if(self instanceof ServerPlayerEntity sp){
            if(self instanceof PlayerBot){
                if(!Settings.isMacebotAutoRefillEnabled()) instance.decrement(amount);
            } else {
                if(!Settings.isPlayerAutoRefillEnabled() && hasAccess(sp)) instance.decrement(amount);
            }
        } else {
            instance.decrement(amount);
        }
    }
}
