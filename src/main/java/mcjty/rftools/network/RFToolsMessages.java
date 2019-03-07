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
import mcjty.rftools.jei.PacketSendRecipe;
import mcjty.rftools.playerprops.PacketSendBuffsToClient;
import mcjty.rftools.shapes.*;
import mcjty.theoneprobe.TheOneProbe;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

import javax.annotation.Nonnull;

public class RFToolsMessages {
    public static SimpleNetworkWrapper INSTANCE;

    private static int id() {
        return PacketHandler.nextPacketID();
    }

    public static void registerMessages(String name) {
        SimpleChannel net2 = ChannelBuilder
                .named(new ResourceLocation(TheOneProbe.MODID, name))
                .networkProtocolVersion(() -> "1.0")
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();

        INSTANCE = net2.getNetwork();

        // Server side
        if (CrafterConfiguration.enabled) {
//            net2.registerMessage(PacketCrafter.Handler.class, PacketCrafter.class, PacketHandler.nextPacketID(), Side.SERVER);
            net2.registerMessageServer(id(), PacketCrafter.class, PacketCrafter::toBytes, PacketCrafter::new, PacketCrafter::handle);
        }

        SimpleNetworkWrapper net = INSTANCE;

        net2.registerMessageServer(id(), PacketGetPlayers.class, PacketGetPlayers::toBytes, PacketGetPlayers::new, PacketGetPlayers::handle);
        net2.registerMessageServer(id(), PacketGetReceivers.class, PacketGetReceivers::toBytes, PacketGetReceivers::new, PacketGetReceivers::handle);
        net2.registerMessageServer(id(), PacketGetAllReceivers.class, PacketGetAllReceivers::toBytes, PacketGetAllReceivers::new, PacketGetAllReceivers::handle);
        net2.registerMessageServer(id(), PacketGetTransmitters.class, PacketGetTransmitters::toBytes, PacketGetTransmitters::new, PacketGetTransmitters::handle);
        net2.registerMessageServer(id(), PacketModuleUpdate.class, PacketModuleUpdate::toBytes, PacketModuleUpdate::new, PacketModuleUpdate::handle);
        net2.registerMessageServer(id(), PacketGetScreenData.class, PacketGetScreenData::toBytes, PacketGetScreenData::new, PacketGetScreenData::handle);
        net2.registerMessageServer(id(), PacketGetAdjacentBlocks.class, PacketGetAdjacentBlocks::toBytes, PacketGetAdjacentBlocks::new, PacketGetAdjacentBlocks::handle);
        net2.registerMessageServer(id(), PacketGetAdjacentTankBlocks.class, PacketGetAdjacentTankBlocks::toBytes, PacketGetAdjacentTankBlocks::new, PacketGetAdjacentTankBlocks::handle);
        net2.registerMessageServer(id(), PacketContentsMonitor.class, PacketContentsMonitor::toBytes, PacketContentsMonitor::new, PacketContentsMonitor::handle);
        net2.registerMessageServer(id(), PacketGetFilters.class, PacketGetFilters::toBytes, PacketGetFilters::new, PacketGetFilters::handle);
        net2.registerMessageServer(id(), PacketGetDelightingInfo.class, PacketGetDelightingInfo::toBytes, PacketGetDelightingInfo::new, PacketGetDelightingInfo::handle);
        if (NetworkMonitorConfiguration.enabled) {
            net2.registerMessageServer(id(), PacketGetConnectedBlocks.class, PacketGetConnectedBlocks::toBytes, PacketGetConnectedBlocks::new, PacketGetConnectedBlocks::handle);
        }
        net2.registerMessageServer(id(), PacketSendRecipe.class, PacketSendRecipe::toBytes, PacketSendRecipe::new, PacketSendRecipe::handle);
        net2.registerMessageServer(id(), PacketGridToServer.class, PacketGridToServer::toBytes, PacketGridToServer::new, PacketGridToServer::handle);
        net2.registerMessageServer(id(), PacketRequestItem.class, PacketRequestItem::toBytes, PacketRequestItem::new, PacketRequestItem::handle);
        net2.registerMessageServer(id(), PacketGetHudLog.class, PacketGetHudLog::toBytes, PacketGetHudLog::new, PacketGetHudLog::handle);
        net2.registerMessageServer(id(), PacketSendComposerData.class, PacketSendComposerData::toBytes, PacketSendComposerData::new, PacketSendComposerData::handle);
        net2.registerMessageServer(id(), PacketUpdateModifier.class, PacketUpdateModifier::toBytes, PacketUpdateModifier::new, PacketUpdateModifier::handle);
        net2.registerMessageServer(id(), PacketRequestShapeData.class, PacketRequestShapeData::toBytes, PacketRequestShapeData::new, PacketRequestShapeData::handle);
        net2.registerMessageServer(id(), PacketUpdateNBTShapeCard.class, PacketUpdateNBTShapeCard::toBytes, PacketUpdateNBTShapeCard::new, PacketUpdateNBTShapeCard::handle);
        net2.registerMessageServer(id(), PacketUpdateNBTItemInventoryShape.class, PacketUpdateNBTItemInventoryShape::toBytes, PacketUpdateNBTItemInventoryShape::new, PacketUpdateNBTItemInventoryShape::handle);
        net2.registerMessageServer(id(), PacketUpdateNBTItemFilter.class, PacketUpdateNBTItemFilter::toBytes, PacketUpdateNBTItemFilter::new, PacketUpdateNBTItemFilter::handle);
        net2.registerMessageServer(id(), PacketUpdateNBTItemStorage.class, PacketUpdateNBTItemStorage::toBytes, PacketUpdateNBTItemStorage::new, PacketUpdateNBTItemStorage::handle);
        net2.registerMessageServer(id(), PacketGetInventoryInfo.class, PacketGetInventoryInfo::toBytes, PacketGetInventoryInfo::new, PacketGetInventoryInfo::handle);

        // Client side
        net2.registerMessageClient(id(), PacketPlayersReady.class, PacketPlayersReady::toBytes, PacketPlayersReady::new, PacketPlayersReady::handle);
        net2.registerMessageClient(id(), PacketTransmittersReady.class, PacketTransmittersReady::toBytes, PacketTransmittersReady::new, PacketTransmittersReady::handle);
        net2.registerMessageClient(id(), PacketReceiversReady.class, PacketReceiversReady::toBytes, PacketReceiversReady::new, PacketReceiversReady::handle);
        net2.registerMessageClient(id(), PacketAllReceiversReady.class, PacketAllReceiversReady::toBytes, PacketAllReceiversReady::new, PacketAllReceiversReady::handle);
        net2.registerMessageClient(id(), PacketTargetsReady.class, PacketTargetsReady::toBytes, PacketTargetsReady::new, PacketTargetsReady::handle);
        net2.registerMessageClient(id(), PacketReturnScreenData.class, PacketReturnScreenData::toBytes, PacketReturnScreenData::new, PacketReturnScreenData::handle);
        net2.registerMessageClient(id(), PacketChamberInfoReady.class, PacketChamberInfoReady::toBytes, PacketChamberInfoReady::new, PacketChamberInfoReady::handle);
        net2.registerMessageClient(id(), PacketAdjacentBlocksReady.class, PacketAdjacentBlocksReady::toBytes, PacketAdjacentBlocksReady::new, PacketAdjacentBlocksReady::handle);
        net.registerMessage(PacketAdjacentTankBlocksReady.Handler.class, PacketAdjacentTankBlocksReady.class, id(), Side.CLIENT);
        net.registerMessage(PacketFiltersReady.Handler.class, PacketFiltersReady.class, id(), Side.CLIENT);
        net.registerMessage(PacketSendBuffsToClient.Handler.class, PacketSendBuffsToClient.class, id(), Side.CLIENT);
        if (SecurityConfiguration.enabled) {
            net.registerMessage(PacketSecurityInfoReady.Handler.class, PacketSecurityInfoReady.class, id(), Side.CLIENT);
        }
        net.registerMessage(PacketDelightingInfoReady.Handler.class, PacketDelightingInfoReady.class, id(), Side.CLIENT);
        if (NetworkMonitorConfiguration.enabled) {
            net.registerMessage(PacketConnectedBlocksReady.Handler.class, PacketConnectedBlocksReady.class, id(), Side.CLIENT);
        }
        net.registerMessage(PacketSyncSlotsToClient.Handler.class, PacketSyncSlotsToClient.class, id(), Side.CLIENT);
        net.registerMessage(PacketGridToClient.Handler.class, PacketGridToClient.class, id(), Side.CLIENT);
        net.registerMessage(PacketCraftTestResultToClient.Handler.class, PacketCraftTestResultToClient.class, id(), Side.CLIENT);
        net.registerMessage(PacketHudLogReady.Handler.class, PacketHudLogReady.class, id(), Side.CLIENT);
        net.registerMessage(PacketReturnRfInRange.Handler.class, PacketReturnRfInRange.class, id(), Side.CLIENT);
        net.registerMessage(PacketReturnShapeData.Handler.class, PacketReturnShapeData.class, id(), Side.CLIENT);
        net.registerMessage(PacketProjectorClientNotification.Handler.class, PacketProjectorClientNotification.class, id(), Side.CLIENT);
        net.registerMessage(PacketReturnExtraData.Handler.class, PacketReturnExtraData.class, id(), Side.CLIENT);
        net.registerMessage(PacketReturnInventoryInfo.Handler.class, PacketReturnInventoryInfo.class, id(), Side.CLIENT);
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
