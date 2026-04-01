package net.katch0420.macebot.main.kits.server;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;

import java.util.HashMap;
import java.util.Map;

public class KitInventoryWrapper implements Inventory {
    private Kit sourceKit;
    private Kit kit;
    private final PlayerEntity player;

    public KitInventoryWrapper(Kit kit, PlayerEntity player) {
        this.sourceKit = kit;
        this.kit = sourceKit.copy();
        this.player = player;
    }

    @Override
    public int size() {
        return 41; // offhand + armor + inventory + hotbar
    }

    @Override
    public boolean isEmpty() {
        return sourceKit.getItems().isEmpty();
    }

    @Override
    public ItemStack getStack(int slot) {
        KitStack stack = sourceKit.getItems().get(slot);
        if (stack != null) {
            ItemStack data = stack.toStack(player);
            return data == null ? ItemStack.EMPTY : data;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeStack(int slot) {
        KitStack kitStack = sourceKit.getItems().remove(slot);
        if (kitStack != null) {
            ItemStack removed = kitStack.toStack(player);
            return removed != null ? removed : ItemStack.EMPTY;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        KitStack kitStack = sourceKit.getItems().get(slot);
        if (kitStack != null) {
            ItemStack data = kitStack.toStack(player);
            if (data != null) {
                if (data.getCount() <= amount) {
                    return removeStack(slot);
                } else {
                    kitStack.count -= amount;
                    ItemStack result = data.copy();
                    result.setCount(amount);
                    return result;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        if (stack.isEmpty()) {
            sourceKit.getItems().remove(slot);
        } else {
            sourceKit.getItems().put(slot, KitStack.fromStack(stack, slot));
        }
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return true;
    }

    @Override
    public void markDirty() {
    }

    @Override
    public void clear() {
        sourceKit.clear();
    }

    public void save(){
        sourceKit = kit.copy();
    }

    public void reset(){
        kit = sourceKit.copy();
    }

    public static Map<RegistryEntry<Enchantment>, Integer> getEnchantsFromStack(ItemStack stack) {
        Map<RegistryEntry<Enchantment>, Integer> enchants = new HashMap<>();
        ItemEnchantmentsComponent enchantmentsComponent = stack.getEnchantments();
        enchantmentsComponent.getEnchantments().forEach(e -> enchants.put(e, enchantmentsComponent.getLevel(e)));
        return enchants;
    }
}