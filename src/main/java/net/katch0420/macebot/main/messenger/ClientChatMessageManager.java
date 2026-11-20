package net.katch0420.macebot.main.messenger;

import net.katch0420.macebot.main.settings.Flags;
import net.katch0420.macebot.main.settings.Setting;
import net.katch0420.macebot.main.settings.Settings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

public class ClientChatMessageManager {
    public static LocalPlayer getUser(){
        return Minecraft.getInstance().player;
    }

    public static void warn(Component msg){
        send(Messages.WARNING.copy().append(msg));
    }

    public static void info(Component msg){
        send(Messages.INFO.copy().append(msg));
    }

    public static void error(Component msg){
        send(Messages.ERROR.copy().append(msg));
    }

    public static void send(Component msg){
        if(Settings.CHAT_MESSAGES.getValue()) getUser()
                //? if >1.21.11
                //.sendSystemMessage(msg);
                //? if >=1.21.2 && <=1.21.11
                .displayClientMessage(msg, false);
                //? if <1.21.2
                //.sendSystemMessage(msg);
    }
}
