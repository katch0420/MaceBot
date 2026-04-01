package net.katch0420.macebot.main.kits.server;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.katch0420.macebot.main.MaceBot;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.component.type.UnbreakableComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.*;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

import java.util.Map;
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

    public static KitStack createItemData(ItemPresets presets, int slot, int count) {
        return new KitStack(presets.getItemData(), slot, count);
    }

    public static KitStack createItemData(ItemPresets presets, int slot) {
        return new KitStack(presets.getItemData(), slot, 1);
    }

    public static Object getSlot(Integer index) {
        if (index == 40) {
            return EquipmentSlot.OFFHAND;
        } else if (index >= 36 && index < 40) {
            return switch (index) {
                case 36 -> EquipmentSlot.FEET;
                case 37 -> EquipmentSlot.LEGS;
                case 38 -> EquipmentSlot.CHEST;
                case 39 -> EquipmentSlot.HEAD;
                default -> EquipmentSlot.MAINHAND;
            };
        } else {
            return index;
        }
    }

    public ItemStack toStack(PlayerEntity player) {
        Item item = Registries.ITEM.get(itemData.id);
        ItemStack stack = new ItemStack(item, count);

        // Apply non-enchantment components
        if (itemData.components != null && !itemData.components.isEmpty()) {
            stack.applyComponentsFrom(itemData.components);
        }

        // Add enchantments using the PLAYER'S network registry
        if (itemData.enchantments != null && !itemData.enchantments.isEmpty()) {
            Registry<Enchantment> registry = player.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);

            // Sort enchantments by registry ID for proper ordering (1.21 requirement)
            java.util.List<Map.Entry<RegistryEntry<Enchantment>, Integer>> sortedEnchants = new java.util.ArrayList<>();

            for (Map.Entry<RegistryKey<Enchantment>, Integer> entry : itemData.enchantments.entrySet()) {
                registry.getEntry(entry.getKey().getValue()).ifPresent(enchantmentEntry -> {
                    sortedEnchants.add(Map.entry(enchantmentEntry, entry.getValue()));
                });
            }

            // Sort by registry ID
            sortedEnchants.sort((a, b) -> {
                int idA = registry.getRawId(a.getKey().value());
                int idB = registry.getRawId(b.getKey().value());
                return Integer.compare(idA, idB);
            });

            // Build component with sorted enchantments
            ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT);
            for (Map.Entry<RegistryEntry<Enchantment>, Integer> entry : sortedEnchants) {
                builder.add(entry.getKey(), entry.getValue());
            }

            stack.set(DataComponentTypes.ENCHANTMENTS, builder.build());
        }

        return stack;
    }

    public static Optional<RegistryEntry.Reference<Enchantment>> getEnchantmentRegistry(RegistryKey<Enchantment> key) {
        return MaceBot.server
                .getRegistryManager()
                .getOrThrow(RegistryKeys.ENCHANTMENT)
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
                ComponentMap.builder().add(DataComponentTypes.UNBREAKABLE, new UnbreakableComponent(true)).build()
        );
    }

    public static KitStack fromStack(ItemStack stack, int slot) {
        if ((stack == null) || (stack.isEmpty() && !stack.isOf(Items.AIR))) {
            return null;
        }

        Identifier itemId = Registries.ITEM.getId(stack.getItem());

        ItemData itemData = ItemData.builder()
                .id(itemId.getPath())
                .components(stack.getComponents())
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