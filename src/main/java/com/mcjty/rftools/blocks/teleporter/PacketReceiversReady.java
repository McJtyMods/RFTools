package com.mcjty.rftools.blocks.teleporter;

import com.mcjty.varia.Coordinate;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;

import java.util.ArrayList;
import java.util.List;

public class PacketReceiversReady implements IMessage, IMessageHandler<PacketReceiversReady, IMessage> {
    private int x;
    private int y;
    private int z;
    private List<TeleportDestination> destinations;

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        int size = buf.readInt();
        destinations = new ArrayList<TeleportDestination>();
        for (int i = 0 ; i < size ; i++) {
            Coordinate c = new Coordinate(buf.readInt(), buf.readInt(), buf.readInt());
            int dim = buf.readInt();
            byte[] dst = new byte[buf.readInt()];
            buf.readBytes(dst);
            TeleportDestination destination = new TeleportDestination(c, dim);
            destination.setName(new String(dst));
            destinations.add(destination);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeInt(destinations.size());
        for (TeleportDestination destination : destinations) {
            buf.writeInt(destination.getCoordinate().getX());
            buf.writeInt(destination.getCoordinate().getY());
            buf.writeInt(destination.getCoordinate().getZ());
            buf.writeInt(destination.getDimension());
            buf.writeInt(destination.getName().length());
            buf.writeBytes(destination.getName().getBytes());
        }
    }

    public PacketReceiversReady() {
    }

    public PacketReceiversReady(int x, int y, int z, List<TeleportDestination> destinations) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.destinations = new ArrayList<TeleportDestination>();
        this.destinations.addAll(destinations);
    }

    @Override
    public IMessage onMessage(PacketReceiversReady message, MessageContext ctx) {
        TileEntity te = Minecraft.getMinecraft().theWorld.getTileEntity(message.x, message.y, message.z);
        if(!(te instanceof DialingDeviceTileEntity)) {
            // @Todo better logging
            System.out.println("createInventoryReadyPacket: TileEntity is not a DialingDeviceTileEntity!");
            return null;
        }
        DialingDeviceTileEntity dialingDeviceTileEntity = (DialingDeviceTileEntity) te;
        dialingDeviceTileEntity.storeReceiversForClient(message.destinations);
        return null;
    }

}
