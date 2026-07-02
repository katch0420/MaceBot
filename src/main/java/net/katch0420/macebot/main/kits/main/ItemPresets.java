package net.katch0420.macebot.main.kits.main;

import net.minecraft.component.ComponentChanges;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.potion.Potions;

import java.util.Map;
import java.util.function.Consumer;

public enum ItemPresets {

    TOTEM_OF_UNDYING(
            ItemData.fromItem(Items.TOTEM_OF_UNDYING)
    ),

    ENDER_PEARL(
            ItemData.fromItem(Items.ENDER_PEARL)
    ),

    WIND_CHARGE(
            ItemData.fromItem(Items.WIND_CHARGE)
    ),

    SHIELD(
            ItemData.fromItem(Items.SHIELD)
    ),

    ELYTRA(
            ItemData.fromItem(Items.ELYTRA)
    ),

    FIREWORK_ROCKET(
            ItemData.fromItem(Items.FIREWORK_ROCKET)
    ),

    GOLDEN_APPLE(
            ItemData.fromItem(Items.GOLDEN_APPLE)
    ),

    BREEZE_ROD(
            ItemData.fromItem(Items.BREEZE_ROD)
    ),

    STRONG_STRENGTH_POT(
            ItemData.builder()
                    .item(Items.SPLASH_POTION)
                    .componentChanges(
                            getComponentChanges(
                                    b -> b.add(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(Potions.STRONG_STRENGTH))
                            )
                    )
                    .build()
    ),

    STRONG_SWIFTNESS_POT(
            ItemData.builder()
                    .item(Items.SPLASH_POTION)
                    .componentChanges(
                            getComponentChanges(
                                    b -> b.add(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(Potions.STRONG_SWIFTNESS))
                            )
                    )
                    .build()
    ),

    STRONG_TURTLE_MASTER_POT(
            ItemData.builder()
                    .item(Items.SPLASH_POTION)
                    .componentChanges(
                            getComponentChanges(
                                    b -> b.add(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(Potions.STRONG_TURTLE_MASTER))
                            )
                    )
                    .build()
    ),

    DENSITY_MACE(
            ItemData.builder()
                    .item(Items.MACE)
                    .enchantments(Map.of(
                            Enchantments.DENSITY, 5,
                            Enchantments.WIND_BURST, 1
                    ))
                    .build()
    ),

    BREACH_MACE(
            ItemData.builder()
                    .item(Items.MACE)
                    .enchantments(Map.of(
                            Enchantments.BREACH, 4
                    ))
                    .build()
    ),

    NETHERITE_SWORD_SHARPNESS(
            ItemData.builder()
                    .item(Items.NETHERITE_SWORD)
                    .enchantments(Map.of(
                            Enchantments.SHARPNESS, 5,
                            Enchantments.SWEEPING_EDGE, 3,
                            Enchantments.LOOTING, 3
                    ))
                    .build()
    ),

    DIAMOND_SWORD_SHARPNESS(
            ItemData.builder()
                    .item(Items.DIAMOND_SWORD)
                    .enchantments(Map.of(
                            Enchantments.SHARPNESS, 5,
                            Enchantments.SWEEPING_EDGE, 3,
                            Enchantments.LOOTING, 3
                    ))
                    .build()
    ),

    NETHERITE_AXE_SHARPNESS(
            ItemData.builder()
                    .item(Items.NETHERITE_AXE)
                    .enchantments(Map.of(
                            Enchantments.SHARPNESS, 5,
                            Enchantments.EFFICIENCY, 5
                    ))
                    .build()
    ),

    DIAMOND_AXE_SHARPNESS(
            ItemData.builder()
                    .item(Items.DIAMOND_AXE)
                    .enchantments(Map.of(
                            Enchantments.SHARPNESS, 5,
                            Enchantments.EFFICIENCY, 5
                    ))
                    .build()
    ),

    NETHERITE_HELMET_PROTECTION(
            ItemData.builder()
                    .item(Items.NETHERITE_HELMET)
                    .enchantments(Map.of(
                            Enchantments.PROTECTION, 4,
                            Enchantments.AQUA_AFFINITY, 1,
                            Enchantments.RESPIRATION, 3
                    ))
                    .build()
    ),

    NETHERITE_CHESTPLATE_PROTECTION(
            ItemData.builder()
                    .item(Items.NETHERITE_CHESTPLATE)
                    .enchantments(Map.of(
                            Enchantments.PROTECTION, 4
                    ))
                    .build()
    ),

    NETHERITE_LEGGINGS_PROTECTION(
            ItemData.builder()
                    .item(Items.NETHERITE_LEGGINGS)
                    .enchantments(Map.of(
                            Enchantments.PROTECTION, 4
                    ))
                    .build()
    ),

    NETHERITE_BOOTS_PROTECTION(
            ItemData.builder()
                    .item(Items.NETHERITE_BOOTS)
                    .enchantments(Map.of(
                            Enchantments.PROTECTION, 4,
                            Enchantments.FEATHER_FALLING, 4,
                            Enchantments.DEPTH_STRIDER, 3
                    ))
                    .build()
    ),

    DIAMOND_HELMET_PROTECTION(
            ItemData.builder()
                    .item(Items.DIAMOND_HELMET)
                    .enchantments(Map.of(
                            Enchantments.PROTECTION, 4,
                            Enchantments.AQUA_AFFINITY, 1,
                            Enchantments.RESPIRATION, 3
                    ))
                    .build()
    ),

    DIAMOND_CHESTPLATE_PROTECTION(
            ItemData.builder()
                    .item(Items.DIAMOND_CHESTPLATE)
                    .enchantments(Map.of(
                            Enchantments.PROTECTION, 4
                    ))
                    .build()
    ),

    DIAMOND_LEGGINGS_PROTECTION(
            ItemData.builder()
                    .item(Items.DIAMOND_LEGGINGS)
                    .enchantments(Map.of(
                            Enchantments.PROTECTION, 4
                    ))
                    .build()
    ),

    DIAMOND_BOOTS_PROTECTION(
            ItemData.builder()
                    .item(Items.DIAMOND_BOOTS)
                    .enchantments(Map.of(
                            Enchantments.PROTECTION, 4,
                            Enchantments.FEATHER_FALLING, 4,
                            Enchantments.DEPTH_STRIDER, 3
                    ))
                    .build()
    );

    private static ComponentChanges getComponentChanges(Consumer<ComponentChanges.Builder> consumer) {
        ComponentChanges.Builder builder = ComponentChanges.builder();
        consumer.accept(builder);
        return builder.build();
    }

    private final ItemData itemData;

    ItemPresets(ItemData itemData) {
        this.itemData = itemData;
    }

    public ItemData getItemData() {
        return itemData;
    }
}