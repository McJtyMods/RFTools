package com.mcjty.rftools.blocks.monitor;

import com.mcjty.varia.Coordinate;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;

import java.util.ArrayList;
import java.util.List;

public class PacketAdjacentBlocksReady implements IMessage, IMessageHandler<PacketAdjacentBlocksReady, IMessage> {
    private int x;
    private int y;
    private int z;
    private List<Coordinate> coordinates;

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        int size = buf.readInt();
        coordinates = new ArrayList<Coordinate>();
        for (int i = 0 ; i < size ; i++) {
            coordinates.add(new Coordinate(buf.readInt(), buf.readInt(), buf.readInt()));
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeInt(coordinates.size());
        for (Coordinate c : coordinates) {
            buf.writeInt(c.getX());
            buf.writeInt(c.getY());
            buf.writeInt(c.getZ());
        }
    }

    public PacketAdjacentBlocksReady() {
    }

    public PacketAdjacentBlocksReady(int x, int y, int z, List<Coordinate> coordinates) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.coordinates = new ArrayList<Coordinate>();
        this.coordinates.addAll(coordinates);
    }

    @Override
    public IMessage onMessage(PacketAdjacentBlocksReady message, MessageContext ctx) {
        TileEntity te = Minecraft.getMinecraft().theWorld.getTileEntity(message.x, message.y, message.z);
        if(!(te instanceof RFMonitorBlockTileEntity)) {
            // @Todo better logging
            System.out.println("createInventoryReadyPacket: TileEntity is not a RFMonitorBlockTileEntity!");
            return null;
        }
        RFMonitorBlockTileEntity monitorBlockTileEntity = (RFMonitorBlockTileEntity) te;
        monitorBlockTileEntity.storeAdjacentBlocksForClient(message.coordinates);
        return null;
    }

}
