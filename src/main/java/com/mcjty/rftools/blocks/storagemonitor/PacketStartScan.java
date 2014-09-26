package com.mcjty.rftools.blocks.storagemonitor;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;

public class PacketStartScan implements IMessage, IMessageHandler<PacketStartScan, IMessage> {
    private int x;
    private int y;
    private int z;
    private boolean start;

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        start = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeBoolean(start);
    }

    public PacketStartScan() {
    }

    public PacketStartScan(int x, int y, int z, boolean start) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.start = start;
    }

    @Override
    public IMessage onMessage(PacketStartScan message, MessageContext ctx) {
        EntityPlayer player = ctx.getServerHandler().playerEntity;
        TileEntity te = player.worldObj.getTileEntity(message.x, message.y, message.z);
        if(!(te instanceof StorageScannerTileEntity)) {
            // @Todo better logging
            System.out.println("createStartScanPacket: TileEntity is not a StorageScannerTileEntity!");
            return null;
        }
        StorageScannerTileEntity storageScannerTileEntity = (StorageScannerTileEntity) te;
        storageScannerTileEntity.startScan(message.start);
        return null;
    }

}
