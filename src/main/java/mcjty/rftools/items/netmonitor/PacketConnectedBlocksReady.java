package mcjty.rftools.items.netmonitor;

import mcjty.rftools.BlockInfo;
import mcjty.varia.Coordinate;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

import java.util.HashMap;
import java.util.Map;

public class PacketConnectedBlocksReady implements IMessage, IMessageHandler<PacketConnectedBlocksReady, IMessage> {
    private int minx;
    private int miny;
    private int minz;
    private Map<Coordinate,BlockInfo> blockInfoMap;

    @Override
    public void fromBytes(ByteBuf buf) {
        minx = buf.readInt();
        miny = buf.readInt();
        minz = buf.readInt();

        int size = buf.readInt();
        blockInfoMap = new HashMap<Coordinate, BlockInfo>();
        for (int i = 0 ; i < size ; i++) {
            Coordinate coordinate = new Coordinate(buf.readShort() + minx, buf.readShort() + miny, buf.readShort() + minz);
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
        for (Map.Entry<Coordinate,BlockInfo> me : blockInfoMap.entrySet()) {
            Coordinate c = me.getKey();
            buf.writeShort(c.getX() - minx);
            buf.writeShort(c.getY() - miny);
            buf.writeShort(c.getZ() - minz);
            buf.writeInt(me.getValue().getEnergyStored());
            buf.writeInt(me.getValue().getMaxEnergyStored());
        }
    }

    public PacketConnectedBlocksReady() {
    }

    public PacketConnectedBlocksReady(Map<Coordinate,BlockInfo> blockInfoMap, int minx, int miny, int minz) {
        this.blockInfoMap = new HashMap<Coordinate, BlockInfo>(blockInfoMap);

        this.minx = minx;
        this.miny = miny;
        this.minz = minz;
    }

    @Override
    public IMessage onMessage(PacketConnectedBlocksReady message, MessageContext ctx) {
        GuiNetworkMonitor.setServerConnectedBlocks(message.blockInfoMap);
        return null;
    }

}
