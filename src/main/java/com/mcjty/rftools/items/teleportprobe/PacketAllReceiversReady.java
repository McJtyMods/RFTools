package com.mcjty.rftools.items.teleportprobe;

import com.mcjty.rftools.blocks.teleporter.TeleportDestination;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;

public class PacketAllReceiversReady implements IMessage, IMessageHandler<PacketAllReceiversReady, IMessage> {
    private List<TeleportDestination> destinationList;

    @Override
    public void fromBytes(ByteBuf buf) {
        int size = buf.readInt();
        destinationList = new ArrayList<TeleportDestination>(size);
        for (int i = 0 ; i < size ; i++) {
            destinationList.add(new TeleportDestination(buf));
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(destinationList.size());
        for (TeleportDestination destination : destinationList) {
            destination.toBytes(buf);
        }
    }

    public PacketAllReceiversReady() {
    }

    public PacketAllReceiversReady(List<TeleportDestination> destinationList) {
        this.destinationList = new ArrayList<TeleportDestination>();
        this.destinationList.addAll(destinationList);
    }

    @Override
    public IMessage onMessage(PacketAllReceiversReady message, MessageContext ctx) {
        GuiTeleportProbe.setReceivers(message.destinationList);
        return null;
    }

}
