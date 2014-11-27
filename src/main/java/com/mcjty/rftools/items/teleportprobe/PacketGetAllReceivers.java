package com.mcjty.rftools.items.teleportprobe;

import com.mcjty.rftools.blocks.teleporter.TeleportDestination;
import com.mcjty.rftools.blocks.teleporter.TeleportDestinationClientInfo;
import com.mcjty.rftools.blocks.teleporter.TeleportDestinations;
import com.mcjty.varia.Coordinate;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

import java.util.ArrayList;
import java.util.List;

public class PacketGetAllReceivers implements IMessage, IMessageHandler<PacketGetAllReceivers, PacketAllReceiversReady> {
    @Override
    public void fromBytes(ByteBuf buf) {
    }

    @Override
    public void toBytes(ByteBuf buf) {
    }

    public PacketGetAllReceivers() {
    }

    @Override
    public PacketAllReceiversReady onMessage(PacketGetAllReceivers message, MessageContext ctx) {
        EntityPlayer player = ctx.getServerHandler().playerEntity;
        TeleportDestinations destinations = TeleportDestinations.getDestinations(player.worldObj);
        List<TeleportDestinationClientInfo> destinationList = new ArrayList<TeleportDestinationClientInfo> (destinations.getValidDestinations());
        addDimensions(destinationList);
        return new PacketAllReceiversReady(destinationList);
    }

    private void addDimensions(List<TeleportDestinationClientInfo> destinationList) {
        WorldServer[] worlds = DimensionManager.getWorlds();
        for (WorldServer world : worlds) {
            int id = world.provider.dimensionId;
            TeleportDestination destination = new TeleportDestination(new Coordinate(0, 70, 0), id);
            destination.setName("Dimension: " + id);
            TeleportDestinationClientInfo teleportDestinationClientInfo = new TeleportDestinationClientInfo(destination);
            destinationList.add(teleportDestinationClientInfo);
        }
    }

}
