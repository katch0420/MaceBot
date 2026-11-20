package net.katch0420.macebot.main.mixin;

import net.katch0420.macebot.main.macebot.bot.PlayerBot;
import net.katch0420.macebot.main.settings.Settings;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.katch0420.macebot.main.settings.Settings.hasAccess;


@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Inject(method = "consume", at = @At("HEAD"), cancellable = true)
    private void preventDecrementUnlessCreative(int amount, LivingEntity entity, CallbackInfo ci) {
        if (!(entity instanceof ServerPlayer player)) return;

        ItemStack self = (ItemStack)(Object)this;
        if (!self.isDamageableItem()) {
            if (player instanceof PlayerBot && Settings.MACEBOT_AUTO_REFILL.getValue()) {
                ci.cancel();
            }
            else if (Settings.PLAYER_AUTO_REFILL.getValue() && hasAccess(player, false) && Settings.SERVER.getValue()) {
                ci.cancel();
            }
        }
        player.containerMenu.broadcastFullState();
    }
}
