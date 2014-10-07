package com.mcjty.rftools.blocks.teleporter;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;

import java.util.List;

public class PacketGetReceivers implements IMessage, IMessageHandler<PacketGetReceivers, PacketReceiversReady> {
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

    public PacketGetReceivers() {
    }

    public PacketGetReceivers(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public PacketReceiversReady onMessage(PacketGetReceivers message, MessageContext ctx) {
        EntityPlayer player = ctx.getServerHandler().playerEntity;
        TileEntity te = player.worldObj.getTileEntity(message.x, message.y, message.z);
        if(!(te instanceof DialingDeviceTileEntity)) {
            // @Todo better logging
            System.out.println("createStartScanPacket: TileEntity is not a DialingDeviceTileEntity!");
            return null;
        }
        DialingDeviceTileEntity dialingDeviceTileEntity = (DialingDeviceTileEntity) te;
        List<TeleportDestination> destinations = dialingDeviceTileEntity.searchReceivers();
        return new PacketReceiversReady(message.x, message.y, message.z, destinations);
    }

}
