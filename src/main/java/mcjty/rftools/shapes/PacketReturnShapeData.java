package mcjty.rftools.shapes;

import gnu.trove.iterator.TLongIterator;
import gnu.trove.set.hash.TLongHashSet;
import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.rftools.RFTools;
import mcjty.rftools.network.ReturnDestinationInfoHelper;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;

import static mcjty.rftools.network.ReturnDestinationInfoHelper.id;
import static mcjty.rftools.network.ReturnDestinationInfoHelper.name;

public class PacketReturnShapeData implements IMessage {
    private TLongHashSet positions;
    private Map<Long, IBlockState> stateMap;
    private int count;

    @Override
    public void fromBytes(ByteBuf buf) {
        int size = buf.readInt();
        positions = new TLongHashSet();
        while (size > 0) {
            positions.add(buf.readLong());
            size--;
        }
        stateMap = new HashMap<>();
        size = buf.readInt();
        while (size > 0) {
            long key = buf.readLong();
            if (buf.readBoolean()) {
                String r = NetworkTools.readString(buf);
                int m = buf.readInt();
                Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(r));
                stateMap.put(key, block.getStateFromMeta(m));
            }
            size--;
        }
        count = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(positions.size());
        TLongIterator iterator = positions.iterator();
        while (iterator.hasNext()) {
            buf.writeLong(iterator.next());
        }
        buf.writeInt(stateMap.size());
        for (Map.Entry<Long, IBlockState> entry : stateMap.entrySet()) {
            buf.writeLong(entry.getKey());
            IBlockState state = entry.getValue();
            if (state == null) {
                buf.writeBoolean(false);
            } else {
                buf.writeBoolean(true);
                NetworkTools.writeString(buf, state.getBlock().getRegistryName().toString());
                buf.writeInt(state.getBlock().getMetaFromState(state));
            }
        }
        buf.writeInt(count);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public PacketReturnShapeData() {
    }

    public PacketReturnShapeData(TLongHashSet positions, Map<Long, IBlockState> stateMap, int count) {
        this.positions = positions;
        this.stateMap = stateMap;
        this.count = count;
    }

    public static class Handler implements IMessageHandler<PacketReturnShapeData, IMessage> {
        @Override
        public IMessage onMessage(PacketReturnShapeData message, MessageContext ctx) {
            RFTools.proxy.addScheduledTaskClient(() -> ShapeRenderer.setRenderData(message.positions, message.stateMap, message.count));
            return null;
        }

    }
}