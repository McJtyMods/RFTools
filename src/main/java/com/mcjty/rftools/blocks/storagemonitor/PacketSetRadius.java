package com.mcjty.rftools.blocks.storagemonitor;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;

public class PacketSetRadius implements IMessage, IMessageHandler<PacketSetRadius, IMessage> {
    private int x;
    private int y;
    private int z;
    private int radius;

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        radius = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeInt(radius);
    }

    public PacketSetRadius() {
    }

    public PacketSetRadius(int x, int y, int z, int radius) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.radius = radius;
    }

    @Override
    public IMessage onMessage(PacketSetRadius message, MessageContext ctx) {
        TileEntity te = ctx.getServerHandler().playerEntity.worldObj.getTileEntity(message.x, message.y, message.z);
        if(!(te instanceof StorageScannerTileEntity)) {
            // @Todo better logging
            System.out.println("createStartScanPacket: TileEntity is not a StorageScannerTileEntity!");
            return null;
        }
        StorageScannerTileEntity storageScannerTileEntity = (StorageScannerTileEntity) te;
        storageScannerTileEntity.setRadius(message.radius);
        return null;
    }

}
