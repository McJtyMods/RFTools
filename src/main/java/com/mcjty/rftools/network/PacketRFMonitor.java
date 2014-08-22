package com.mcjty.rftools.network;

import com.mcjty.rftools.Coordinate;
import com.mcjty.rftools.blocks.RFMonitorBlockTileEntity;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;

public class PacketRFMonitor implements IMessage, IMessageHandler<PacketRFMonitor, IMessage> {
    private int x;
    private int y;
    private int z;
    private Coordinate monitor;

    public PacketRFMonitor() {
    }

    public PacketRFMonitor(int x, int y, int z, Coordinate monitor) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.monitor = monitor;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        System.out.println("com.mcjty.rftools.network.PacketRFMonitor.fromBytes");
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        monitor = new Coordinate(buf.readInt(), buf.readInt(), buf.readInt());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        System.out.println("com.mcjty.rftools.network.PacketRFMonitor.toBytes");
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeInt(monitor.getX());
        buf.writeInt(monitor.getY());
        buf.writeInt(monitor.getZ());
    }

    @Override
    public IMessage onMessage(PacketRFMonitor message, MessageContext ctx) {
        System.out.println("com.mcjty.rftools.network.PacketRFMonitor.onMessage");
        EntityPlayer player = ctx.getServerHandler().playerEntity;
        System.out.println("    player.worldObj.isRemote = " + player.worldObj.isRemote);
        TileEntity te = player.worldObj.getTileEntity(message.x, message.y, message.z);
        if(!(te instanceof RFMonitorBlockTileEntity)) {
            // @Todo better logging
            System.out.println("createPowerMonitotPacket: Could not handle packet as TileEntity was not a TilePowerMonitor.");
            return null;
        }
        RFMonitorBlockTileEntity monitorBlockTileEntity = (RFMonitorBlockTileEntity) te;
        System.out.println("    message.monitor = " + message.monitor);
        System.out.println("    message.x = " + message.x);
        System.out.println("    message.y = " + message.y);
        System.out.println("    message.z = " + message.z);
        monitorBlockTileEntity.setMonitor(message.monitor);
        player.worldObj.markBlockForUpdate(x, y, z);
        return null;
    }
}
