package net.katch0420.macebot.main.kits.main;

import net.katch0420.macebot.main.MaceBot;
import net.katch0420.macebot.main.messenger.Logger;
import net.katch0420.macebot.main.utils.YarnHelpers;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
//? if <1.21.5
//import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

import static net.katch0420.macebot.main.MaceBot.server;

import java.util.Optional;

public class KitStack {
    public ItemData itemData;
    public int slot;
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
        Item item = BuiltInRegistries.ITEM
                //? if >=1.21.2 {
                .getValue
                //?} else
                //.get
                        (itemData.id);
        ItemStack stack = new ItemStack(item, count);
        stack.applyComponents(itemData.components);
        return stack;
    }

    public static Holder.Reference<Enchantment> getEnchantmentRegistry(ResourceKey<Enchantment> key) {
        return MaceBot.server.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(key);
    }

    public static void addUnbreakingEnchant(ItemStack stack, int lvl) {
        if (stack.isEmpty()) return;
        stack.enchant(getEnchantmentRegistry(Enchantments.UNBREAKING), lvl);
    }

    public static void addMendingEnchant(ItemStack stack) {
        if (stack.isEmpty()) return;
        stack.enchant(getEnchantmentRegistry(Enchantments.MENDING), 1);
    }

    public static void addUnbreakableComponent(ItemStack stack) {
        if (stack.isEmpty()) return;
        stack.applyComponents(
                DataComponentMap.builder().set(DataComponents.UNBREAKABLE,
                        //? >=1.21.5 {
                        Unit.INSTANCE
                        //?} else
                        //new Unbreakable(true)
                ).build()
        );
    }

    public static KitStack fromStack(ItemStack stack, int slot) {
        if ((stack == null) || (stack.isEmpty() && !stack.is(Items.AIR))) {
            return null;
        }

        ItemData itemData = ItemData.builder()
                .item(stack.getItem())
                .components(new PatchedDataComponentMap(stack.getComponents()))
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