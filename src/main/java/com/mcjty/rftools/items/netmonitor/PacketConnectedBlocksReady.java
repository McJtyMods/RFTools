package com.mcjty.rftools.items.netmonitor;

import com.mcjty.rftools.BlockInfo;
import com.mcjty.varia.Coordinate;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

import java.util.HashMap;
import java.util.Map;

public class PacketConnectedBlocksReady implements IMessage, IMessageHandler<PacketConnectedBlocksReady, IMessage> {
    private Map<Coordinate,BlockInfo> blockInfoMap;

    @Override
    public void fromBytes(ByteBuf buf) {
        int size = buf.readInt();
        blockInfoMap = new HashMap<Coordinate, BlockInfo>();
        for (int i = 0 ; i < size ; i++) {
            Coordinate coordinate = new Coordinate(buf.readInt(), buf.readInt(), buf.readInt());
            BlockInfo blockInfo = new BlockInfo(buf.readBoolean(), coordinate, buf.readInt(), buf.readInt());
            blockInfoMap.put(coordinate, blockInfo);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(blockInfoMap.size());
        for (Map.Entry<Coordinate,BlockInfo> me : blockInfoMap.entrySet()) {
            Coordinate c = me.getKey();
            buf.writeInt(c.getX());
            buf.writeInt(c.getY());
            buf.writeInt(c.getZ());
            buf.writeBoolean(me.getValue().isFirst());
            buf.writeInt(me.getValue().getEnergyStored());
            buf.writeInt(me.getValue().getMaxEnergyStored());
        }
    }

    public PacketConnectedBlocksReady() {
    }

    public PacketConnectedBlocksReady(Map<Coordinate,BlockInfo> blockInfoMap) {
        this.blockInfoMap = new HashMap<Coordinate, BlockInfo>();
        this.blockInfoMap.putAll(blockInfoMap);
    }

    @Override
    public IMessage onMessage(PacketConnectedBlocksReady message, MessageContext ctx) {
        GuiNetworkMonitor.setServerConnectedBlocks(message.blockInfoMap);
        return null;
    }

}
