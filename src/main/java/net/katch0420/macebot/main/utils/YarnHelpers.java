package net.katch0420.macebot.main.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

public class YarnHelpers {
    public static void playClickSound(){playClickSound(MinecraftClient.getInstance().getSoundManager());}
    public static void playClickSound(SoundManager soundManager) {
        soundManager.play(PositionedSoundInstance.
                //? if >= 1.21.11 {
                /*ui(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                 *///?} else
                    master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    public static void pushMatrix(DrawContext c){
        //? if >=1.21.6
        /*context.getMatrices().pushMatrix();*/
        //? if <=1.21.5
        c.getMatrices().push();
    }

    public static void popMatrix(DrawContext c){
        //? if >=1.21.6
        /*context.getMatrices().popMatrix();*/
        //? if <=1.21.5
        c.getMatrices().pop();
    }

    public static <T> Registry<T> getRegistry(DynamicRegistryManager manager, RegistryKey<? extends Registry<T>> key){
        //? if >=1.21.2 {
        //return manager.getOrThrow(key);
        //?} else
        return manager.get(key);
    }

    public static World getWorld(ServerPlayerEntity p){
        return p.
                //? if >=1.21.9 {
                /*getEntityWorld(),
                 *///?} else
                getWorld();
    }
}
