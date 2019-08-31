package mcjty.rftools.items.netmonitor;

import mcjty.lib.network.NetworkTools;
import mcjty.lib.varia.EnergyTools;
import mcjty.rftools.network.RFToolsMessages;
import mcjty.rftools.varia.BlockInfo;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.*;
import java.util.function.Supplier;

public class PacketGetConnectedBlocks {
    private BlockPos pos;

    public void toBytes(PacketBuffer buf) {
        NetworkTools.writePos(buf, pos);
    }

    public PacketGetConnectedBlocks() {
    }

    public PacketGetConnectedBlocks(PacketBuffer buf) {
        pos = NetworkTools.readPos(buf);
    }

    public PacketGetConnectedBlocks(BlockPos pos) {
        this.pos = pos;
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            PlayerEntity player = ctx.getSender();
            Map<BlockPos,BlockInfo> connectedBlocks = new HashMap<>();
            findConnectedBlocks(connectedBlocks, player.getEntityWorld(), pos);

            if (connectedBlocks.size() > NetworkMonitorConfiguration.maximumBlocks.get()) {
                connectedBlocks = compactConnectedBlocks(connectedBlocks, pos, NetworkMonitorConfiguration.maximumBlocks.get());
            }

            int minx = 300000000;
            int miny = 300000000;
            int minz = 300000000;
            for (BlockPos coordinate : connectedBlocks.keySet()) {
                minx = Math.min(minx, coordinate.getX());
                miny = Math.min(miny, coordinate.getY());
                minz = Math.min(minz, coordinate.getZ());
            }
            RFToolsMessages.INSTANCE.sendTo(new PacketConnectedBlocksReady(connectedBlocks, minx, miny, minz), ctx.getSender().connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
        });
        ctx.setPacketHandled(true);
    }

    private Map<BlockPos,BlockInfo> compactConnectedBlocks(Map<BlockPos,BlockInfo> old, final BlockPos pos, int max) {
        List<BlockPos> list = new ArrayList<>(old.keySet());
        Collections.sort(list, Comparator.comparingInt(o1 -> (o1.getX()-pos.getX()) * (o1.getX()-pos.getX()) + (o1.getY()-pos.getY()) * (o1.getY()-pos.getY()) + (o1.getZ()-pos.getZ()) * (o1.getZ()-pos.getZ())));

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
            if (EnergyTools.isEnergyTE(tileEntity, null)) {
                connectedBlocks.put(pos, new BlockInfo(tileEntity, null, pos));
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
