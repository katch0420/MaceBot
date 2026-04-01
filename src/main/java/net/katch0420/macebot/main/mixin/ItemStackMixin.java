package net.katch0420.macebot.main.mixin;

import net.katch0420.macebot.main.macebot.bot.PlayerBot;
import net.katch0420.macebot.main.settings.server.Settings;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Inject(method = "decrementUnlessCreative", at = @At("HEAD"), cancellable = true)
    private void preventDecrementUnlessCreative(int amount, LivingEntity entity, CallbackInfo ci) {
        if (!(entity instanceof ServerPlayerEntity player)) return;

        ItemStack self = (ItemStack)(Object)this;
        if (!self.isDamageable()) {
            if (player instanceof PlayerBot && Settings.isMacebotAutoRefillEnabled()) {
                ci.cancel();
            }
            else if (Settings.isPlayerAutoRefillEnabled()) {
                ci.cancel();
            }
        }
        player.playerScreenHandler.updateToClient();
    }
}
