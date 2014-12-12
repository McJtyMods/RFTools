package com.mcjty.rftools;

import com.mcjty.rftools.dimension.RfToolsDimensionManager;
import com.mcjty.rftools.network.DimensionSyncPacket;
import com.mcjty.rftools.network.PacketHandler;
import com.mcjty.rftools.network.PacketRegisterDimensions;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import cpw.mods.fml.common.network.FMLOutboundHandler;
import cpw.mods.fml.common.network.handshake.NetworkDispatcher;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;

public class PlayerLoginEvent {
//    @SubscribeEvent
//    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event){
//        RFTools.log("Player logged in!");
//        EntityPlayer player = event.player;
//        RfToolsDimensionManager manager = RfToolsDimensionManager.getDimensionManager(player.getEntityWorld());
//        for (Integer id : manager.getDimensions().keySet()) {
//            RFTools.log("Sending over dimension " + id + " to the client");
//            PacketHandler.INSTANCE.sendTo(new PacketRegisterDimensions(id), (EntityPlayerMP) player);
//        }
//    }

    @SubscribeEvent
    public void onConnectionCreated(FMLNetworkEvent.ServerConnectionFromClientEvent event) {
        RFTools.log("onConnectionCreated");

        DimensionSyncPacket packet = new DimensionSyncPacket();

        EntityPlayer player = ((NetHandlerPlayServer) event.handler).playerEntity;
        RfToolsDimensionManager manager = RfToolsDimensionManager.getDimensionManager(player.getEntityWorld());
        for (Integer id : manager.getDimensions().keySet()) {
            RFTools.log("Sending over dimension " + id + " to the client");
            packet.addDimension(id);
        }

        RFTools.channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.DISPATCHER);
        RFTools.channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(event.manager.channel().attr(NetworkDispatcher.FML_DISPATCHER).get());
        RFTools.channels.get(Side.SERVER).writeOutbound(packet);
    }


}
