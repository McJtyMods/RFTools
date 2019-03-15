package mcjty.rftools.network;

import mcjty.lib.network.PacketHandler;
import mcjty.lib.network.PacketSendClientCommand;
import mcjty.lib.network.PacketSendServerCommand;
import mcjty.lib.thirteen.ChannelBuilder;
import mcjty.lib.thirteen.SimpleChannel;
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
import mcjty.rftools.blocks.storagemonitor.PacketGetInventoryInfo;
import mcjty.rftools.blocks.storagemonitor.PacketRequestItem;
import mcjty.rftools.blocks.storagemonitor.PacketReturnInventoryInfo;
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
import mcjty.rftools.compat.jei.PacketSendRecipe;
import mcjty.rftools.playerprops.PacketSendBuffsToClient;
import mcjty.rftools.shapes.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

import javax.annotation.Nonnull;

public class RFToolsMessages {
    public static SimpleNetworkWrapper INSTANCE;

    private static int id() {
        return PacketHandler.nextPacketID();
    }

    public static void registerMessages(String name) {
        SimpleChannel net = ChannelBuilder
                .named(new ResourceLocation(RFTools.MODID, name))
                .networkProtocolVersion(() -> "1.0")
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();

        INSTANCE = net.getNetwork();

        // Server side
        if (CrafterConfiguration.enabled.get()) {
            net.registerMessageServer(id(), PacketCrafter.class, PacketCrafter::toBytes, PacketCrafter::new, PacketCrafter::handle);
        }

        net.registerMessageServer(id(), PacketGetPlayers.class, PacketGetPlayers::toBytes, PacketGetPlayers::new, PacketGetPlayers::handle);
        net.registerMessageServer(id(), PacketGetReceivers.class, PacketGetReceivers::toBytes, PacketGetReceivers::new, PacketGetReceivers::handle);
        net.registerMessageServer(id(), PacketGetAllReceivers.class, PacketGetAllReceivers::toBytes, PacketGetAllReceivers::new, PacketGetAllReceivers::handle);
        net.registerMessageServer(id(), PacketGetTransmitters.class, PacketGetTransmitters::toBytes, PacketGetTransmitters::new, PacketGetTransmitters::handle);
        net.registerMessageServer(id(), PacketModuleUpdate.class, PacketModuleUpdate::toBytes, PacketModuleUpdate::new, PacketModuleUpdate::handle);
        net.registerMessageServer(id(), PacketGetScreenData.class, PacketGetScreenData::toBytes, PacketGetScreenData::new, PacketGetScreenData::handle);
        net.registerMessageServer(id(), PacketGetAdjacentBlocks.class, PacketGetAdjacentBlocks::toBytes, PacketGetAdjacentBlocks::new, PacketGetAdjacentBlocks::handle);
        net.registerMessageServer(id(), PacketGetAdjacentTankBlocks.class, PacketGetAdjacentTankBlocks::toBytes, PacketGetAdjacentTankBlocks::new, PacketGetAdjacentTankBlocks::handle);
        net.registerMessageServer(id(), PacketContentsMonitor.class, PacketContentsMonitor::toBytes, PacketContentsMonitor::new, PacketContentsMonitor::handle);
        net.registerMessageServer(id(), PacketGetFilters.class, PacketGetFilters::toBytes, PacketGetFilters::new, PacketGetFilters::handle);
        net.registerMessageServer(id(), PacketGetDelightingInfo.class, PacketGetDelightingInfo::toBytes, PacketGetDelightingInfo::new, PacketGetDelightingInfo::handle);
        if (NetworkMonitorConfiguration.enabled.get()) {
            net.registerMessageServer(id(), PacketGetConnectedBlocks.class, PacketGetConnectedBlocks::toBytes, PacketGetConnectedBlocks::new, PacketGetConnectedBlocks::handle);
        }
        net.registerMessageServer(id(), PacketSendRecipe.class, PacketSendRecipe::toBytes, PacketSendRecipe::new, PacketSendRecipe::handle);
        net.registerMessageServer(id(), PacketGridToServer.class, PacketGridToServer::toBytes, PacketGridToServer::new, PacketGridToServer::handle);
        net.registerMessageServer(id(), PacketRequestItem.class, PacketRequestItem::toBytes, PacketRequestItem::new, PacketRequestItem::handle);
        net.registerMessageServer(id(), PacketGetHudLog.class, PacketGetHudLog::toBytes, PacketGetHudLog::new, PacketGetHudLog::handle);
        net.registerMessageServer(id(), PacketSendComposerData.class, PacketSendComposerData::toBytes, PacketSendComposerData::new, PacketSendComposerData::handle);
        net.registerMessageServer(id(), PacketUpdateModifier.class, PacketUpdateModifier::toBytes, PacketUpdateModifier::new, PacketUpdateModifier::handle);
        net.registerMessageServer(id(), PacketRequestShapeData.class, PacketRequestShapeData::toBytes, PacketRequestShapeData::new, PacketRequestShapeData::handle);
        net.registerMessageServer(id(), PacketUpdateNBTShapeCard.class, PacketUpdateNBTShapeCard::toBytes, PacketUpdateNBTShapeCard::new, PacketUpdateNBTShapeCard::handle);
        net.registerMessageServer(id(), PacketUpdateNBTItemInventoryShape.class, PacketUpdateNBTItemInventoryShape::toBytes, PacketUpdateNBTItemInventoryShape::new, PacketUpdateNBTItemInventoryShape::handle);
        net.registerMessageServer(id(), PacketUpdateNBTItemFilter.class, PacketUpdateNBTItemFilter::toBytes, PacketUpdateNBTItemFilter::new, PacketUpdateNBTItemFilter::handle);
        net.registerMessageServer(id(), PacketUpdateNBTItemStorage.class, PacketUpdateNBTItemStorage::toBytes, PacketUpdateNBTItemStorage::new, PacketUpdateNBTItemStorage::handle);
        net.registerMessageServer(id(), PacketGetInventoryInfo.class, PacketGetInventoryInfo::toBytes, PacketGetInventoryInfo::new, PacketGetInventoryInfo::handle);

        // Client side
        net.registerMessageClient(id(), PacketPlayersReady.class, PacketPlayersReady::toBytes, PacketPlayersReady::new, PacketPlayersReady::handle);
        net.registerMessageClient(id(), PacketTransmittersReady.class, PacketTransmittersReady::toBytes, PacketTransmittersReady::new, PacketTransmittersReady::handle);
        net.registerMessageClient(id(), PacketReceiversReady.class, PacketReceiversReady::toBytes, PacketReceiversReady::new, PacketReceiversReady::handle);
        net.registerMessageClient(id(), PacketAllReceiversReady.class, PacketAllReceiversReady::toBytes, PacketAllReceiversReady::new, PacketAllReceiversReady::handle);
        net.registerMessageClient(id(), PacketTargetsReady.class, PacketTargetsReady::toBytes, PacketTargetsReady::new, PacketTargetsReady::handle);
        net.registerMessageClient(id(), PacketReturnScreenData.class, PacketReturnScreenData::toBytes, PacketReturnScreenData::new, PacketReturnScreenData::handle);
        net.registerMessageClient(id(), PacketChamberInfoReady.class, PacketChamberInfoReady::toBytes, PacketChamberInfoReady::new, PacketChamberInfoReady::handle);
        net.registerMessageClient(id(), PacketAdjacentBlocksReady.class, PacketAdjacentBlocksReady::toBytes, PacketAdjacentBlocksReady::new, PacketAdjacentBlocksReady::handle);
        net.registerMessageClient(id(), PacketAdjacentTankBlocksReady.class, PacketAdjacentTankBlocksReady::toBytes, PacketAdjacentTankBlocksReady::new, PacketAdjacentTankBlocksReady::handle);
        net.registerMessageClient(id(), PacketFiltersReady.class, PacketFiltersReady::toBytes, PacketFiltersReady::new, PacketFiltersReady::handle);
        net.registerMessageClient(id(), PacketSendBuffsToClient.class, PacketSendBuffsToClient::toBytes, PacketSendBuffsToClient::new, PacketSendBuffsToClient::handle);
        if (SecurityConfiguration.enabled.get()) {
            net.registerMessageClient(id(), PacketSecurityInfoReady.class, PacketSecurityInfoReady::toBytes, PacketSecurityInfoReady::new, PacketSecurityInfoReady::handle);
        }
        net.registerMessageClient(id(), PacketDelightingInfoReady.class, PacketDelightingInfoReady::toBytes, PacketDelightingInfoReady::new, PacketDelightingInfoReady::handle);
        if (NetworkMonitorConfiguration.enabled.get()) {
            net.registerMessageClient(id(), PacketConnectedBlocksReady.class, PacketConnectedBlocksReady::toBytes, PacketConnectedBlocksReady::new, PacketConnectedBlocksReady::handle);
        }
        net.registerMessageClient(id(), PacketSyncSlotsToClient.class, PacketSyncSlotsToClient::toBytes, PacketSyncSlotsToClient::new, PacketSyncSlotsToClient::handle);
        net.registerMessageClient(id(), PacketGridToClient.class, PacketGridToClient::toBytes, PacketGridToClient::new, PacketGridToClient::handle);
        net.registerMessageClient(id(), PacketCraftTestResultToClient.class, PacketCraftTestResultToClient::toBytes, PacketCraftTestResultToClient::new, PacketCraftTestResultToClient::handle);
        net.registerMessageClient(id(), PacketHudLogReady.class, PacketHudLogReady::toBytes, PacketHudLogReady::new, PacketHudLogReady::handle);
        net.registerMessageClient(id(), PacketReturnRfInRange.class, PacketReturnRfInRange::toBytes, PacketReturnRfInRange::new, PacketReturnRfInRange::handle);
        net.registerMessageClient(id(), PacketReturnShapeData.class, PacketReturnShapeData::toBytes, PacketReturnShapeData::new, PacketReturnShapeData::handle);
        net.registerMessageClient(id(), PacketProjectorClientNotification.class, PacketProjectorClientNotification::toBytes, PacketProjectorClientNotification::new, PacketProjectorClientNotification::handle);
        net.registerMessageClient(id(), PacketReturnExtraData.class, PacketReturnExtraData::toBytes, PacketReturnExtraData::new, PacketReturnExtraData::handle);
        net.registerMessageClient(id(), PacketReturnInventoryInfo.class, PacketReturnInventoryInfo::toBytes, PacketReturnInventoryInfo::new, PacketReturnInventoryInfo::handle);
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
