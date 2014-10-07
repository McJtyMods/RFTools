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

public class PacketTransmittersReady implements IMessage, IMessageHandler<PacketTransmittersReady, IMessage> {
    private int x;
    private int y;
    private int z;
    private List<TransmitterInfo> transmitters;

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        int size = buf.readInt();
        transmitters = new ArrayList<TransmitterInfo>();
        for (int i = 0 ; i < size ; i++) {
            Coordinate c = new Coordinate(buf.readInt(), buf.readInt(), buf.readInt());
            byte[] dst = new byte[buf.readInt()];
            buf.readBytes(dst);
            transmitters.add(new TransmitterInfo(c, new String(dst)));
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeInt(transmitters.size());
        for (TransmitterInfo info : transmitters) {
            buf.writeInt(info.getCoordinate().getX());
            buf.writeInt(info.getCoordinate().getY());
            buf.writeInt(info.getCoordinate().getZ());
            buf.writeInt(info.getName().length());
            buf.writeBytes(info.getName().getBytes());
        }
    }

    public PacketTransmittersReady() {
    }

    public PacketTransmittersReady(int x, int y, int z, List<TransmitterInfo> transmitters) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.transmitters = new ArrayList<TransmitterInfo>();
        this.transmitters.addAll(transmitters);
    }

    @Override
    public IMessage onMessage(PacketTransmittersReady message, MessageContext ctx) {
        TileEntity te = Minecraft.getMinecraft().theWorld.getTileEntity(message.x, message.y, message.z);
        if(!(te instanceof DialingDeviceTileEntity)) {
            // @Todo better logging
            System.out.println("createInventoryReadyPacket: TileEntity is not a DialingDeviceTileEntity!");
            return null;
        }
        DialingDeviceTileEntity dialingDeviceTileEntity = (DialingDeviceTileEntity) te;
        dialingDeviceTileEntity.storeTransmittersForClient(message.transmitters);
        return null;
    }

}
