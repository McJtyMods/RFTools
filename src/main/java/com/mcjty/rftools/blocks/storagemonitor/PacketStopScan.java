package com.mcjty.rftools.blocks.storagemonitor;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;

public class PacketStopScan implements IMessage, IMessageHandler<PacketStopScan, IMessage> {
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

    public PacketStopScan() {
    }

    public PacketStopScan(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public IMessage onMessage(PacketStopScan message, MessageContext ctx) {
        EntityPlayer player = ctx.getServerHandler().playerEntity;
        TileEntity te = player.worldObj.getTileEntity(message.x, message.y, message.z);
        if(!(te instanceof StorageMonitorTileEntity)) {
            // @Todo better logging
            System.out.println("createStartScanPacket: TileEntity is not a StorageMonitorTileEntity!");
            return null;
        }
        StorageMonitorTileEntity storageMonitorTileEntity = (StorageMonitorTileEntity) te;
        storageMonitorTileEntity.stopScan();
        return null;
    }

}
