package net.katch0420.macebot.client.macebot;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.katch0420.macebot.main.networking.packets.c2s.ControlCommandC2SPacket;

public class MaceBotCommandSender {
    public static void spawn(){
        ClientPlayNetworking.send(new ControlCommandC2SPacket(ControlCommandC2SPacket.ControlCommands.MACEBOT_SPAWN));
    }
    public static void kick(){
        ClientPlayNetworking.send(new ControlCommandC2SPacket(ControlCommandC2SPacket.ControlCommands.MACEBOT_DESPAWN));
    }
    public static void play(){
        ClientPlayNetworking.send(new ControlCommandC2SPacket(ControlCommandC2SPacket.ControlCommands.MACEBOT_START));
    }
    public static void stop(){
        ClientPlayNetworking.send(new ControlCommandC2SPacket(ControlCommandC2SPacket.ControlCommands.MACEBOT_PAUSE));
    }
}
