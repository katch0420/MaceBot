package net.katch0420.macebot.main.kits.server;

import net.katch0420.macebot.main.kits.main.ItemPresets;
import net.katch0420.macebot.main.kits.main.Kit;
import net.katch0420.macebot.main.kits.main.KitStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

public class BuiltInKitLoader {

    public static void registerAll() {
        netheriteMaceKit();
        diamondMaceKit();
    }

    private static void netheriteMaceKit(){
        Kit netherite_mace = new Kit("netherite_mace", "Netherite Mace Kit", Identifier.ofVanilla("netherite_sword"), false);
        List<KitStack> netherite_mace_items = List.of(
                KitStack.stack(ItemPresets.NETHERITE_SWORD_SHARPNESS, 0),
                KitStack.stack(ItemPresets.ENDER_PEARL, 1, 16),
                KitStack.stack(ItemPresets.SHIELD, 2),
                KitStack.stack(ItemPresets.BREACH_MACE, 3),
                KitStack.stack(ItemPresets.DENSITY_MACE, 4),
                KitStack.stack(ItemPresets.ELYTRA,5),
                KitStack.stack(ItemPresets.WIND_CHARGE,6,64),
                KitStack.stack(ItemPresets.GOLDEN_APPLE,7,64),
                KitStack.stack(ItemPresets.NETHERITE_AXE_SHARPNESS, 8),

                KitStack.stack(ItemPresets.TOTEM_OF_UNDYING,9),
                KitStack.stack(ItemPresets.ENDER_PEARL,10,16),
                KitStack.stack(ItemPresets.BREEZE_ROD,11,64),
                KitStack.stack(ItemPresets.GOLDEN_APPLE,12,64),
                KitStack.stack(ItemPresets.STRONG_STRENGTH_POT,13),
                KitStack.stack(ItemPresets.STRONG_SWIFTNESS_POT,14),
                KitStack.stack(ItemPresets.STRONG_STRENGTH_POT,15),
                KitStack.stack(ItemPresets.STRONG_SWIFTNESS_POT, 16),
                KitStack.stack(ItemPresets.STRONG_TURTLE_MASTER_POT,17),

                KitStack.stack(ItemPresets.TOTEM_OF_UNDYING,18),
                KitStack.stack(ItemPresets.ENDER_PEARL,19,16),
                KitStack.stack(ItemPresets.STRONG_STRENGTH_POT,20),
                KitStack.stack(ItemPresets.STRONG_SWIFTNESS_POT,21),
                KitStack.stack(ItemPresets.STRONG_STRENGTH_POT,22),
                KitStack.stack(ItemPresets.STRONG_SWIFTNESS_POT,23),
                KitStack.stack(ItemPresets.STRONG_STRENGTH_POT,24),
                KitStack.stack(ItemPresets.STRONG_SWIFTNESS_POT, 25),
                KitStack.stack(ItemPresets.STRONG_TURTLE_MASTER_POT,26),

                KitStack.stack(ItemPresets.TOTEM_OF_UNDYING,27),
                KitStack.stack(ItemPresets.ENDER_PEARL,28,16),
                KitStack.stack(ItemPresets.STRONG_STRENGTH_POT,29),
                KitStack.stack(ItemPresets.STRONG_SWIFTNESS_POT,30),
                KitStack.stack(ItemPresets.STRONG_STRENGTH_POT,31),
                KitStack.stack(ItemPresets.STRONG_SWIFTNESS_POT,32),
                KitStack.stack(ItemPresets.STRONG_STRENGTH_POT,33),
                KitStack.stack(ItemPresets.STRONG_SWIFTNESS_POT, 34),
                KitStack.stack(ItemPresets.STRONG_TURTLE_MASTER_POT,35),

                KitStack.stack(ItemPresets.NETHERITE_BOOTS_PROTECTION,36),
                KitStack.stack(ItemPresets.NETHERITE_LEGGINGS_PROTECTION, 37),
                KitStack.stack(ItemPresets.NETHERITE_CHESTPLATE_PROTECTION,38),
                KitStack.stack(ItemPresets.NETHERITE_HELMET_PROTECTION, 39),

                KitStack.stack(ItemPresets.TOTEM_OF_UNDYING, 40)
        );
        netherite_mace_items.forEach(
                itemStackData -> netherite_mace.addItem(netherite_mace_items.indexOf(itemStackData), itemStackData)
        );

        KitRegistry.register(netherite_mace);
    }

    private static void diamondMaceKit(){
        Kit diamond_mace = new Kit("diamond_mace", "Diamond Mace Kit", Identifier.ofVanilla("diamond_sword") ,false);
        List<KitStack> diamond_mace_items = List.of(
                KitStack.stack(ItemPresets.DIAMOND_SWORD_SHARPNESS, 0),
                KitStack.stack(ItemPresets.ENDER_PEARL, 1, 16),
                KitStack.stack(ItemPresets.SHIELD, 2),
                KitStack.stack(ItemPresets.BREACH_MACE, 3),
                KitStack.stack(ItemPresets.DENSITY_MACE, 4),
                KitStack.stack(ItemPresets.ELYTRA,5),
                KitStack.stack(ItemPresets.WIND_CHARGE,6,64),
                KitStack.stack(ItemPresets.GOLDEN_APPLE,7,64),
                KitStack.stack(ItemPresets.DIAMOND_AXE_SHARPNESS, 8),

                KitStack.stack(ItemPresets.TOTEM_OF_UNDYING,9),
                KitStack.stack(ItemPresets.ENDER_PEARL,10,16),
                KitStack.stack(ItemPresets.BREEZE_ROD,11,64),
                KitStack.stack(ItemPresets.GOLDEN_APPLE,12,64),
                KitStack.stack(ItemPresets.STRONG_STRENGTH_POT,13),
                KitStack.stack(ItemPresets.STRONG_SWIFTNESS_POT,14),
                KitStack.stack(ItemPresets.STRONG_STRENGTH_POT,15),
                KitStack.stack(ItemPresets.STRONG_SWIFTNESS_POT, 16),
                KitStack.stack(ItemPresets.STRONG_TURTLE_MASTER_POT,17),

                KitStack.stack(ItemPresets.TOTEM_OF_UNDYING,18),
                KitStack.stack(ItemPresets.ENDER_PEARL,19,16),
                KitStack.stack(ItemPresets.STRONG_STRENGTH_POT,20),
                KitStack.stack(ItemPresets.STRONG_SWIFTNESS_POT,21),
                KitStack.stack(ItemPresets.STRONG_STRENGTH_POT,22),
                KitStack.stack(ItemPresets.STRONG_SWIFTNESS_POT,23),
                KitStack.stack(ItemPresets.STRONG_STRENGTH_POT,24),
                KitStack.stack(ItemPresets.STRONG_SWIFTNESS_POT, 25),
                KitStack.stack(ItemPresets.STRONG_TURTLE_MASTER_POT,26),

                KitStack.stack(ItemPresets.TOTEM_OF_UNDYING,27),
                KitStack.stack(ItemPresets.ENDER_PEARL,28,16),
                KitStack.stack(ItemPresets.STRONG_STRENGTH_POT,29),
                KitStack.stack(ItemPresets.STRONG_SWIFTNESS_POT,30),
                KitStack.stack(ItemPresets.STRONG_STRENGTH_POT,31),
                KitStack.stack(ItemPresets.STRONG_SWIFTNESS_POT,32),
                KitStack.stack(ItemPresets.STRONG_STRENGTH_POT,33),
                KitStack.stack(ItemPresets.STRONG_SWIFTNESS_POT, 34),
                KitStack.stack(ItemPresets.STRONG_TURTLE_MASTER_POT,35),

                KitStack.stack(ItemPresets.DIAMOND_BOOTS_PROTECTION,36),
                KitStack.stack(ItemPresets.DIAMOND_LEGGINGS_PROTECTION, 37),
                KitStack.stack(ItemPresets.DIAMOND_CHESTPLATE_PROTECTION,38),
                KitStack.stack(ItemPresets.DIAMOND_HELMET_PROTECTION, 39),

                KitStack.stack(ItemPresets.TOTEM_OF_UNDYING, 1)
        );
        diamond_mace_items.forEach(
                itemStackData -> {
                    diamond_mace.addItem(diamond_mace_items.indexOf(itemStackData), itemStackData);
                });

        KitRegistry.register(diamond_mace);
    }
}
