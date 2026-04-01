package net.katch0420.macebot.main.kits.server;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.List;

public class BuiltInKitLoader {

    public static void registerAll() {
        netheriteMaceKit();
        diamondMaceKit();
    }

    private static void netheriteMaceKit(){
        Kit netherite_mace = new Kit("netherite_mace", "Netherite Mace Kit", "netherite_sword", false);
        List<KitStack> netherite_mace_items = List.of(
                KitStack.createItemData(ItemPresets.NETHERITE_SWORD_SHARPNESS, 0),
                KitStack.createItemData(ItemPresets.ENDER_PEARL, 1, 16),
                KitStack.createItemData(ItemPresets.SHIELD, 2),
                KitStack.createItemData(ItemPresets.BREACH_MACE, 3),
                KitStack.createItemData(ItemPresets.DENSITY_MACE, 4),
                KitStack.createItemData(ItemPresets.ELYTRA,5),
                KitStack.createItemData(ItemPresets.WIND_CHARGE,6,64),
                KitStack.createItemData(ItemPresets.GOLDEN_APPLE,7,64),
                KitStack.createItemData(ItemPresets.NETHERITE_AXE_SHARPNESS, 8),

                KitStack.createItemData(ItemPresets.TOTEM_OF_UNDYING,9),
                KitStack.createItemData(ItemPresets.ENDER_PEARL,10,16),
                KitStack.createItemData(ItemPresets.BREEZE_ROD,11,64),
                KitStack.createItemData(ItemPresets.GOLDEN_APPLE,12,64),
                KitStack.createItemData(ItemPresets.STRONG_STRENGTH_POT,13),
                KitStack.createItemData(ItemPresets.STRONG_SWIFTNESS_POT,14),
                KitStack.createItemData(ItemPresets.STRONG_STRENGTH_POT,15),
                KitStack.createItemData(ItemPresets.STRONG_SWIFTNESS_POT, 16),
                KitStack.createItemData(ItemPresets.STRONG_TURTLE_MASTER_POT,17),

                KitStack.createItemData(ItemPresets.TOTEM_OF_UNDYING,18),
                KitStack.createItemData(ItemPresets.ENDER_PEARL,19,16),
                KitStack.createItemData(ItemPresets.STRONG_STRENGTH_POT,20),
                KitStack.createItemData(ItemPresets.STRONG_SWIFTNESS_POT,21),
                KitStack.createItemData(ItemPresets.STRONG_STRENGTH_POT,22),
                KitStack.createItemData(ItemPresets.STRONG_SWIFTNESS_POT,23),
                KitStack.createItemData(ItemPresets.STRONG_STRENGTH_POT,24),
                KitStack.createItemData(ItemPresets.STRONG_SWIFTNESS_POT, 25),
                KitStack.createItemData(ItemPresets.STRONG_TURTLE_MASTER_POT,26),

                KitStack.createItemData(ItemPresets.TOTEM_OF_UNDYING,27),
                KitStack.createItemData(ItemPresets.ENDER_PEARL,28,16),
                KitStack.createItemData(ItemPresets.STRONG_STRENGTH_POT,29),
                KitStack.createItemData(ItemPresets.STRONG_SWIFTNESS_POT,30),
                KitStack.createItemData(ItemPresets.STRONG_STRENGTH_POT,31),
                KitStack.createItemData(ItemPresets.STRONG_SWIFTNESS_POT,32),
                KitStack.createItemData(ItemPresets.STRONG_STRENGTH_POT,33),
                KitStack.createItemData(ItemPresets.STRONG_SWIFTNESS_POT, 34),
                KitStack.createItemData(ItemPresets.STRONG_TURTLE_MASTER_POT,35),

                KitStack.createItemData(ItemPresets.NETHERITE_BOOTS_PROTECTION,36),
                KitStack.createItemData(ItemPresets.NETHERITE_LEGGINGS_PROTECTION, 37),
                KitStack.createItemData(ItemPresets.NETHERITE_CHESTPLATE_PROTECTION,38),
                KitStack.createItemData(ItemPresets.NETHERITE_HELMET_PROTECTION, 39),

                KitStack.createItemData(ItemPresets.TOTEM_OF_UNDYING, 40)
        );
        netherite_mace_items.forEach(
                itemStackData -> netherite_mace.addItem(netherite_mace_items.indexOf(itemStackData), itemStackData)
        );

        KitRegistry.register(netherite_mace);
    }

    private static void diamondMaceKit(){
        Kit diamond_mace = new Kit("diamond_mace", "Diamond Mace Kit", "diamond_sword" ,false);
        List<KitStack> diamond_mace_items = List.of(
                KitStack.createItemData(ItemPresets.DIAMOND_SWORD_SHARPNESS, 0),
                KitStack.createItemData(ItemPresets.ENDER_PEARL, 1, 16),
                KitStack.createItemData(ItemPresets.SHIELD, 2),
                KitStack.createItemData(ItemPresets.BREACH_MACE, 3),
                KitStack.createItemData(ItemPresets.DENSITY_MACE, 4),
                KitStack.createItemData(ItemPresets.ELYTRA,5),
                KitStack.createItemData(ItemPresets.WIND_CHARGE,6,64),
                KitStack.createItemData(ItemPresets.GOLDEN_APPLE,7,64),
                KitStack.createItemData(ItemPresets.DIAMOND_AXE_SHARPNESS, 8),

                KitStack.createItemData(ItemPresets.TOTEM_OF_UNDYING,9),
                KitStack.createItemData(ItemPresets.ENDER_PEARL,10,16),
                KitStack.createItemData(ItemPresets.BREEZE_ROD,11,64),
                KitStack.createItemData(ItemPresets.GOLDEN_APPLE,12,64),
                KitStack.createItemData(ItemPresets.STRONG_STRENGTH_POT,13),
                KitStack.createItemData(ItemPresets.STRONG_SWIFTNESS_POT,14),
                KitStack.createItemData(ItemPresets.STRONG_STRENGTH_POT,15),
                KitStack.createItemData(ItemPresets.STRONG_SWIFTNESS_POT, 16),
                KitStack.createItemData(ItemPresets.STRONG_TURTLE_MASTER_POT,17),

                KitStack.createItemData(ItemPresets.TOTEM_OF_UNDYING,18),
                KitStack.createItemData(ItemPresets.ENDER_PEARL,19,16),
                KitStack.createItemData(ItemPresets.STRONG_STRENGTH_POT,20),
                KitStack.createItemData(ItemPresets.STRONG_SWIFTNESS_POT,21),
                KitStack.createItemData(ItemPresets.STRONG_STRENGTH_POT,22),
                KitStack.createItemData(ItemPresets.STRONG_SWIFTNESS_POT,23),
                KitStack.createItemData(ItemPresets.STRONG_STRENGTH_POT,24),
                KitStack.createItemData(ItemPresets.STRONG_SWIFTNESS_POT, 25),
                KitStack.createItemData(ItemPresets.STRONG_TURTLE_MASTER_POT,26),

                KitStack.createItemData(ItemPresets.TOTEM_OF_UNDYING,27),
                KitStack.createItemData(ItemPresets.ENDER_PEARL,28,16),
                KitStack.createItemData(ItemPresets.STRONG_STRENGTH_POT,29),
                KitStack.createItemData(ItemPresets.STRONG_SWIFTNESS_POT,30),
                KitStack.createItemData(ItemPresets.STRONG_STRENGTH_POT,31),
                KitStack.createItemData(ItemPresets.STRONG_SWIFTNESS_POT,32),
                KitStack.createItemData(ItemPresets.STRONG_STRENGTH_POT,33),
                KitStack.createItemData(ItemPresets.STRONG_SWIFTNESS_POT, 34),
                KitStack.createItemData(ItemPresets.STRONG_TURTLE_MASTER_POT,35),

                KitStack.createItemData(ItemPresets.DIAMOND_BOOTS_PROTECTION,36),
                KitStack.createItemData(ItemPresets.DIAMOND_LEGGINGS_PROTECTION, 37),
                KitStack.createItemData(ItemPresets.DIAMOND_CHESTPLATE_PROTECTION,38),
                KitStack.createItemData(ItemPresets.DIAMOND_HELMET_PROTECTION, 39),

                KitStack.createItemData(ItemPresets.TOTEM_OF_UNDYING, 1)
        );
        diamond_mace_items.forEach(
                itemStackData -> {
                    diamond_mace.addItem(diamond_mace_items.indexOf(itemStackData), itemStackData);
                });

        KitRegistry.register(diamond_mace);
    }
}
