package net.katch0420.macebot.mixin;

import net.katch0420.macebot.player.PlayerSettings;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Inject(method = "decrementUnlessCreative", at = @At("HEAD"), cancellable = true)
    private void preventDecrementUnlessCreative(int amount, LivingEntity entity, CallbackInfo ci) {
        if (PlayerSettings.autoRefill && entity instanceof ServerPlayerEntity) {
            ci.cancel();
        }
    }
    @Inject(method = "decrement", at = @At("HEAD"), cancellable = true)
    private void preventDecrement(int amount, CallbackInfo ci) {
        if (PlayerSettings.autoRefill) {
            ci.cancel();
        }
    }

}

