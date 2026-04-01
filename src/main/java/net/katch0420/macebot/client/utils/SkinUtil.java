package net.katch0420.macebot.client.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.util.Identifier;

public class SkinUtil {
    public static Identifier getPlayerSkin(ClientPlayerEntity player) {
        return player.getSkinTextures().texture();
    }
}
