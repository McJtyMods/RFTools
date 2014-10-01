package com.mcjty.rftools.blocks.storagemonitor;

import com.mcjty.varia.Coordinate;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;

import java.util.List;
import java.util.Set;

public class PacketSearchItems implements IMessage, IMessageHandler<PacketSearchItems, PacketSearchReady> {
    private int x;
    private int y;
    private int z;
    private String search;

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        byte[] dst = new byte[buf.readInt()];
        buf.readBytes(dst);
        search = new String(dst);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeInt(search.length());
        buf.writeBytes(search.getBytes());
    }

    public PacketSearchItems() {
    }

    public PacketSearchItems(int x, int y, int z, String search) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.search = search;
    }

    @Override
    public PacketSearchReady onMessage(PacketSearchItems message, MessageContext ctx) {
        EntityPlayer player = ctx.getServerHandler().playerEntity;
        TileEntity te = player.worldObj.getTileEntity(message.x, message.y, message.z);
        if(!(te instanceof StorageScannerTileEntity)) {
            // @Todo better logging
            System.out.println("createStartScanPacket: TileEntity is not a StorageScannerTileEntity!");
            return null;
        }
        StorageScannerTileEntity storageScannerTileEntity = (StorageScannerTileEntity) te;
        Set<Coordinate> coordinates = storageScannerTileEntity.startSearch(message.search);
        return new PacketSearchReady(message.x, message.y, message.z, coordinates);
    }

}
