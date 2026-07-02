package net.katch0420.macebot.client.macebot;

import net.katch0420.macebot.main.MaceBot;
import net.katch0420.macebot.main.settings.client.ClientSideSettings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.component.ComponentType;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

import java.util.List;

public class MacebotDataGetter {

    public static OtherClientPlayerEntity macebot;

    public static LivingEntity getMacebot() {
        assert MinecraftClient.getInstance().world != null;
        Entity entity = MinecraftClient.getInstance().world.getEntityById(ClientSideSettings.getMacebotId());
        macebot = entity instanceof LivingEntity bot ? (OtherClientPlayerEntity) bot : null;
        return macebot;
    }

    public static Text getDisplayName() {
        return macebot == null ? Text.of("N/A") : macebot.getDisplayName();
    }

    public static float getHealth() {
        return macebot == null ? -1 : macebot.getHealth();
    }

    public static float getMaxHealth() {
        return macebot == null ? -1 : macebot.getMaxHealth();
    }

    public static float getHunger(){
        return macebot == null ? -1 : macebot.getHungerManager().getFoodLevel();
    }

    public static List<StatusEffect> getStatusEffects() {
        List<StatusEffect> list = new java.util.ArrayList<>();
        if(macebot != null) macebot.getActiveStatusEffects().forEach((key, value) -> list.add(value.getEffectType().value()));
        return list;
    }

    public static ClientPlayerEntity getTarget() {
        assert MinecraftClient.getInstance().world != null;
        Entity entity = MinecraftClient.getInstance().world.getPlayers().stream().filter(p -> p.getId() == ClientSideSettings.getOpponentId()).findFirst().orElse(null);
        return entity instanceof ClientPlayerEntity player ? player : null;
    }
}
