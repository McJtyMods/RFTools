package mcjty.rftools.items.netmonitor;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import mcjty.lib.varia.Coordinate;
import mcjty.rftools.BlockInfo;
import mcjty.rftools.varia.EnergyTools;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import java.util.*;

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
        Map<Coordinate,BlockInfo> connectedBlocks = new HashMap<Coordinate, BlockInfo>();
        findConnectedBlocks(connectedBlocks, player.worldObj, message.x, message.y, message.z);

        if (connectedBlocks.size() > NetworkMonitorConfiguration.maximumBlocks) {
            connectedBlocks = compactConnectedBlocks(connectedBlocks, message.x, message.y, message.z, NetworkMonitorConfiguration.maximumBlocks);
        }

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

    private Map<Coordinate,BlockInfo> compactConnectedBlocks(Map<Coordinate,BlockInfo> old, final int x, final int y, final int z, int max) {
        List<Coordinate> list = new ArrayList<Coordinate>(old.keySet());
        Collections.sort(list, new Comparator<Coordinate>() {
            @Override
            public int compare(Coordinate o1, Coordinate o2) {
                double sqdist1 = calcDist(o1);
                double sqdist2 = calcDist(o2);
                if (sqdist1 < sqdist2) {
                    return -1;
                } else if (sqdist1 > sqdist2) {
                    return 1;
                } else {
                    return 0;
                }
            }

            private int calcDist(Coordinate o1) {
                return (o1.getX()-x) * (o1.getX()-x) + (o1.getY()-y) * (o1.getY()-y) + (o1.getZ()-z) * (o1.getZ()-z);
            }
        });

        Map<Coordinate,BlockInfo> connectedBlocks = new HashMap<Coordinate, BlockInfo>();
        for (Coordinate coordinate : list) {
            connectedBlocks.put(coordinate, old.get(coordinate));
            max--;
            if (max <= 0) {
                break;
            }
        }

        return connectedBlocks;
    }


    private void findConnectedBlocks(Map<Coordinate,BlockInfo> connectedBlocks, World world, int x, int y, int z) {
        if (y < 0 || y >= world.getHeight()) {
            return;
        }
        Coordinate c = new Coordinate(x, y, z);
        if (connectedBlocks.containsKey(c)) {
            return;
        }
        TileEntity tileEntity = world.getTileEntity(x, y, z);
        if (tileEntity != null) {
            if (EnergyTools.isEnergyTE(tileEntity)) {
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
