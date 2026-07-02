package net.katch0420.macebot.main.kits.main;

import net.katch0420.macebot.main.messenger.Logger;
import net.katch0420.macebot.main.utils.YarnHelpers;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentMapImpl;
import net.minecraft.component.DataComponentTypes;
//? if <=1.21.4
import net.minecraft.component.type.UnbreakableComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.*;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

import static net.katch0420.macebot.main.MaceBot.server;

import java.util.Optional;

public class KitStack {
    public ItemData itemData;
    public Object slot;
    public int count;
    public KitStack(ItemData itemData, int slot, int count) {
        this.itemData = itemData;
        this.slot = slot;
        this.count = count;
    }

    public static KitStack stack(ItemPresets presets, int slot, int count) {
        return new KitStack(presets.getItemData(), slot, count);
    }

    public static KitStack stack(ItemPresets presets, int slot) {
        return new KitStack(presets.getItemData(), slot, 1);
    }

    public static Object getSlot(Integer index) {
        Logger.debug("Invalid Slot Index", KitStack.class, index > 40);
        if (index >= 36) {
            return switch (index) {
                case 36 -> EquipmentSlot.FEET;
                case 37 -> EquipmentSlot.LEGS;
                case 38 -> EquipmentSlot.CHEST;
                case 39 -> EquipmentSlot.HEAD;
                case 40 -> EquipmentSlot.OFFHAND;
                default -> EquipmentSlot.MAINHAND;
            };
        } else {
            return index;
        }
    }

    public ItemStack toStack() {
        Item item = Registries.ITEM.get(itemData.id);
        ItemStack stack = new ItemStack(item, count);
        stack.applyComponentsFrom(itemData.components);
        return stack;
    }

    public static Optional<RegistryEntry.Reference<Enchantment>> getEnchantmentRegistry(RegistryKey<Enchantment> key) {
        return YarnHelpers.getRegistry(server.getRegistryManager(),RegistryKeys.ENCHANTMENT)
                .getEntry(key.getValue());
    }

    public static void addUnbreakingEnchant(ItemStack stack, int lvl) {
        if (stack.isEmpty()) return;
        getEnchantmentRegistry(Enchantments.UNBREAKING).ifPresent(e -> stack.addEnchantment(e, lvl));
    }

    public static void addMendingEnchant(ItemStack stack) {
        if (stack.isEmpty()) return;
        getEnchantmentRegistry(Enchantments.MENDING).ifPresent(e -> stack.addEnchantment(e, 1));
    }

    public static void addUnbreakableComponent(ItemStack stack) {
        if (stack.isEmpty()) return;
        stack.applyComponentsFrom(
                ComponentMap.builder().add(DataComponentTypes.UNBREAKABLE,
                        //? if >=1.21.5 {
                        /*Unit.INSTANCE
                        *///?} else
                        new UnbreakableComponent(true)
                ).build()
        );
    }

    public static KitStack fromStack(ItemStack stack, int slot) {
        if ((stack == null) || (stack.isEmpty() && !stack.isOf(Items.AIR))) {
            return null;
        }

        ItemData itemData = ItemData.builder()
                .item(stack.getItem())
                .components(new ComponentMapImpl(stack.getComponents()))
                .build();

        return new KitStack(itemData, slot, stack.getCount());
    }

    public String getItemId() {
        return itemData.id.getPath();
    }

    public int getCount() {
        return this.count;
    }
}