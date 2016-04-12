package mcjty.rftools.items.netmonitor;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.rftools.BlockInfo;
import mcjty.rftools.network.RFToolsMessages;
import mcjty.rftools.varia.EnergyTools;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.*;

public class PacketGetConnectedBlocks implements IMessage {
    private BlockPos pos;

    @Override
    public void fromBytes(ByteBuf buf) {
        pos = NetworkTools.readPos(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        NetworkTools.writePos(buf, pos);
    }

    public PacketGetConnectedBlocks() {
    }

    public PacketGetConnectedBlocks(BlockPos pos) {
        this.pos = pos;
    }

    public static class Handler implements IMessageHandler<PacketGetConnectedBlocks, IMessage> {
        @Override
        public IMessage onMessage(PacketGetConnectedBlocks message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketGetConnectedBlocks message, MessageContext ctx) {
            EntityPlayer player = ctx.getServerHandler().playerEntity;
            Map<BlockPos,BlockInfo> connectedBlocks = new HashMap<>();
            findConnectedBlocks(connectedBlocks, player.worldObj, message.pos);

            if (connectedBlocks.size() > NetworkMonitorConfiguration.maximumBlocks) {
                connectedBlocks = compactConnectedBlocks(connectedBlocks, message.pos, NetworkMonitorConfiguration.maximumBlocks);
            }

            int minx = 300000000;
            int miny = 300000000;
            int minz = 300000000;
            for (BlockPos coordinate : connectedBlocks.keySet()) {
                minx = Math.min(minx, coordinate.getX());
                miny = Math.min(miny, coordinate.getY());
                minz = Math.min(minz, coordinate.getZ());
            }
            RFToolsMessages.INSTANCE.sendTo(new PacketConnectedBlocksReady(connectedBlocks, minx, miny, minz), ctx.getServerHandler().playerEntity);
        }

        private Map<BlockPos,BlockInfo> compactConnectedBlocks(Map<BlockPos,BlockInfo> old, final BlockPos pos, int max) {
            List<BlockPos> list = new ArrayList<>(old.keySet());
            Collections.sort(list, new Comparator<BlockPos>() {
                @Override
                public int compare(BlockPos o1, BlockPos o2) {
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

                private int calcDist(BlockPos o1) {
                    return (o1.getX()-pos.getX()) * (o1.getX()-pos.getX()) + (o1.getY()-pos.getY()) * (o1.getY()-pos.getY()) + (o1.getZ()-pos.getZ()) * (o1.getZ()-pos.getZ());
                }
            });

            Map<BlockPos,BlockInfo> connectedBlocks = new HashMap<>();
            for (BlockPos coordinate : list) {
                connectedBlocks.put(coordinate, old.get(coordinate));
                max--;
                if (max <= 0) {
                    break;
                }
            }

            return connectedBlocks;
        }


        private void findConnectedBlocks(Map<BlockPos,BlockInfo> connectedBlocks, World world, BlockPos pos) {
            if (pos.getY() < 0 || pos.getY() >= world.getHeight()) {
                return;
            }
            if (connectedBlocks.containsKey(pos)) {
                return;
            }
            TileEntity tileEntity = world.getTileEntity(pos);
            if (tileEntity != null) {
                if (EnergyTools.isEnergyTE(tileEntity)) {
                    connectedBlocks.put(pos, new BlockInfo(tileEntity, pos));
                    findConnectedBlocks(connectedBlocks, world, pos.up());
                    findConnectedBlocks(connectedBlocks, world, pos.down());
                    findConnectedBlocks(connectedBlocks, world, pos.east());
                    findConnectedBlocks(connectedBlocks, world, pos.west());
                    findConnectedBlocks(connectedBlocks, world, pos.south());
                    findConnectedBlocks(connectedBlocks, world, pos.north());
                }
            }
        }
    }
}
