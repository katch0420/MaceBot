package net.katch0420.macebot.main.kits.main;

import net.katch0420.macebot.main.MaceBot;
import net.katch0420.macebot.main.utils.RegistryHelper;
import net.minecraft.component.*;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public class ItemData {
    public final Identifier id;
    public final ComponentMapImpl components;

    private ItemData(Builder builder) {
        this.id = builder.id;
        this.components = builder.components;
    }

    private ItemData(Identifier id, ComponentMapImpl components){
        this.id = id;
        this.components = components;
    }

    public static ItemData fromItem(Item item){
        return new ItemData(Registries.ITEM.getId(item), (ComponentMapImpl) new ItemStack(item).getComponents());
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Identifier id;
        private ComponentMapImpl components = new ComponentMapImpl(ComponentMap.EMPTY);

        public Builder item(Item item) {
            this.id = Registries.ITEM.getId(item);
            return this;
        }

        public Builder id(Identifier id) {
            this.id = id;
            return this;
        }

        public Builder components(ComponentMapImpl components){
            this.components = components;
            return this;
        }

        public Builder componentChanges(ComponentChanges changes){
            components.applyChanges(changes);
            return this;
        }

        public Builder enchantments(Map<RegistryKey<Enchantment>, Integer> enchantments) {
            components.set(DataComponentTypes.ENCHANTMENTS,getItemEnchantmentComponent(enchantments));
            return this;
        }

        private ItemEnchantmentsComponent getItemEnchantmentComponent(Map<RegistryKey<Enchantment>, Integer> enchantments) {
            ItemEnchantmentsComponent.Builder b = new ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT);
            Registry<Enchantment> registry = RegistryHelper.getEnchantmentRegistry(MaceBot.server);
            enchantments.forEach((e, i) -> b.add(registry.entryOf(e), i));
            return b.build();
        }

        public ItemData build() {
            if (id == null) {
                throw new IllegalStateException("Item ID must be set");
            }
            return new ItemData(this);
        }
    }
}