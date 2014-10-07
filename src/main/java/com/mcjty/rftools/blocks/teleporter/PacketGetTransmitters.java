package com.mcjty.rftools.blocks.teleporter;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;

import java.util.List;

public class PacketGetTransmitters implements IMessage, IMessageHandler<PacketGetTransmitters, PacketTransmittersReady> {
    private int x;
    private int y;
    private int z;

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
    }

    public PacketGetTransmitters() {
    }

    public PacketGetTransmitters(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public PacketTransmittersReady onMessage(PacketGetTransmitters message, MessageContext ctx) {
        TileEntity te = ctx.getServerHandler().playerEntity.worldObj.getTileEntity(message.x, message.y, message.z);
        if(!(te instanceof DialingDeviceTileEntity)) {
            // @Todo better logging
            System.out.println("createStartScanPacket: TileEntity is not a DialingDeviceTileEntity!");
            return null;
        }
        DialingDeviceTileEntity dialingDeviceTileEntity = (DialingDeviceTileEntity) te;
        List<TransmitterInfo> coordinates = dialingDeviceTileEntity.searchTransmitters();
        return new PacketTransmittersReady(message.x, message.y, message.z, coordinates);
    }

}
