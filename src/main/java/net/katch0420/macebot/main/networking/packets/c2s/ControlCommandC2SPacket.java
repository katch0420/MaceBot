package net.katch0420.macebot.main.networking.packets.c2s;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.katch0420.macebot.main.MaceBot;
import net.katch0420.macebot.main.macebot.bot.PlayerBot;

import net.katch0420.macebot.main.settings.main.SettingsKey;
import net.katch0420.macebot.main.settings.server.Settings;
import net.katch0420.macebot.main.settings.server.SettingsSyncHelper;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public record ControlCommandC2SPacket(ControlCommands command) implements CustomPayload {

    public static final Identifier RAW_ID = Identifier.of(MaceBot.MOD_ID, "control_command_c2s");
    public static final CustomPayload.Id<ControlCommandC2SPacket> ID = new CustomPayload.Id<>(RAW_ID);

    public static final PacketCodec<RegistryByteBuf, ControlCommandC2SPacket> CODEC =
            PacketCodec.tuple(
                    PacketCodecs.indexed(i -> ControlCommands.values()[i], ControlCommands::ordinal),
                    ControlCommandC2SPacket::command,
                    ControlCommandC2SPacket::new
            );

    @Override
    public Id<? extends CustomPayload> getId() { return ID; }

    public static void receive(ControlCommandC2SPacket payload, ServerPlayNetworking.Context context) {
        context.server().execute(() -> {
            switch (payload.command()) {
                case MACEBOT_SPAWN -> {
                    if(Settings.isMacebotOnline()){
                        context.player().sendMessage(Text.of("MaceBot is already online!"));
                        SettingsSyncHelper.broadcastCurrent(SettingsKey.MACEBOT_ONLINE, MaceBot.server);
                        return;
                    }
                    PlayerBot.spawnMaceBot(context.player());
                }
                case MACEBOT_DESPAWN -> {
                    System.out.println(2);
                    PlayerBot.disconnect();
                }
                case MACEBOT_START -> PlayerBot.controller.startOrResume();
                case MACEBOT_PAUSE -> PlayerBot.controller.pause();
            }
        });
    }

    public enum ControlCommands {
        MACEBOT_SPAWN,
        MACEBOT_DESPAWN,
        MACEBOT_START,
        MACEBOT_PAUSE
    }

    public static void send(ControlCommands cmd){
        ClientPlayNetworking.send(
                new ControlCommandC2SPacket(cmd)
        );
    }
}