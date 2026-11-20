package net.katch0420.macebot.main.network.packets.c2s;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.katch0420.macebot.main.MaceBot;
import net.katch0420.macebot.main.macebot.bot.PlayerBot;

import net.katch0420.macebot.main.settings.Settings;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

import java.util.Set;

public record PlayerBotC2SPacket(ControlCommands command) implements CustomPacketPayload {

    public static final Identifier RAW_ID = Identifier.fromNamespaceAndPath(MaceBot.MOD_ID, "control_command_c2s");
    public static final Type<PlayerBotC2SPacket> ID = new Type<>(RAW_ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerBotC2SPacket> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.idMapper(i -> ControlCommands.values()[i], ControlCommands::ordinal),
                    PlayerBotC2SPacket::command,
                    PlayerBotC2SPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() { return ID; }

    public static void receive(PlayerBotC2SPacket payload, ServerPlayNetworking.Context context) {
        context.server().execute(() -> {
            switch (payload.command()) {
                case MACEBOT_SPAWN -> {
                    if(Settings.MACEBOT_ONLINE.getValue()){
                        MaceBot.debugLog("Recieved Spawn packet");
                        context.player().sendSystemMessage(Component.nullToEmpty("MaceBot is already online!"));
                        Settings.MACEBOT_ONLINE.updateToClients();
                        return;
                    }
                    MaceBot.debugLog("Recieved Spawn packet");
                    PlayerBot.spawnMaceBot(context.player());
                }
                case MACEBOT_DESPAWN -> {
                    if (PlayerBot.playerBot == null) {
                        context.player().sendSystemMessage(Component.nullToEmpty("MaceBot is not spawned."));
                    } else {
                        PlayerBot.kick();
                    }
                }
                case MACEBOT_START -> {
                    if (PlayerBot.playerBot.core == null) {
                        context.player().sendSystemMessage(Component.nullToEmpty("MaceBot is not spawned."));
                    } else {
                        PlayerBot.playerBot.core.play();
                    }
                }
                case MACEBOT_PAUSE -> {
                    if (PlayerBot.playerBot.core == null) {
                        context.player().sendSystemMessage(Component.nullToEmpty("MaceBot is not spawned."));
                    } else {
                        PlayerBot.playerBot.core.pause();
                    }
                }
                case TELEPORT_P2M -> {
                    ServerPlayer p = PlayerBot.playerBot;
                    if (p == null) {
                        context.player().sendSystemMessage(Component.nullToEmpty("MaceBot is not spawned."));
                        return;
                    }
                    context.player().teleportTo(p
                                    //? if >=1.21.6 {
                                    .level()
                                    //?} else
                                    //.serverLevel()
                            ,p.getX(),p.getY(),p.getZ(), Set.of(), p.getYRot(), p.getXRot()
                            //? if >=1.21.2
                            , false
                    );
                }
                case TELEPORT_M2P -> {
                    if (PlayerBot.playerBot == null) {
                        context.player().sendSystemMessage(Component.nullToEmpty("MaceBot is not spawned."));
                        return;
                    }
                    ServerPlayer p = context.player();
                    PlayerBot.playerBot.teleportTo(p
                                    //? if >=1.21.6 {
                                    .level()
                                    //?} else
                                    //.serverLevel()
                            ,p.getX(),p.getY(),p.getZ(), Set.of(), p.getYRot(), p.getXRot()
                            //? if >=1.21.2
                            , false
                    );
                }
            }
        });
    }

    public enum ControlCommands {
        MACEBOT_SPAWN,
        MACEBOT_DESPAWN,
        MACEBOT_START,
        MACEBOT_PAUSE,
        TELEPORT_P2M,
        TELEPORT_M2P
    }

    public static void send(ControlCommands cmd){
        ClientPlayNetworking.send(
                new PlayerBotC2SPacket(cmd)
        );
    }
}