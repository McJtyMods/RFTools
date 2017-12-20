package mcjty.rftools.network;

import mcjty.lib.network.Arguments;
import mcjty.lib.network.PacketHandler;
import mcjty.lib.network.PacketSendClientCommand;
import mcjty.lib.network.PacketSendServerCommand;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.builder.PacketChamberInfoReady;
import mcjty.rftools.blocks.crafter.PacketCrafter;
import mcjty.rftools.blocks.monitor.*;
import mcjty.rftools.blocks.powercell.PowerCellInfoPacketClient;
import mcjty.rftools.blocks.powercell.PowerCellInfoPacketServer;
import mcjty.rftools.blocks.screens.network.*;
import mcjty.rftools.blocks.security.PacketSecurityInfoReady;
import mcjty.rftools.blocks.shaper.PacketProjectorClientNotification;
import mcjty.rftools.blocks.shield.PacketFiltersReady;
import mcjty.rftools.blocks.shield.PacketGetFilters;
import mcjty.rftools.blocks.spawner.SpawnerInfoPacketClient;
import mcjty.rftools.blocks.spawner.SpawnerInfoPacketServer;
import mcjty.rftools.blocks.storage.PacketSyncSlotsToClient;
import mcjty.rftools.blocks.storage.PacketUpdateNBTItemStorage;
import mcjty.rftools.blocks.storagemonitor.*;
import mcjty.rftools.blocks.teleporter.*;
import mcjty.rftools.craftinggrid.PacketCraftTestResultToClient;
import mcjty.rftools.craftinggrid.PacketGridToClient;
import mcjty.rftools.craftinggrid.PacketGridToServer;
import mcjty.rftools.items.builder.PacketUpdateNBTItemInventoryShape;
import mcjty.rftools.items.creativeonly.PacketDelightingInfoReady;
import mcjty.rftools.items.creativeonly.PacketGetDelightingInfo;
import mcjty.rftools.items.modifier.PacketUpdateModifier;
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
        net.registerMessage(PacketCrafter.Handler.class, PacketCrafter.class, PacketHandler.nextID(), Side.SERVER);
        net.registerMessage(PacketGetPlayers.Handler.class, PacketGetPlayers.class, PacketHandler.nextID(), Side.SERVER);
        net.registerMessage(PacketGetReceivers.Handler.class, PacketGetReceivers.class, PacketHandler.nextID(), Side.SERVER);
        net.registerMessage(PacketGetAllReceivers.Handler.class, PacketGetAllReceivers.class, PacketHandler.nextID(), Side.SERVER);
        net.registerMessage(PacketGetTransmitters.Handler.class, PacketGetTransmitters.class, PacketHandler.nextID(), Side.SERVER);
        net.registerMessage(PacketModuleUpdate.Handler.class, PacketModuleUpdate.class, PacketHandler.nextID(), Side.SERVER);
        net.registerMessage(PacketGetScreenData.Handler.class, PacketGetScreenData.class, PacketHandler.nextID(), Side.SERVER);
        net.registerMessage(PacketGetAdjacentBlocks.Handler.class, PacketGetAdjacentBlocks.class, PacketHandler.nextID(), Side.SERVER);
        net.registerMessage(PacketGetAdjacentTankBlocks.Handler.class, PacketGetAdjacentTankBlocks.class, PacketHandler.nextID(), Side.SERVER);
        net.registerMessage(PacketContentsMonitor.Handler.class, PacketContentsMonitor.class, PacketHandler.nextID(), Side.SERVER);
        net.registerMessage(PacketGetFilters.Handler.class, PacketGetFilters.class, PacketHandler.nextID(), Side.SERVER);
        net.registerMessage(PacketGetDelightingInfo.Handler.class, PacketGetDelightingInfo.class, PacketHandler.nextID(), Side.SERVER);
        net.registerMessage(PacketGetConnectedBlocks.Handler.class, PacketGetConnectedBlocks.class, PacketHandler.nextID(), Side.SERVER);
        net.registerMessage(PacketSendRecipe.Handler.class, PacketSendRecipe.class, PacketHandler.nextID(), Side.SERVER);
        net.registerMessage(PacketGridToServer.Handler.class, PacketGridToServer.class, PacketHandler.nextID(), Side.SERVER);
        net.registerMessage(PacketRequestItem.Handler.class, PacketRequestItem.class, PacketHandler.nextID(), Side.SERVER);
        net.registerMessage(PacketGetHudLog.Handler.class, PacketGetHudLog.class, PacketHandler.nextID(), Side.SERVER);
        net.registerMessage(PacketSendComposerData.Handler.class, PacketSendComposerData.class, PacketHandler.nextID(), Side.SERVER);
        net.registerMessage(PacketUpdateModifier.Handler.class, PacketUpdateModifier.class, PacketHandler.nextID(), Side.SERVER);
        net.registerMessage(PacketRequestShapeData.Handler.class, PacketRequestShapeData.class, PacketHandler.nextID(), Side.SERVER);
        net.registerMessage(PacketUpdateNBTShapeCard.Handler.class, PacketUpdateNBTShapeCard.class, PacketHandler.nextID(), Side.SERVER);
        net.registerMessage(PacketUpdateNBTItemInventoryShape.Handler.class, PacketUpdateNBTItemInventoryShape.class, PacketHandler.nextID(), Side.SERVER);
        net.registerMessage(PacketUpdateNBTItemFilter.Handler.class, PacketUpdateNBTItemFilter.class, PacketHandler.nextID(), Side.SERVER);
        net.registerMessage(PacketUpdateNBTItemStorage.Handler.class, PacketUpdateNBTItemStorage.class, PacketHandler.nextID(), Side.SERVER);

        // Client side
        net.registerMessage(PacketPlayersReady.Handler.class, PacketPlayersReady.class, PacketHandler.nextID(), Side.CLIENT);
        net.registerMessage(PacketTransmittersReady.Handler.class, PacketTransmittersReady.class, PacketHandler.nextID(), Side.CLIENT);
        net.registerMessage(PacketReceiversReady.Handler.class, PacketReceiversReady.class, PacketHandler.nextID(), Side.CLIENT);
        net.registerMessage(PacketAllReceiversReady.Handler.class, PacketAllReceiversReady.class, PacketHandler.nextID(), Side.CLIENT);
        net.registerMessage(PacketTargetsReady.Handler.class, PacketTargetsReady.class, PacketHandler.nextID(), Side.CLIENT);
        net.registerMessage(PacketReturnScreenData.Handler.class, PacketReturnScreenData.class, PacketHandler.nextID(), Side.CLIENT);
        net.registerMessage(PacketChamberInfoReady.Handler.class, PacketChamberInfoReady.class, PacketHandler.nextID(), Side.CLIENT);
        net.registerMessage(PacketAdjacentBlocksReady.Handler.class, PacketAdjacentBlocksReady.class, PacketHandler.nextID(), Side.CLIENT);
        net.registerMessage(PacketAdjacentTankBlocksReady.Handler.class, PacketAdjacentTankBlocksReady.class, PacketHandler.nextID(), Side.CLIENT);
        net.registerMessage(PacketFiltersReady.Handler.class, PacketFiltersReady.class, PacketHandler.nextID(), Side.CLIENT);
        net.registerMessage(PacketSendBuffsToClient.Handler.class, PacketSendBuffsToClient.class, PacketHandler.nextID(), Side.CLIENT);
        net.registerMessage(PacketSecurityInfoReady.Handler.class, PacketSecurityInfoReady.class, PacketHandler.nextID(), Side.CLIENT);
        net.registerMessage(PacketDelightingInfoReady.Handler.class, PacketDelightingInfoReady.class, PacketHandler.nextID(), Side.CLIENT);
        net.registerMessage(PacketConnectedBlocksReady.Handler.class, PacketConnectedBlocksReady.class, PacketHandler.nextID(), Side.CLIENT);
        net.registerMessage(PacketSyncSlotsToClient.Handler.class, PacketSyncSlotsToClient.class, PacketHandler.nextID(), Side.CLIENT);
        net.registerMessage(PacketGridToClient.Handler.class, PacketGridToClient.class, PacketHandler.nextID(), Side.CLIENT);
        net.registerMessage(PacketCraftTestResultToClient.Handler.class, PacketCraftTestResultToClient.class, PacketHandler.nextID(), Side.CLIENT);
        net.registerMessage(PacketHudLogReady.Handler.class, PacketHudLogReady.class, PacketHandler.nextID(), Side.CLIENT);
        net.registerMessage(PacketReturnRfInRange.Handler.class, PacketReturnRfInRange.class, PacketHandler.nextID(), Side.CLIENT);
        net.registerMessage(PacketReturnShapeData.Handler.class, PacketReturnShapeData.class, PacketHandler.nextID(), Side.CLIENT);
        net.registerMessage(PacketProjectorClientNotification.Handler.class, PacketProjectorClientNotification.class, PacketHandler.nextID(), Side.CLIENT);
        net.registerMessage(PacketReturnExtraData.Handler.class, PacketReturnExtraData.class, PacketHandler.nextID(), Side.CLIENT);

        PacketHandler.register(PacketHandler.nextPacketID(), PowerCellInfoPacketServer.class, PowerCellInfoPacketClient.class);
        PacketHandler.register(PacketHandler.nextPacketID(), InventoriesInfoPacketServer.class, InventoriesInfoPacketClient.class);
        PacketHandler.register(PacketHandler.nextPacketID(), SearchItemsInfoPacketServer.class, SearchItemsInfoPacketClient.class);
        PacketHandler.register(PacketHandler.nextPacketID(), GetContentsInfoPacketServer.class, GetContentsInfoPacketClient.class);
        PacketHandler.register(PacketHandler.nextPacketID(), SpawnerInfoPacketServer.class, SpawnerInfoPacketClient.class);
        PacketHandler.register(PacketHandler.nextPacketID(), ScreenInfoPacketServer.class, ScreenInfoPacketClient.class);
        PacketHandler.register(PacketHandler.nextPacketID(), ScannerInfoPacketServer.class, ScannerInfoPacketClient.class);
    }

    public static void sendToServer(String command, @Nonnull Arguments.Builder argumentBuilder) {
        INSTANCE.sendToServer(new PacketSendServerCommand(RFTools.MODID, command, argumentBuilder.build()));
    }

    public static void sendToServer(String command) {
        INSTANCE.sendToServer(new PacketSendServerCommand(RFTools.MODID, command, Arguments.EMPTY));
    }

    public static void sendToClient(EntityPlayer player, String command, @Nonnull Arguments.Builder argumentBuilder) {
        INSTANCE.sendTo(new PacketSendClientCommand(RFTools.MODID, command, argumentBuilder.build()), (EntityPlayerMP) player);
    }

    public static void sendToClient(EntityPlayer player, String command) {
        INSTANCE.sendTo(new PacketSendClientCommand(RFTools.MODID, command, Arguments.EMPTY), (EntityPlayerMP) player);
    }
}
