package net.katch0420.macebot.client.macebot;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.katch0420.macebot.main.network.packets.c2s.PlayerBotC2SPacket;

public class MaceBotCommandSender {
    public static void spawn(){
        ClientPlayNetworking.send(new PlayerBotC2SPacket(PlayerBotC2SPacket.ControlCommands.MACEBOT_SPAWN));
    }
    public static void kick(){
        ClientPlayNetworking.send(new PlayerBotC2SPacket(PlayerBotC2SPacket.ControlCommands.MACEBOT_DESPAWN));
    }
    public static void play(){
        ClientPlayNetworking.send(new PlayerBotC2SPacket(PlayerBotC2SPacket.ControlCommands.MACEBOT_START));
    }
    public static void stop(){
        ClientPlayNetworking.send(new PlayerBotC2SPacket(PlayerBotC2SPacket.ControlCommands.MACEBOT_PAUSE));
    }
    public static void teleportE2M(){
        ClientPlayNetworking.send(new PlayerBotC2SPacket(PlayerBotC2SPacket.ControlCommands.TELEPORT_P2M));
    }
    public static void teleportM2E(){
        ClientPlayNetworking.send(new PlayerBotC2SPacket(PlayerBotC2SPacket.ControlCommands.TELEPORT_M2P));
    }
}
