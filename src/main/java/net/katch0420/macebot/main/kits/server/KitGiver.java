package net.katch0420.macebot.main.kits.server;

import net.katch0420.macebot.main.kits.main.Kit;
import net.katch0420.macebot.main.kits.main.KitStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

public class KitGiver {
    public static void giveKit(ServerPlayerEntity player, Kit kit, boolean unbreakable) {
        giveKit(player, kit, true, unbreakable, true);
    }

    public static void giveKit(ServerPlayerEntity player, String kitId, boolean unbreaking, boolean unbreakable, boolean mending) {
        Kit kit = KitRegistry.get(kitId);
        giveKit(player, kit, unbreaking, unbreakable, mending);
    }

    public static void giveKit(ServerPlayerEntity player, Kit kit, boolean unbreaking, boolean unbreakable, boolean mending) {
        if (kit != null) {
            kit.getItems().forEach((index, kitStack) -> {
                ItemStack stack = kitStack.toStack();
                if (stack.isDamageable()) {
                    if (unbreakable) {
                        KitStack.addUnbreakableComponent(stack);
                    } else {
                        if (unbreaking) KitStack.addUnbreakingEnchant(stack, 3);
                        if (mending) KitStack.addMendingEnchant(stack);
                    }
                }

                Object slot = KitStack.getSlot(index);
                if (slot instanceof Integer inventorySlot) {
                    player.getInventory().setStack(inventorySlot, stack);
                } else if (slot instanceof EquipmentSlot equipmentSlot) {
                    player.equipStack(equipmentSlot, stack);
                }
            });
        }
    }
}
