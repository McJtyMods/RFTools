package com.mcjty.rftools.blocks.monitor;

import com.mcjty.varia.Coordinate;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;

import java.util.List;

public class PacketGetAdjacentBlocks implements IMessage, IMessageHandler<PacketGetAdjacentBlocks, PacketAdjacentBlocksReady> {
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

    public PacketGetAdjacentBlocks() {
    }

    public PacketGetAdjacentBlocks(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public PacketAdjacentBlocksReady onMessage(PacketGetAdjacentBlocks message, MessageContext ctx) {
        EntityPlayer player = ctx.getServerHandler().playerEntity;
        TileEntity te = player.worldObj.getTileEntity(message.x, message.y, message.z);
        if(!(te instanceof RFMonitorBlockTileEntity)) {
            // @Todo better logging
            System.out.println("createGetInventoryPacket: TileEntity is not a RFMonitorBlockTileEntity!");
            return null;
        }
        RFMonitorBlockTileEntity monitorBlockTileEntity = (RFMonitorBlockTileEntity) te;
        List<Coordinate> adjacentBlocks = monitorBlockTileEntity.findAdjacentBlocks();
        return new PacketAdjacentBlocksReady(message.x, message.y, message.z, adjacentBlocks);
    }

}
