package mcjty.rftools.shapes;

import gnu.trove.iterator.TLongIterator;
import gnu.trove.set.hash.TLongHashSet;
import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.rftools.RFTools;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PacketReturnShapeData implements IMessage {
    private Map<Long, IBlockState> positions;
    private int count;

    @Override
    public void fromBytes(ByteBuf buf) {
        int size = buf.readInt();
        System.out.println("1: size = " + size);
        List<IBlockState> palette = new ArrayList<>();
        while (size > 0) {
            String r = NetworkTools.readString(buf);
            int m = buf.readInt();
            Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(r));
            palette.add(block.getStateFromMeta(m));
            size--;
        }

        size = buf.readInt();
        System.out.println("2: size = " + size);
        positions = new HashMap<>();
        while (size > 0) {
            long pos = buf.readLong();
            int index = ((int) buf.readByte()) & 0xff;
            IBlockState state = null;
            if (index != 255) {
                if (index >= palette.size()) {
//                    System.out.println("index = " + index);
                } else {
                    state = palette.get(index);
                }
            }
            positions.put(pos, state);
            size--;
        }
        count = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        System.out.println("#########################################");
        System.out.println("positions.size() = " + positions.size());


        // First make a palette for more compact transmission
        StatePalette palette = new StatePalette();
        for (Map.Entry<Long, IBlockState> entry : positions.entrySet()) {
            IBlockState state = entry.getValue();
            if (state != null) {
                palette.alloc(state);
            }
        }

        buf.writeInt(palette.getPalette().size());
        for (IBlockState state : palette.getPalette()) {
            NetworkTools.writeString(buf, state.getBlock().getRegistryName().toString());
            buf.writeInt(state.getBlock().getMetaFromState(state));
        }

        buf.writeInt(positions.size());
        for (Map.Entry<Long, IBlockState> entry : positions.entrySet()) {
            long pos = entry.getKey();
            buf.writeLong(pos);
            IBlockState state = entry.getValue();
            if (state == null) {
                buf.writeByte(255); // Indicates no entry (palette only goes up to 254)
            } else {
                buf.writeByte(palette.alloc(state));
            }
        }

        buf.writeInt(count);
    }

    public PacketReturnShapeData() {
    }

    public PacketReturnShapeData(Map<Long, IBlockState> positions,int count) {
        this.positions = positions;
        this.count = count;
    }

    public static class Handler implements IMessageHandler<PacketReturnShapeData, IMessage> {
        @Override
        public IMessage onMessage(PacketReturnShapeData message, MessageContext ctx) {
            RFTools.proxy.addScheduledTaskClient(() -> ShapeRenderer.setRenderData(message.positions, message.count));
            return null;
        }

    }
}