package net.katch0420.macebot.utils;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.HashMap;
import java.util.Map;

public class Messenger {

    private static final Map<Integer, MutableText> messageComponents = new HashMap<>();
    private static int index = 0;
    public static void add(String a, Formatting b){
        MutableText text = Text.literal(a).formatted(b);
        if(messageComponents.isEmpty()){
            index = 0;
        }
        messageComponents.put(index,text);
        index++;
    }
    public static boolean send(ServerPlayerEntity player, boolean overlay, boolean sound){
        if(player != null) {
            MutableText msg = Text.literal("");
            for (int a = 0; a < messageComponents.size(); a++) {
                msg.append(messageComponents.get(a));
            }
            player.sendMessage(msg, overlay);
            if (sound) {
                player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS);
            }
            messageComponents.clear();
            return true;
        } else {
            return false;
        }
    }
}