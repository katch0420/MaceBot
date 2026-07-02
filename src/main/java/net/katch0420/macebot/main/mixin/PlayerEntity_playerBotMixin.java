package net.katch0420.macebot.main.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.katch0420.macebot.main.macebot.bot.PlayerBot;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntity_playerBotMixin {

    @WrapOperation(
            //? if >= 1.21.11 {
            /*method = "knockbackTarget",
            *///?} else {
            method = "attack",
            //?}
            at = @At(
                    value = "FIELD",
                    //? if >= 1.21.11 {
                    /*target = "Lnet/minecraft/entity/Entity;knockedBack:Z",
                    *///?} else {
                    target = "Lnet/minecraft/entity/Entity;velocityModified:Z",
                    //?}
                    ordinal = 0
            )
    )
    private boolean knockedBackAndNotPlayerBot(Entity instance, Operation<Boolean> original) {
        return original.call(instance) && !(instance instanceof PlayerBot);
    }
}

