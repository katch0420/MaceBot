package net.katch0420.macebot.player;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Kits {

    private static final Map<Integer, ItemData> selectedItems = new HashMap<>();
    private static final String speedPotAtr = "minecraft:potion_contents={potion:strong_swiftness},";
    private static final String strengthPotAtr ="minecraft:potion_contents={potion:strong_strength},";
    private static final String turtleMasterPotAtr = "minecraft:potion_contents={potion:strong_turtle_master},";
    public enum Kit{
        MACE_DIAMOND{
            @Override
            void execute() {
                selectedItems.clear();
                selectedItems.put(0,new ItemData("container.0","diamond_sword","sharpness:5,sweeping_edge:3",1,true));
                selectedItems.put(1,new ItemData("container.1","ender_pearl","",16));
                selectedItems.put(2,new ItemData("container.2","shield","",1,true));
                selectedItems.put(3,new ItemData("container.3","mace","breach:4",1,true));
                selectedItems.put(4,new ItemData("container.4","mace","density:5,wind_burst:1",1,true));
                selectedItems.put(5,new ItemData("container.5","elytra","",1,true));
                selectedItems.put(6,new ItemData("container.6","wind_charge","",64));
                selectedItems.put(7,new ItemData("container.7","golden_apple","",64));
                selectedItems.put(8,new ItemData("container.8","diamond_axe","sharpness:5,efficiency:5",1,true));
                selectedItems.put(9,new ItemData("container.9","totem_of_undying","",1));
                selectedItems.put(10,new ItemData("container.10","ender_pearl","",16));
                selectedItems.put(11,new ItemData("container.11","breeze_rod","",64));
                selectedItems.put(12,new ItemData("container.12","golden_apple","",64));
                selectedItems.put(13,new ItemData("container.13","splash_potion","", strengthPotAtr,1));
                selectedItems.put(14,new ItemData("container.14","splash_potion","",speedPotAtr,1));
                selectedItems.put(15,new ItemData("container.15","splash_potion","", strengthPotAtr,1));
                selectedItems.put(16,new ItemData("container.16","splash_potion","",speedPotAtr,1));
                selectedItems.put(17,new ItemData("container.17","splash_potion","",turtleMasterPotAtr,1));
                selectedItems.put(18,new ItemData("container.18","totem_of_undying","",1));
                selectedItems.put(19,new ItemData("container.19","ender_pearl","",16));
                selectedItems.put(20,new ItemData("container.20","splash_potion","", strengthPotAtr,1));
                selectedItems.put(21,new ItemData("container.21","splash_potion","",speedPotAtr,1));
                selectedItems.put(22,new ItemData("container.22","splash_potion","", strengthPotAtr,1));
                selectedItems.put(23,new ItemData("container.23","splash_potion","",speedPotAtr,1));
                selectedItems.put(24,new ItemData("container.24","splash_potion","", strengthPotAtr,1));
                selectedItems.put(25,new ItemData("container.25","splash_potion","",speedPotAtr,1));
                selectedItems.put(26,new ItemData("container.26","splash_potion","",turtleMasterPotAtr,1));
                selectedItems.put(27,new ItemData("container.27","totem_of_undying","",1));
                selectedItems.put(28,new ItemData("container.28","ender_pearl","",16));
                selectedItems.put(29,new ItemData("container.29","splash_potion","", strengthPotAtr,1));
                selectedItems.put(30,new ItemData("container.30","splash_potion","",speedPotAtr,1));
                selectedItems.put(31,new ItemData("container.31","splash_potion","", strengthPotAtr,1));
                selectedItems.put(32,new ItemData("container.32","splash_potion","",speedPotAtr,1));
                selectedItems.put(33,new ItemData("container.33","splash_potion","", strengthPotAtr,1));
                selectedItems.put(34,new ItemData("container.34","splash_potion","",speedPotAtr,1));
                selectedItems.put(35,new ItemData("container.35","splash_potion","",turtleMasterPotAtr,1));

                selectedItems.put(36,new ItemData("armor.head","diamond_helmet","protection:4,aqua_affinity:1,respiration:3",1,true));
                selectedItems.put(37,new ItemData("armor.chest","diamond_chestplate","protection:4",1,true));
                selectedItems.put(38,new ItemData("armor.legs","diamond_leggings","protection:4",1,true));
                selectedItems.put(39,new ItemData("armor.feet","diamond_boots","protection:4,feather_falling:4",1,true));
                selectedItems.put(40,new ItemData("weapon.offhand","totem_of_undying","",1));
            }
        },
        MACE_NETHERITE{
            @Override
            void execute() {
                selectedItems.clear();
                selectedItems.put(0,new ItemData("container.0","netherite_sword","sharpness:5,sweeping_edge:3",1,true));
                selectedItems.put(1,new ItemData("container.1","ender_pearl","",16));
                selectedItems.put(2,new ItemData("container.2","shield","",1,true));
                selectedItems.put(3,new ItemData("container.3","mace","breach:4",1,true));
                selectedItems.put(4,new ItemData("container.4","mace","density:5,wind_burst:1",1,true));
                selectedItems.put(5,new ItemData("container.5","elytra","",1,true));
                selectedItems.put(6,new ItemData("container.6","wind_charge","",64));
                selectedItems.put(7,new ItemData("container.7","golden_apple","",64));
                selectedItems.put(8,new ItemData("container.8","netherite_axe","sharpness:5,efficiency:5",1,true));
                selectedItems.put(9,new ItemData("container.9","totem_of_undying","",1));
                selectedItems.put(10,new ItemData("container.10","ender_pearl","",16));
                selectedItems.put(11,new ItemData("container.11","breeze_rod","",64));
                selectedItems.put(12,new ItemData("container.12","golden_apple","",64));
                selectedItems.put(13,new ItemData("container.13","splash_potion","", strengthPotAtr,1));
                selectedItems.put(14,new ItemData("container.14","splash_potion","",speedPotAtr,1));
                selectedItems.put(15,new ItemData("container.15","splash_potion","", strengthPotAtr,1));
                selectedItems.put(16,new ItemData("container.16","splash_potion","",speedPotAtr,1));
                selectedItems.put(17,new ItemData("container.17","splash_potion","",turtleMasterPotAtr,1));
                selectedItems.put(18,new ItemData("container.18","totem_of_undying","",1));
                selectedItems.put(19,new ItemData("container.19","ender_pearl","",16));
                selectedItems.put(20,new ItemData("container.20","splash_potion","", strengthPotAtr,1));
                selectedItems.put(21,new ItemData("container.21","splash_potion","",speedPotAtr,1));
                selectedItems.put(22,new ItemData("container.22","splash_potion","", strengthPotAtr,1));
                selectedItems.put(23,new ItemData("container.23","splash_potion","",speedPotAtr,1));
                selectedItems.put(24,new ItemData("container.24","splash_potion","", strengthPotAtr,1));
                selectedItems.put(25,new ItemData("container.25","splash_potion","",speedPotAtr,1));
                selectedItems.put(26,new ItemData("container.26","splash_potion","",turtleMasterPotAtr,1));
                selectedItems.put(27,new ItemData("container.27","totem_of_undying","",1));
                selectedItems.put(28,new ItemData("container.28","ender_pearl","",16));
                selectedItems.put(29,new ItemData("container.29","splash_potion","", strengthPotAtr,1));
                selectedItems.put(30,new ItemData("container.30","splash_potion","",speedPotAtr,1));
                selectedItems.put(31,new ItemData("container.31","splash_potion","", strengthPotAtr,1));
                selectedItems.put(32,new ItemData("container.32","splash_potion","",speedPotAtr,1));
                selectedItems.put(33,new ItemData("container.33","splash_potion","", strengthPotAtr,1));
                selectedItems.put(34,new ItemData("container.34","splash_potion","",speedPotAtr,1));
                selectedItems.put(35,new ItemData("container.35","splash_potion","",turtleMasterPotAtr,1));

                selectedItems.put(36,new ItemData("armor.head","netherite_helmet","protection:4,aqua_affinity:1,respiration:3",1,true));
                selectedItems.put(37,new ItemData("armor.chest","netherite_chestplate","protection:4",1,true));
                selectedItems.put(38,new ItemData("armor.legs","netherite_leggings","protection:4",1,true));
                selectedItems.put(39,new ItemData("armor.feet","netherite_boots","protection:4,feather_falling:4",1,true));
                selectedItems.put(40,new ItemData("weapon.offhand","totem_of_undying","",1));

            }
        },
        REFILL{
            @Override
            void execute() {
                selectedItems.clear();
                selectedItems.put(0,new ItemData("container.1","ender_pearl","",16));
                selectedItems.put(1,new ItemData("container.6","wind_charge","",64));
                selectedItems.put(2,new ItemData("container.7","golden_apple","",64));
                selectedItems.put(3,new ItemData("weapon.offhand","totem_of_undying","",1));
            }
        };
        abstract void execute();
    }

    public static void giveKit(ServerCommandSource source, Kit kit, boolean unbreakable,String name){
        kit.execute();
        String unbreakableField;
        String unbreakingField;
        for(int i = 0; i < selectedItems.size(); i++){
            if(selectedItems.get(i) != null) {
                ItemData data = selectedItems.get(i);
                if(data.breakable) {
                    unbreakableField = unbreakable ? "minecraft:unbreakable={}," : "";
                    unbreakingField = unbreakable ? "" : "mending:1,unbreaking:3,";
                } else {
                    unbreakableField = "";
                    unbreakingField = "";
                }
                String input = String.format("item replace entity %s %s with minecraft:%s[%s%sminecraft:enchantments={%s%s}] %d", name, data.slot, data.id,data.attribute, unbreakableField, unbreakingField, data.enchants, data.count);
                executeCmd(source, input);
            }
        }
    }
    private static void executeCmd(ServerCommandSource source, String input){
        try {
            source.getServer().getCommandManager().getDispatcher().execute(input,source.withSilent());
        } catch (CommandSyntaxException ignored) {

        }
    }
    public static void refillItems(ServerPlayerEntity player){
        giveKit(Objects.requireNonNull(player.getServer()).getCommandSource(),Kit.REFILL,false,player.getName().getString());
    }
    public static class ItemData{
        String slot;
        String id;
        String enchants;
        String attribute;
        int count;
        boolean breakable;
        ItemData(String slot, String id,String enchants, int count, boolean breakable){
            this.slot = slot;
            this.id = id;
            this.enchants = enchants;
            this.attribute = "";
            this.count = count;
            this.breakable = breakable;
        }
        ItemData(String slot, String id,String enchants, int count){
            this.slot = slot;
            this.id = id;
            this.enchants = enchants;
            this.attribute = "";
            this.count = count;
            this.breakable = false;
        }

        ItemData(String slot, String id,String enchants,String attribute, int count){
            this.slot = slot;
            this.id = id;
            this.enchants = enchants;
            this.attribute = attribute;
            this.count = count;
            this.breakable = false;
        }
    }
}
