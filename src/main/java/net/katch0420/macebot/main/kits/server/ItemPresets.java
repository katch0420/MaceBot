package net.katch0420.macebot.main.kits.server;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.potion.Potions;

import java.util.Map;
import java.util.function.Consumer;

public enum ItemPresets {

    TOTEM_OF_UNDYING(
            ItemData.builder()
                    .id("totem_of_undying")
                    .build()
    ),

    ENDER_PEARL(
            ItemData.builder()
                    .id("ender_pearl")
                    .build()
    ),

    WIND_CHARGE(
            ItemData.builder()
                    .id("wind_charge")
                    .build()
    ),

    SHIELD(
            ItemData.builder()
                    .id("shield")
                    .build()
    ),

    ELYTRA(
            ItemData.builder()
                    .id("elytra")
                    .build()
    ),

    FIREWORK_ROCKET(
            ItemData.builder()
                    .id("firework_rocket")
                    .build()
    ),

    GOLDEN_APPLE(
            ItemData.builder()
                    .id("golden_apple")
                    .build()
    ),

    BREEZE_ROD(
            ItemData.builder()
                    .id("breeze_rod")
                    .build()
    ),

    STRONG_STRENGTH_POT(
            ItemData.builder()
                    .id("splash_potion")
                    .components(
                            getComponentMap(
                                    b -> b.add(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(Potions.STRONG_STRENGTH))
                            )
                    )
                    .build()
    ),

    STRONG_SWIFTNESS_POT(
            ItemData.builder()
                    .id("splash_potion")
                    .components(
                            getComponentMap(
                                    b -> b.add(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(Potions.STRONG_SWIFTNESS))
                            )
                    )
                    .build()
    ),

    STRONG_TURTLE_MASTER_POT(
            ItemData.builder()
                    .id("splash_potion")
                    .components(
                            getComponentMap(
                                    b -> b.add(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(Potions.STRONG_TURTLE_MASTER))
                            )
                    )
                    .build()
    ),

    DENSITY_MACE(
            ItemData.builder()
                    .id("mace")
                    .enchantments(Map.of(
                            Enchantments.DENSITY, 5,
                            Enchantments.WIND_BURST, 1
                    ))
                    .build()
    ),

    BREACH_MACE(
            ItemData.builder()
                    .id("mace")
                    .enchantments(Map.of(
                            Enchantments.BREACH, 4
                    ))
                    .build()
    ),

    NETHERITE_SWORD_SHARPNESS(
            ItemData.builder()
                    .id("netherite_sword")
                    .enchantments(Map.of(
                            Enchantments.SHARPNESS, 5,
                            Enchantments.SWEEPING_EDGE, 3,
                            Enchantments.LOOTING, 3
                    ))
                    .build()
    ),

    DIAMOND_SWORD_SHARPNESS(
            ItemData.builder()
                    .id("diamond_sword")
                    .enchantments(Map.of(
                            Enchantments.SHARPNESS, 5,
                            Enchantments.SWEEPING_EDGE, 3,
                            Enchantments.LOOTING, 3
                    ))
                    .build()
    ),

    NETHERITE_AXE_SHARPNESS(
            ItemData.builder()
                    .id("netherite_axe")
                    .enchantments(Map.of(
                            Enchantments.SHARPNESS, 5,
                            Enchantments.EFFICIENCY, 5
                    ))
                    .build()
    ),

    DIAMOND_AXE_SHARPNESS(
            ItemData.builder()
                    .id("diamond_axe")
                    .enchantments(Map.of(
                            Enchantments.SHARPNESS, 5,
                            Enchantments.EFFICIENCY, 5
                    ))
                    .build()
    ),

    NETHERITE_HELMET_PROTECTION(
            ItemData.builder()
                    .id("netherite_helmet")
                    .enchantments(Map.of(
                            Enchantments.PROTECTION, 4,
                            Enchantments.AQUA_AFFINITY, 1,
                            Enchantments.RESPIRATION, 3
                    ))
                    .build()
    ),

    NETHERITE_CHESTPLATE_PROTECTION(
            ItemData.builder()
                    .id("netherite_chestplate")
                    .enchantments(Map.of(
                            Enchantments.PROTECTION, 4
                    ))
                    .build()
    ),

    NETHERITE_LEGGINGS_PROTECTION(
            ItemData.builder()
                    .id("netherite_leggings")
                    .enchantments(Map.of(
                            Enchantments.PROTECTION, 4
                    ))
                    .build()
    ),

    NETHERITE_BOOTS_PROTECTION(
            ItemData.builder()
                    .id("netherite_boots")
                    .enchantments(Map.of(
                            Enchantments.PROTECTION, 4,
                            Enchantments.FEATHER_FALLING, 4,
                            Enchantments.DEPTH_STRIDER, 3
                    ))
                    .build()
    ),

    DIAMOND_HELMET_PROTECTION(
            ItemData.builder()
                    .id("diamond_helmet")
                    .enchantments(Map.of(
                            Enchantments.PROTECTION, 4,
                            Enchantments.AQUA_AFFINITY, 1,
                            Enchantments.RESPIRATION, 3
                    ))
                    .build()
    ),

    DIAMOND_CHESTPLATE_PROTECTION(
            ItemData.builder()
                    .id("diamond_chestplate")
                    .enchantments(Map.of(
                            Enchantments.PROTECTION, 4
                    ))
                    .build()
    ),

    DIAMOND_LEGGINGS_PROTECTION(
            ItemData.builder()
                    .id("diamond_leggings")
                    .enchantments(Map.of(
                            Enchantments.PROTECTION, 4
                    ))
                    .build()
    ),

    DIAMOND_BOOTS_PROTECTION(
            ItemData.builder()
                    .id("diamond_boots")
                    .enchantments(Map.of(
                            Enchantments.PROTECTION, 4,
                            Enchantments.FEATHER_FALLING, 4,
                            Enchantments.DEPTH_STRIDER, 3
                    ))
                    .build()
    );

    private static ComponentMap getComponentMap(Consumer<ComponentMap.Builder> consumer) {
        ComponentMap.Builder builder = ComponentMap.builder();
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