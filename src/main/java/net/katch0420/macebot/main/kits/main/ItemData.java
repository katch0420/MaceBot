package net.katch0420.macebot.main.kits.main;

import net.katch0420.macebot.main.MaceBot;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Map;

public class ItemData {
    public final Identifier id;
    public final PatchedDataComponentMap components;

    private ItemData(Builder builder) {
        this.id = builder.id;
        this.components = builder.components;
    }

    private ItemData(Identifier id, PatchedDataComponentMap components){
        this.id = id;
        this.components = components;
    }

    public static ItemData fromItem(Item item){
        return new ItemData(BuiltInRegistries.ITEM.getKey(item), (PatchedDataComponentMap) new ItemStack(item).getComponents());
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Identifier id;
        private PatchedDataComponentMap components = new PatchedDataComponentMap(DataComponentMap.EMPTY);

        public Builder item(Item item) {
            this.id = BuiltInRegistries.ITEM.getKey(item);
            return this;
        }

        public Builder id(Identifier id) {
            this.id = id;
            return this;
        }

        public Builder components(PatchedDataComponentMap components){
            this.components = components;
            return this;
        }

        public Builder componentChanges(DataComponentPatch changes){
            components.applyPatch(changes);
            return this;
        }

        public Builder enchantments(Map<ResourceKey<Enchantment>, Integer> enchantments) {
            components.set(DataComponents.ENCHANTMENTS,getItemEnchantmentComponent(enchantments));
            return this;
        }

        private ItemEnchantments getItemEnchantmentComponent(@UnknownNullability Map<ResourceKey<Enchantment>, Integer> enchantments) {
            ItemEnchantments.Mutable b = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
            Registry<Enchantment> registry = MaceBot.server.registryAccess()
                    //? if >=1.21.2 {
                    .lookupOrThrow
                    //?} else
                    //.registryOrThrow
                            (Registries.ENCHANTMENT);
            enchantments.forEach((e, i) -> b.set(registry
                    //? if >=1.21.2 {
                    .getOrThrow
                    //?} else
                    //.getHolderOrThrow
                            (e), i));
            return b.toImmutable();
        }

        public ItemData build() {
            if (id == null) {
                throw new IllegalStateException("Item Type must be set");
            }
            return new ItemData(this);
        }
    }
}