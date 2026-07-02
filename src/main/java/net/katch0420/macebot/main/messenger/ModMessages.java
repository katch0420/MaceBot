package net.katch0420.macebot.main.messenger;

import net.katch0420.macebot.main.utils.LegacyText;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class ModMessages {
    public static MutableText SUCCESS = LegacyText.parse("&f[&aMaceBot&f] ");
    public static MutableText WARNING = LegacyText.parse("&f[&eMaceBot&f] ");
    public static MutableText ERROR = LegacyText.parse("&f[&cMaceBot&f] ");

    public static MutableText CLIENT_WARN_CHAT_NOT_CONNECTED = LegacyText.parse("Please connect to a server with MaceBot");;
    public static MutableText CLIENT_INFO_CHAT_NETWORK_DETECTION_SUCCESSFUL = LegacyText.parse("MaceBot was detected in server environment.");
    public static MutableText CLIENT_WARN_CHAT_NETWORK_DETECTION_SUCCESSFUL_MISMATCH = LegacyText.parse("&eMisMatched version of MaceBot was detected in server environment.");

    public static MutableText CLIENT_WARN_RESTRICTED_ACTION = LegacyText.parse("Operative Access Required");

    public static void send(PlayerEntity player, Text text){
        player.sendMessage(text, false);
    }
    public static void send(PlayerEntity player, Text text, boolean overlay){
        player.sendMessage(text, overlay);
    }
}
