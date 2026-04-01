package net.katch0420.macebot.main.kits.server;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.component.ComponentMap;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public class ItemData {
    public final Identifier id;
    public final ComponentMap components;
    public Map<RegistryKey<Enchantment>, Integer> enchantments;

    private ItemData(Builder builder) {
        this.id = builder.id;
        this.components = builder.components;
        this.enchantments = builder.enchantments;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Identifier id;
        private ComponentMap components = ComponentMap.builder().build();
        private Map<RegistryKey<Enchantment>, Integer> enchantments = new HashMap<>();

        public Builder id(String id) {
            this.id = Identifier.of("minecraft", id);
            return this;
        }

        public Builder id(Identifier id) {
            this.id = id;
            return this;
        }

        public Builder components(ComponentMap components) {
            this.components = components;
            return this;
        }

        public Builder enchantments(Map<RegistryKey<Enchantment>, Integer> enchantments) {
            this.enchantments = enchantments;
            return this;
        }

        public ItemData build() {
            if (id == null) {
                throw new IllegalStateException("Item ID must be set");
            }
            return new ItemData(this);
        }
    }
}