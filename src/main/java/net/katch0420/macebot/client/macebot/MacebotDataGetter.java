package net.katch0420.macebot.client.macebot;

import net.katch0420.macebot.main.settings.Settings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

public class MacebotDataGetter {

    public static RemotePlayer macebot;

    public static LivingEntity getMacebot() {
        assert Minecraft.getInstance().level != null;
        Entity entity = Minecraft.getInstance().level.getEntity(Settings.MACEBOT_ID.getValue());
        macebot = entity instanceof LivingEntity bot ? (RemotePlayer) bot : null;
        return macebot;
    }

    public static Component getDisplayName() {
        return macebot == null ? Component.nullToEmpty("N/A") : macebot.getDisplayName();
    }

    public static float getHealth() {
        return macebot == null ? -1 : macebot.getHealth();
    }

    public static float getMaxHealth() {
        return macebot == null ? -1 : macebot.getMaxHealth();
    }

    public static float getHunger(){
        return macebot == null ? -1 : macebot.getFoodData().getFoodLevel();
    }

    public static List<MobEffect> getStatusEffects() {
        List<MobEffect> list = new java.util.ArrayList<>();
        if(macebot != null) macebot.getActiveEffectsMap().forEach((key, value) -> list.add(value.getEffect().value()));
        return list;
    }

    public static LocalPlayer getTarget() {
        assert Minecraft.getInstance().level != null;
        Entity entity = Minecraft.getInstance().level.players().stream().filter(p -> p.getId() == Settings.OPPONENT_ID.getValue()).findFirst().orElse(null);
        return entity instanceof LocalPlayer player ? player : null;
    }
}
