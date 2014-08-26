package com.mcjty.rftools.network;

import com.mcjty.rftools.Coordinate;
import com.mcjty.rftools.blocks.RFMonitorBlockTileEntity;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;

public class PacketRFMonitor implements IMessage, IMessageHandler<PacketRFMonitor, IMessage> {
    private int x;
    private int y;
    private int z;

    private Coordinate monitor;

    private int alarmLevel;
    private boolean alarmMode;

    public PacketRFMonitor() {
        monitor = new Coordinate(-1, -1, -1);
        alarmLevel = -1;
        alarmMode = false;
    }

    public PacketRFMonitor(int x, int y, int z, Coordinate monitor) {
        this();
        this.x = x;
        this.y = y;
        this.z = z;
        this.monitor = monitor;
    }

    public PacketRFMonitor(int x, int y, int z, boolean alarmMode, int alarmLevel) {
        this();
        this.x = x;
        this.y = y;
        this.z = z;
        this.alarmLevel = alarmLevel;
        this.alarmMode = alarmMode;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        monitor = new Coordinate(buf.readInt(), buf.readInt(), buf.readInt());
        alarmLevel = buf.readInt();
        alarmMode = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeInt(monitor.getX());
        buf.writeInt(monitor.getY());
        buf.writeInt(monitor.getZ());
        buf.writeInt(alarmLevel);
        buf.writeBoolean(alarmMode);
    }

    @Override
    public IMessage onMessage(PacketRFMonitor message, MessageContext ctx) {
        EntityPlayer player = ctx.getServerHandler().playerEntity;
        System.out.println("    player.worldObj.isRemote = " + player.worldObj.isRemote);
        TileEntity te = player.worldObj.getTileEntity(message.x, message.y, message.z);
        if(!(te instanceof RFMonitorBlockTileEntity)) {
            // @Todo better logging
            System.out.println("createPowerMonitotPacket: TileEntity is not a RFMonitorBlockTileEntity!");
            return null;
        }
        RFMonitorBlockTileEntity monitorBlockTileEntity = (RFMonitorBlockTileEntity) te;
        if (monitor.getY() != -1) {
            monitorBlockTileEntity.setMonitor(message.monitor);
        }
        if (alarmLevel != -1) {
            monitorBlockTileEntity.setAlarm(alarmMode, alarmLevel);
        }
        return null;
    }
}
