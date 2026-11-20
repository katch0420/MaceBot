package net.katch0420.macebot.main.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.katch0420.macebot.main.macebot.bot.PlayerBot;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Player.class)
public abstract class PlayerEntity_playerBotMixin {
    //? if <1.21.11 {
    /*@WrapOperation(
            method = "attack",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/entity/Entity;hurtMarked:Z",
                    ordinal = 0
            )
    )
    private boolean knockedBackAndNotPlayerBot(Entity instance, Operation<Boolean> original) {
        return original.call(instance) && !(instance instanceof PlayerBot);
    }
    *///?} else {
    @WrapOperation(
            method = "causeExtraKnockback",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/entity/Entity;hurtMarked:Z",
                    ordinal = 0
            )
    )
    private boolean knockedBackAndNotPlayerBot(Entity instance, Operation<Boolean> original) {
        return original.call(instance) && !(instance instanceof PlayerBot);
    }
    //?}
}

