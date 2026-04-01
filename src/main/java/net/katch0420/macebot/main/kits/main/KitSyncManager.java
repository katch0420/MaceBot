package net.katch0420.macebot.main.kits.main;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.katch0420.macebot.main.MaceBot;
import net.katch0420.macebot.main.kits.client.data.KitData;
import net.katch0420.macebot.main.kits.server.Kit;
import net.katch0420.macebot.main.networking.packets.c2s.UpdateKitDataC2SPacket;
import net.katch0420.macebot.main.networking.packets.s2c.UpdateKitDataS2CPacket;

import java.util.Collection;

public class KitSyncManager {
    public static void syncKitToClients(Kit kit){
        MaceBot.server.getPlayerManager().getPlayerList().forEach(
                p -> ServerPlayNetworking.send(
                        p,
                        new UpdateKitDataS2CPacket(
                                kit.getId(),
                                kit.getDisplayName(),
                                kit.getIconItem(),
                                kit.isCustom(),
                                kit.getItems().size()
                        )
                )
        );
    }

    public static void syncKitDataToServer(KitData kitData){
        ClientPlayNetworking.send(new UpdateKitDataC2SPacket(kitData));
    }

    public static void requestKitDelete(KitData kitData){
        kitData.setIconItem(UpdateKitDataS2CPacket.KIT_DELETE_COMMAND);
        ClientPlayNetworking.send(new UpdateKitDataC2SPacket(kitData));
    }

    public static void syncKitDeleteToClients(String kitId){
        MaceBot.server.getPlayerManager().getPlayerList().forEach(
                p -> ServerPlayNetworking.send(
                        p,
                        new UpdateKitDataS2CPacket(
                                kitId,
                                "",
                                UpdateKitDataS2CPacket.KIT_DELETE_COMMAND,
                                true,
                                0
                        )
                )
        );
    }

    public static void resyncAllKitsToClients(Collection<Kit> values) {
        MaceBot.server.getPlayerManager().getPlayerList().forEach(
                p -> {
                    ServerPlayNetworking.send(p, new UpdateKitDataS2CPacket(UpdateKitDataS2CPacket.RESET_COMMAND));
                    values.forEach(k -> ServerPlayNetworking.send(p, new UpdateKitDataS2CPacket(k)));
                }
        );
    }
}
