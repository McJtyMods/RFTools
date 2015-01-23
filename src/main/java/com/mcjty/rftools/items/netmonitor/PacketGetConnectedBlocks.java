package com.mcjty.rftools.items.netmonitor;

import cofh.api.energy.IEnergyHandler;
import com.mcjty.rftools.BlockInfo;
import com.mcjty.varia.Coordinate;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

public class PacketGetConnectedBlocks implements IMessage, IMessageHandler<PacketGetConnectedBlocks, PacketConnectedBlocksReady> {
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

    public PacketGetConnectedBlocks() {
    }

    public PacketGetConnectedBlocks(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public PacketConnectedBlocksReady onMessage(PacketGetConnectedBlocks message, MessageContext ctx) {
        EntityPlayer player = ctx.getServerHandler().playerEntity;
        HashMap<Coordinate,BlockInfo> connectedBlocks = new HashMap<Coordinate, BlockInfo>();
        findConnectedBlocks(connectedBlocks, player.worldObj, message.x, message.y, message.z);

        int minx = 300000000;
        int miny = 300000000;
        int minz = 300000000;
        for (Coordinate coordinate : connectedBlocks.keySet()) {
            minx = Math.min(minx, coordinate.getX());
            miny = Math.min(miny, coordinate.getY());
            minz = Math.min(minz, coordinate.getZ());
        }

        return new PacketConnectedBlocksReady(connectedBlocks, minx, miny, minz);
    }

    private void findConnectedBlocks(Map<Coordinate,BlockInfo> connectedBlocks, World world, int x, int y, int z) {
        if (y < 0 || y >= world.getActualHeight()) {
            return;
        }
        Coordinate c = new Coordinate(x, y, z);
        if (connectedBlocks.containsKey(c)) {
            return;
        }
        TileEntity tileEntity = world.getTileEntity(x, y, z);
        if (tileEntity != null) {
            if (tileEntity instanceof IEnergyHandler) {
                connectedBlocks.put(c, new BlockInfo(tileEntity, c));
                findConnectedBlocks(connectedBlocks, world, x + 1, y, z);
                findConnectedBlocks(connectedBlocks, world, x - 1, y, z);
                findConnectedBlocks(connectedBlocks, world, x, y - 1, z);
                findConnectedBlocks(connectedBlocks, world, x, y + 1, z);
                findConnectedBlocks(connectedBlocks, world, x, y, z - 1);
                findConnectedBlocks(connectedBlocks, world, x, y, z + 1);
            }
        }
    }


}
