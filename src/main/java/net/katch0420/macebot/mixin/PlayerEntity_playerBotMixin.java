package net.katch0420.macebot.mixin;

import net.katch0420.macebot.playerbot.PlayerBot;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntity_playerBotMixin
{
    @Redirect(
        method = "attack",
        at = @At(
                value = "FIELD",
                target = "Lnet/minecraft/entity/Entity;velocityModified:Z",
                ordinal = 0
        )
    )
    private boolean velocityModifiedAndNotPlayerBot(Entity target){
        return target.velocityModified && !(target instanceof PlayerBot);
    }
}
