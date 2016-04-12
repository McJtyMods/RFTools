package mcjty.rftools.items.netmonitor;

import io.netty.buffer.ByteBuf;
import mcjty.rftools.BlockInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.HashMap;
import java.util.Map;

public class PacketConnectedBlocksReady implements IMessage {
    private int minx;
    private int miny;
    private int minz;
    private Map<BlockPos,BlockInfo> blockInfoMap;

    @Override
    public void fromBytes(ByteBuf buf) {
        minx = buf.readInt();
        miny = buf.readInt();
        minz = buf.readInt();

        int size = buf.readInt();
        blockInfoMap = new HashMap<BlockPos, BlockInfo>();
        for (int i = 0 ; i < size ; i++) {
            BlockPos coordinate = new BlockPos(buf.readShort() + minx, buf.readShort() + miny, buf.readShort() + minz);
            BlockInfo blockInfo = new BlockInfo(coordinate, buf.readInt(), buf.readInt());
            blockInfoMap.put(coordinate, blockInfo);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(minx);
        buf.writeInt(miny);
        buf.writeInt(minz);

        buf.writeInt(blockInfoMap.size());
        for (Map.Entry<BlockPos,BlockInfo> me : blockInfoMap.entrySet()) {
            BlockPos c = me.getKey();
            buf.writeShort(c.getX() - minx);
            buf.writeShort(c.getY() - miny);
            buf.writeShort(c.getZ() - minz);
            buf.writeInt(me.getValue().getEnergyStored());
            buf.writeInt(me.getValue().getMaxEnergyStored());
        }
    }

    public PacketConnectedBlocksReady() {
    }

    public PacketConnectedBlocksReady(Map<BlockPos,BlockInfo> blockInfoMap, int minx, int miny, int minz) {
        this.blockInfoMap = new HashMap<BlockPos, BlockInfo>(blockInfoMap);

        this.minx = minx;
        this.miny = miny;
        this.minz = minz;
    }

    public static class Handler implements IMessageHandler<PacketConnectedBlocksReady, IMessage> {
        @Override
        public IMessage onMessage(PacketConnectedBlocksReady message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketConnectedBlocksReady message, MessageContext ctx) {
            GuiNetworkMonitor.setServerConnectedBlocks(message.blockInfoMap);
        }

    }
}
