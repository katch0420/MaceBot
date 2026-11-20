package net.katch0420.macebot.main.messenger;

import net.katch0420.macebot.main.utils.LegacyText;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class Messages {
    public static MutableComponent INFO = LegacyText.parse("&f[&aMaceBot&f] ");
    public static MutableComponent WARNING = LegacyText.parse("&f[&eMaceBot&f] ");
    public static MutableComponent ERROR = LegacyText.parse("&f[&cMaceBot&f] ");

    public static MutableComponent W_NO_CONNECTION = Component.literal("Please connect to a server running MaceBot.");
    public static MutableComponent I_CONNECTION_DETECTED = Component.literal("MaceBot detected in the server environment.");
    public static MutableComponent I_CLIENT_DISABLED_ON_JOIN_NON_OP = Component.literal("MaceBot Client has been disabled because you lack operative access. Enable it in SettingManager once access is granted.");
    public static MutableComponent W_SERVER_DISABLED = Component.literal("MaceBot Server is currently disabled. Enable it in SettingManager or using").append(Component.literal("&e/bot settings mod server_enabled")).append(Component.literal("once access is granted."));
    public static MutableComponent W_CONNECTION_DETECTION_MISMATCH = Component.literal("A mismatched version of MaceBot was detected on the server.");

    public static MutableComponent W_RESTRICTED_ACCESS = Component.literal("Operative access required.");

    public static MutableComponent I_BOT_SPAWN = Component.literal("MaceBot spawned.");
    public static MutableComponent I_BOT_KICK = Component.literal("MaceBot kicked.");
    public static MutableComponent I_BOT_STOP = Component.literal("MaceBot stopped.");
    public static MutableComponent I_BOT_PLAY = Component.literal("MaceBot resumed.");
}
