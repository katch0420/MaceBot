package net.katch0420.macebot.main.kits.server;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.katch0420.macebot.main.MaceBot;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

public class KitGiver {
    public static void giveBuiltInKit(ServerPlayerEntity player, Kit kit, boolean unbreakable){
        kit.getItems().forEach(
                (index, kitStack) -> {
                    ItemStack stack = kitStack.toStack(player);
                    if(stack.isDamageable()){
                        if(unbreakable){
                            KitStack.addUnbreakableComponent(stack);
                        } else {
                            KitStack.addUnbreakingEnchant(stack, 3);
                            KitStack.addMendingEnchant(stack);
                        }
                    }

                    Object slot = KitStack.getSlot(index);
                    if(slot instanceof Integer slotInt){
                        player.getInventory().setStack(slotInt, stack);
                    } else if (slot instanceof EquipmentSlot equipmentSlot){
                        player.equipStack(equipmentSlot, stack);
                    }
                }
        );
    }

    public static void giveKit(ServerPlayerEntity player, String kitId, boolean unbreaking, boolean unbreakable, boolean mending){
        Kit kit = KitRegistry.get(kitId);
        if(kit != null){
            kit.getItems().forEach(
                    (index, kitStack) -> {
                        ItemStack stack = kitStack.toStack(player);
                        if(stack.isDamageable()){
                            if(unbreakable){
                                KitStack.addUnbreakableComponent(stack);
                            } else {
                                if(unbreaking) KitStack.addUnbreakingEnchant(stack, 3);
                                if(mending) KitStack.addMendingEnchant(stack);
                            }
                        }

                        Object slot = KitStack.getSlot(index);
                        if(slot instanceof Integer inventorySlot){
                            player.getInventory().setStack(inventorySlot, stack);
                        } else if (slot instanceof EquipmentSlot equipmentSlot){
                            player.equipStack(equipmentSlot, stack);
                        }
                    }
            );
        }
    }

    public static void legacyGiveKit(ServerPlayerEntity player, String kitId, boolean unbreakable){
        giveKit(player,kitId,true,unbreakable,true);
    }
}
