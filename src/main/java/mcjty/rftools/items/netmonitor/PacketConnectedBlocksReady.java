package mcjty.rftools.items.netmonitor;

import io.netty.buffer.ByteBuf;
import mcjty.lib.thirteen.Context;
import mcjty.rftools.varia.BlockInfo;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class PacketConnectedBlocksReady implements IMessage {
    private int minx;
    private int miny;
    private int minz;
    private Map<BlockPos, BlockInfo> blockInfoMap;

    @Override
    public void fromBytes(ByteBuf buf) {
        minx = buf.readInt();
        miny = buf.readInt();
        minz = buf.readInt();

        int size = buf.readInt();
        blockInfoMap = new HashMap<>();
        for (int i = 0; i < size; i++) {
            BlockPos coordinate = new BlockPos(buf.readShort() + minx, buf.readShort() + miny, buf.readShort() + minz);
            BlockInfo blockInfo = new BlockInfo(coordinate, buf.readLong(), buf.readLong());
            blockInfoMap.put(coordinate, blockInfo);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(minx);
        buf.writeInt(miny);
        buf.writeInt(minz);

        buf.writeInt(blockInfoMap.size());
        for (Map.Entry<BlockPos, BlockInfo> me : blockInfoMap.entrySet()) {
            BlockPos c = me.getKey();
            buf.writeShort(c.getX() - minx);
            buf.writeShort(c.getY() - miny);
            buf.writeShort(c.getZ() - minz);
            buf.writeLong(me.getValue().getStoredPower());
            buf.writeLong(me.getValue().getCapacity());
        }
    }

    public PacketConnectedBlocksReady() {
    }

    public PacketConnectedBlocksReady(ByteBuf buf) {
        fromBytes(buf);
    }

    public PacketConnectedBlocksReady(Map<BlockPos, BlockInfo> blockInfoMap, int minx, int miny, int minz) {
        this.blockInfoMap = new HashMap<>(blockInfoMap);

        this.minx = minx;
        this.miny = miny;
        this.minz = minz;
    }

    public void handle(Supplier<Context> supplier) {
        Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            GuiNetworkMonitor.setServerConnectedBlocks(blockInfoMap);
        });
        ctx.setPacketHandled(true);
    }
}

