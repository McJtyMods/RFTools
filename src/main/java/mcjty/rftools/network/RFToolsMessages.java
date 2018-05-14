package mcjty.rftools.network;

import mcjty.lib.network.PacketHandler;
import mcjty.lib.network.PacketSendClientCommand;
import mcjty.lib.network.PacketSendServerCommand;
import mcjty.lib.typed.TypedMap;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.builder.PacketChamberInfoReady;
import mcjty.rftools.blocks.crafter.CrafterConfiguration;
import mcjty.rftools.blocks.crafter.PacketCrafter;
import mcjty.rftools.blocks.monitor.*;
import mcjty.rftools.blocks.screens.network.PacketGetScreenData;
import mcjty.rftools.blocks.screens.network.PacketModuleUpdate;
import mcjty.rftools.blocks.screens.network.PacketReturnScreenData;
import mcjty.rftools.blocks.security.PacketSecurityInfoReady;
import mcjty.rftools.blocks.security.SecurityConfiguration;
import mcjty.rftools.blocks.shaper.PacketProjectorClientNotification;
import mcjty.rftools.blocks.shield.PacketFiltersReady;
import mcjty.rftools.blocks.shield.PacketGetFilters;
import mcjty.rftools.blocks.storage.PacketSyncSlotsToClient;
import mcjty.rftools.blocks.storage.PacketUpdateNBTItemStorage;
import mcjty.rftools.blocks.storagemonitor.*;
import mcjty.rftools.blocks.teleporter.PacketGetReceivers;
import mcjty.rftools.blocks.teleporter.PacketGetTransmitters;
import mcjty.rftools.blocks.teleporter.PacketReceiversReady;
import mcjty.rftools.blocks.teleporter.PacketTransmittersReady;
import mcjty.rftools.craftinggrid.PacketCraftTestResultToClient;
import mcjty.rftools.craftinggrid.PacketGridToClient;
import mcjty.rftools.craftinggrid.PacketGridToServer;
import mcjty.rftools.items.builder.PacketUpdateNBTItemInventoryShape;
import mcjty.rftools.items.creativeonly.PacketDelightingInfoReady;
import mcjty.rftools.items.creativeonly.PacketGetDelightingInfo;
import mcjty.rftools.items.modifier.PacketUpdateModifier;
import mcjty.rftools.items.netmonitor.NetworkMonitorConfiguration;
import mcjty.rftools.items.netmonitor.PacketConnectedBlocksReady;
import mcjty.rftools.items.netmonitor.PacketGetConnectedBlocks;
import mcjty.rftools.items.storage.PacketUpdateNBTItemFilter;
import mcjty.rftools.items.teleportprobe.PacketAllReceiversReady;
import mcjty.rftools.items.teleportprobe.PacketGetAllReceivers;
import mcjty.rftools.items.teleportprobe.PacketTargetsReady;
import mcjty.rftools.jei.PacketSendRecipe;
import mcjty.rftools.playerprops.PacketSendBuffsToClient;
import mcjty.rftools.shapes.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

import javax.annotation.Nonnull;

public class RFToolsMessages {
    public static SimpleNetworkWrapper INSTANCE;

    public static void registerNetworkMessages(SimpleNetworkWrapper net) {
        INSTANCE = net;

        // Server side
        if(CrafterConfiguration.enabled)
            net.registerMessage(PacketCrafter.Handler.class, PacketCrafter.class, PacketHandler.nextPacketID(), Side.SERVER);
        net.registerMessage(PacketGetPlayers.Handler.class, PacketGetPlayers.class, PacketHandler.nextPacketID(), Side.SERVER);
        net.registerMessage(PacketGetReceivers.Handler.class, PacketGetReceivers.class, PacketHandler.nextPacketID(), Side.SERVER);
        net.registerMessage(PacketGetAllReceivers.Handler.class, PacketGetAllReceivers.class, PacketHandler.nextPacketID(), Side.SERVER);
        net.registerMessage(PacketGetTransmitters.Handler.class, PacketGetTransmitters.class, PacketHandler.nextPacketID(), Side.SERVER);
        net.registerMessage(PacketModuleUpdate.Handler.class, PacketModuleUpdate.class, PacketHandler.nextPacketID(), Side.SERVER);
        net.registerMessage(PacketGetScreenData.Handler.class, PacketGetScreenData.class, PacketHandler.nextPacketID(), Side.SERVER);
        net.registerMessage(PacketGetAdjacentBlocks.Handler.class, PacketGetAdjacentBlocks.class, PacketHandler.nextPacketID(), Side.SERVER);
        net.registerMessage(PacketGetAdjacentTankBlocks.Handler.class, PacketGetAdjacentTankBlocks.class, PacketHandler.nextPacketID(), Side.SERVER);
        net.registerMessage(PacketContentsMonitor.Handler.class, PacketContentsMonitor.class, PacketHandler.nextPacketID(), Side.SERVER);
        net.registerMessage(PacketGetFilters.Handler.class, PacketGetFilters.class, PacketHandler.nextPacketID(), Side.SERVER);
        net.registerMessage(PacketGetDelightingInfo.Handler.class, PacketGetDelightingInfo.class, PacketHandler.nextPacketID(), Side.SERVER);
        if(NetworkMonitorConfiguration.enabled)
            net.registerMessage(PacketGetConnectedBlocks.Handler.class, PacketGetConnectedBlocks.class, PacketHandler.nextPacketID(), Side.SERVER);
        net.registerMessage(PacketSendRecipe.Handler.class, PacketSendRecipe.class, PacketHandler.nextPacketID(), Side.SERVER);
        net.registerMessage(PacketGridToServer.Handler.class, PacketGridToServer.class, PacketHandler.nextPacketID(), Side.SERVER);
        net.registerMessage(PacketRequestItem.Handler.class, PacketRequestItem.class, PacketHandler.nextPacketID(), Side.SERVER);
        net.registerMessage(PacketGetHudLog.Handler.class, PacketGetHudLog.class, PacketHandler.nextPacketID(), Side.SERVER);
        net.registerMessage(PacketSendComposerData.Handler.class, PacketSendComposerData.class, PacketHandler.nextPacketID(), Side.SERVER);
        net.registerMessage(PacketUpdateModifier.Handler.class, PacketUpdateModifier.class, PacketHandler.nextPacketID(), Side.SERVER);
        net.registerMessage(PacketRequestShapeData.Handler.class, PacketRequestShapeData.class, PacketHandler.nextPacketID(), Side.SERVER);
        net.registerMessage(PacketUpdateNBTShapeCard.Handler.class, PacketUpdateNBTShapeCard.class, PacketHandler.nextPacketID(), Side.SERVER);
        net.registerMessage(PacketUpdateNBTItemInventoryShape.Handler.class, PacketUpdateNBTItemInventoryShape.class, PacketHandler.nextPacketID(), Side.SERVER);
        net.registerMessage(PacketUpdateNBTItemFilter.Handler.class, PacketUpdateNBTItemFilter.class, PacketHandler.nextPacketID(), Side.SERVER);
        net.registerMessage(PacketUpdateNBTItemStorage.Handler.class, PacketUpdateNBTItemStorage.class, PacketHandler.nextPacketID(), Side.SERVER);

        // Client side
        net.registerMessage(PacketPlayersReady.Handler.class, PacketPlayersReady.class, PacketHandler.nextPacketID(), Side.CLIENT);
        net.registerMessage(PacketTransmittersReady.Handler.class, PacketTransmittersReady.class, PacketHandler.nextPacketID(), Side.CLIENT);
        net.registerMessage(PacketReceiversReady.Handler.class, PacketReceiversReady.class, PacketHandler.nextPacketID(), Side.CLIENT);
        net.registerMessage(PacketAllReceiversReady.Handler.class, PacketAllReceiversReady.class, PacketHandler.nextPacketID(), Side.CLIENT);
        net.registerMessage(PacketTargetsReady.Handler.class, PacketTargetsReady.class, PacketHandler.nextPacketID(), Side.CLIENT);
        net.registerMessage(PacketReturnScreenData.Handler.class, PacketReturnScreenData.class, PacketHandler.nextPacketID(), Side.CLIENT);
        net.registerMessage(PacketChamberInfoReady.Handler.class, PacketChamberInfoReady.class, PacketHandler.nextPacketID(), Side.CLIENT);
        net.registerMessage(PacketAdjacentBlocksReady.Handler.class, PacketAdjacentBlocksReady.class, PacketHandler.nextPacketID(), Side.CLIENT);
        net.registerMessage(PacketAdjacentTankBlocksReady.Handler.class, PacketAdjacentTankBlocksReady.class, PacketHandler.nextPacketID(), Side.CLIENT);
        net.registerMessage(PacketFiltersReady.Handler.class, PacketFiltersReady.class, PacketHandler.nextPacketID(), Side.CLIENT);
        net.registerMessage(PacketSendBuffsToClient.Handler.class, PacketSendBuffsToClient.class, PacketHandler.nextPacketID(), Side.CLIENT);
        if(SecurityConfiguration.enabled)
            net.registerMessage(PacketSecurityInfoReady.Handler.class, PacketSecurityInfoReady.class, PacketHandler.nextPacketID(), Side.CLIENT);
        net.registerMessage(PacketDelightingInfoReady.Handler.class, PacketDelightingInfoReady.class, PacketHandler.nextPacketID(), Side.CLIENT);
        if(NetworkMonitorConfiguration.enabled)
            net.registerMessage(PacketConnectedBlocksReady.Handler.class, PacketConnectedBlocksReady.class, PacketHandler.nextPacketID(), Side.CLIENT);
        net.registerMessage(PacketSyncSlotsToClient.Handler.class, PacketSyncSlotsToClient.class, PacketHandler.nextPacketID(), Side.CLIENT);
        net.registerMessage(PacketGridToClient.Handler.class, PacketGridToClient.class, PacketHandler.nextPacketID(), Side.CLIENT);
        net.registerMessage(PacketCraftTestResultToClient.Handler.class, PacketCraftTestResultToClient.class, PacketHandler.nextPacketID(), Side.CLIENT);
        net.registerMessage(PacketHudLogReady.Handler.class, PacketHudLogReady.class, PacketHandler.nextPacketID(), Side.CLIENT);
        net.registerMessage(PacketReturnRfInRange.Handler.class, PacketReturnRfInRange.class, PacketHandler.nextPacketID(), Side.CLIENT);
        net.registerMessage(PacketReturnShapeData.Handler.class, PacketReturnShapeData.class, PacketHandler.nextPacketID(), Side.CLIENT);
        net.registerMessage(PacketProjectorClientNotification.Handler.class, PacketProjectorClientNotification.class, PacketHandler.nextPacketID(), Side.CLIENT);
        net.registerMessage(PacketReturnExtraData.Handler.class, PacketReturnExtraData.class, PacketHandler.nextPacketID(), Side.CLIENT);

        PacketHandler.register(PacketHandler.nextPacketID(), InventoriesInfoPacketServer.class, InventoriesInfoPacketClient.class);
        PacketHandler.register(PacketHandler.nextPacketID(), SearchItemsInfoPacketServer.class, SearchItemsInfoPacketClient.class);
        PacketHandler.register(PacketHandler.nextPacketID(), GetContentsInfoPacketServer.class, GetContentsInfoPacketClient.class);
        PacketHandler.register(PacketHandler.nextPacketID(), ScannerInfoPacketServer.class, ScannerInfoPacketClient.class);
    }

    public static void sendToServer(String command, @Nonnull TypedMap.Builder argumentBuilder) {
        INSTANCE.sendToServer(new PacketSendServerCommand(RFTools.MODID, command, argumentBuilder.build()));
    }

    public static void sendToServer(String command) {
        INSTANCE.sendToServer(new PacketSendServerCommand(RFTools.MODID, command, TypedMap.EMPTY));
    }

    public static void sendToClient(EntityPlayer player, String command, @Nonnull TypedMap.Builder argumentBuilder) {
        INSTANCE.sendTo(new PacketSendClientCommand(RFTools.MODID, command, argumentBuilder.build()), (EntityPlayerMP) player);
    }

    public static void sendToClient(EntityPlayer player, String command) {
        INSTANCE.sendTo(new PacketSendClientCommand(RFTools.MODID, command, TypedMap.EMPTY), (EntityPlayerMP) player);
    }
}
