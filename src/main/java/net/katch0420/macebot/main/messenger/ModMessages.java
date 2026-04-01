package net.katch0420.macebot.main.messenger;

import net.katch0420.macebot.main.utils.LegacyText;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

public class ModMessages {
    public static Text CLIENT_INFO_CHAT_NETWORK_DETECTION_SUCCESSFUL = LegacyText.parse("&aMaceBot was detected in server environment.");
    public static Text CLIENT_WARN_CHAT_NETWORK_DETECTION_SUCCESSFUL_MISMATCH = LegacyText.parse("&eMisMatched version of MaceBot was detected in server environment.");

    public static void send(PlayerEntity player, Text text){
        player.sendMessage(text, false);
    }
    public static void send(PlayerEntity player, Text text, boolean overlay){
        player.sendMessage(text, overlay);
    }
}
