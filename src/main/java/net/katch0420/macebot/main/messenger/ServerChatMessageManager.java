package net.katch0420.macebot.main.messenger;

import net.katch0420.macebot.main.macebot.ai.Mode;
import net.katch0420.macebot.main.macebot.ai.PracticeMode;
import net.katch0420.macebot.main.settings.Flags;
import net.katch0420.macebot.main.settings.Setting;
import net.katch0420.macebot.main.settings.Settings;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class ServerChatMessageManager {
    public static void warn(ServerPlayer player, Component msg){
        send(player, Messages.WARNING.copy().append(msg));
    }

    public static void info(ServerPlayer player, Component msg){
        send(player, Messages.INFO.copy().append(msg));
    }

    public static void error(ServerPlayer player, Component msg){
        send(player, Messages.ERROR.copy().append(msg));
    }

    public static void send(ServerPlayer player, Component msg){
        if(Settings.CHAT_MESSAGES.getValue()) player.sendSystemMessage(msg);
    }

    public static <T> void sendEnumSettingUpdate(ServerPlayer player, Setting<T> s, T val){
        if(!Settings.SERVER.getValue()) {
            warn(player, Messages.W_SERVER_DISABLED);
            return;
        }
        if(!s.getType().isEnum() || val == null) return;
        if(s.hasFlag(Flags.SETTING_RESTRICTED) && !Settings.hasAccess(player)) return;
        Component d = Component.literal("");
        if(val instanceof Mode m) d = m.displayText;
        if(val instanceof PracticeMode p) d = p.displayText;
        s.update(val);
        info(player, Component.literal(s.getDisplayName() + " is set to " + d.getString()));
    }

    public static void sendIntegerSettingUpdate(ServerPlayer player, Setting<Integer> setting, int value) {
        if (!Settings.SERVER.getValue()) {
            warn(player, Messages.W_SERVER_DISABLED);
            return;
        }
        if (setting.getType() != Integer.class) return;
        if (setting.hasFlag(Flags.SETTING_RESTRICTED) && !Settings.hasAccess(player)) return;

        setting.update(value);
        info(player, Component.literal(setting.getDisplayName() + " is set to " + setting.getValue()));
    }

    public static <T> void sendBoolSettingUpdate(ServerPlayer player, Setting<Boolean> s, boolean val){
        if(!s.isBoolean()) return;
        if(s.hasFlag(Flags.SETTING_RESTRICTED)) if(!Settings.hasAccess(player)) return;
        s.update(val);
        info(player, Component.literal(((Boolean) val ? "Enabled ":"Disabled ") + s.getDisplayName() + " of "+ s.getCategory().getDisplayName()));
    }

    public static void sendBoolSettingUpdate(ServerPlayer player, Setting<Boolean> s){
        if(!s.isBoolean()) return;
        sendBoolSettingUpdate(player,s, s.getValue());
    }
}
