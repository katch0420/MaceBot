package net.katch0420.macebot.main.kits.server;

import net.katch0420.macebot.main.kits.main.Kit;
import net.katch0420.macebot.main.kits.main.KitStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class KitGiver {
    public static void giveKit(ServerPlayer player, Kit kit, boolean unbreakable) {
        giveKit(player, kit, unbreakable, true, true);
    }

    public static void giveKit(ServerPlayer player, String kitId, boolean unbreakable) {
        giveKit(player, kitId, unbreakable, true, true);
    }

    public static void giveKit(ServerPlayer player, String kitId, boolean unbreakable, boolean unbreaking, boolean mending) {
        Kit kit = KitRegistry.get(kitId);
        giveKit(player, kit, unbreakable, unbreaking, mending);
    }

    public static void giveKit(ServerPlayer player, Kit kit, boolean unbreakable, boolean unbreaking, boolean mending) {
        if (kit != null && player != null) {
            kit.getItems().forEach((index, kitStack) -> {
                ItemStack stack = kitStack.toStack();
                if (stack.isDamageableItem()) {
                    if (unbreakable) {
                        KitStack.addUnbreakableComponent(stack);
                    } else {
                        if (unbreaking) KitStack.addUnbreakingEnchant(stack, 3);
                        if (mending) KitStack.addMendingEnchant(stack);
                    }
                }

                Object slot = KitStack.getSlot(index);
                if (slot instanceof Integer inventorySlot) {
                    player.getInventory().setItem(inventorySlot, stack);
                } else if (slot instanceof EquipmentSlot equipmentSlot) {
                    player.setItemSlot(equipmentSlot, stack);
                }
            });
        }
    }
}
