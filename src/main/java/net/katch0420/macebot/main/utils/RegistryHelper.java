package net.katch0420.macebot.main.utils;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public class RegistryHelper {
    public static <R> Registry<R> getRegistry(DynamicRegistryManager m, RegistryKey<? extends Registry<R>> k){
        return YarnHelpers.getRegistry(m,k);
    }

    public static <R> Registry<R> getRegistry(MinecraftServer s, RegistryKey<? extends Registry<R>> k){
        return getRegistry(s.getRegistryManager(),k);
    }

    public static <R> Registry<R> getRegistry(ServerPlayerEntity p, RegistryKey<? extends Registry<R>> k){
        return getRegistry(p.getRegistryManager(),k);
    }

    public static Registry<Enchantment> getEnchantmentRegistry(ServerPlayerEntity p){
        return getRegistry(p.getRegistryManager(), RegistryKeys.ENCHANTMENT);
    }

    public static Registry<Enchantment> getEnchantmentRegistry(MinecraftServer s){
        return getRegistry(s.getRegistryManager(), RegistryKeys.ENCHANTMENT);
    }
}
