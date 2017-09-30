package mcjty.rftools.shapes;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.rftools.RFTools;
import mcjty.rftools.varia.RLE;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;

public class PacketReturnShapeData implements IMessage {
    private int id;
    private RLE positions;
    private StatePalette statePalette;
    private int count;
    private String msg;
    private BlockPos dimension;

    @Override
    public void fromBytes(ByteBuf buf) {
        id = buf.readInt();
        count = buf.readInt();
        msg = NetworkTools.readStringUTF8(buf);
        dimension = NetworkTools.readPos(buf);

        int size = buf.readInt();
        if (size == 0) {
            statePalette = null;
        } else {
            statePalette = new StatePalette();
            while (size > 0) {
                String r = NetworkTools.readString(buf);
                int m = buf.readInt();
                Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(r));
                statePalette.add(block.getStateFromMeta(m));
                size--;
            }
        }

        size = buf.readInt();
        if (size == 0) {
            positions = null;
        } else {
            positions = new RLE();
            byte[] data = new byte[size];
            buf.readBytes(data);
            positions.setData(data);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(id);
        buf.writeInt(count);
        NetworkTools.writeStringUTF8(buf, msg);
        NetworkTools.writePos(buf, dimension);

        if (statePalette == null) {
            buf.writeInt(0);
        } else {
            buf.writeInt(statePalette.getPalette().size());
            for (IBlockState state : statePalette.getPalette()) {
                if (state.getBlock().getRegistryName() == null) {
                    state = Blocks.STONE.getDefaultState();
                }
                NetworkTools.writeString(buf, state.getBlock().getRegistryName().toString());
                buf.writeInt(state.getBlock().getMetaFromState(state));
            }
        }

        if (positions == null) {
            buf.writeInt(0);
        } else {
            buf.writeInt(positions.getData().length);
            buf.writeBytes(positions.getData());
        }
    }

    public PacketReturnShapeData() {
    }

    public PacketReturnShapeData(int id, RLE positions, StatePalette statePalette, BlockPos dimension, int count, String msg) {
        this.id = id;
        this.positions = positions;
        this.statePalette = statePalette;
        this.dimension = dimension;
        this.count = count;
        this.msg = msg;
    }

    public static class Handler implements IMessageHandler<PacketReturnShapeData, IMessage> {
        @Override
        public IMessage onMessage(PacketReturnShapeData message, MessageContext ctx) {
            RFTools.proxy.addScheduledTaskClient(() -> handle(message));
            return null;
        }

        private void handle(PacketReturnShapeData message) {
//            Map<Long, IBlockState> positions = new HashMap<>();
            int dx = message.dimension.getX();
            int dy = message.dimension.getY();
            int dz = message.dimension.getZ();

            ShapeRenderer.RenderColumn columns[] = new ShapeRenderer.RenderColumn[dx * dz];
            RLE rle = message.positions;

            if (rle != null) {
                rle.reset();
                for (int ox = 0; ox < dx; ox++) {
                    int x = ox - dx / 2;
                    for (int oz = 0; oz < dz; oz++) {
                        int z = oz - dz / 2;

                        ShapeRenderer.RenderColumn column = new ShapeRenderer.RenderColumn(new BlockPos(x, -dy/2, z));
                        columns[ox*dz+oz] = column;

                        for (int oy = 0; oy < dy; oy++) {
//                            int y = oy - dy / 2;
                            int data = rle.read();
                            if (data < 255) {
                                if (data == 0) {
//                                    positions.put(BlockPosHelper.toLong(x, y, z), null);
                                    column.add(null);
                                } else {
                                    data--;
//                                    positions.put(BlockPosHelper.toLong(x, y, z), message.statePalette.getPalette().get(data));
                                    column.add(message.statePalette.getPalette().get(data));
                                }
                            } else {
                                column.add(null); // @todo, two kinds of null: no data or unknown texture!
                            }
                        }

                        column.close();
                    }
                }
            }
            ShapeRenderer.setRenderData(message.id, null/* positions*/, columns, message.count, message.msg);
        }

    }
}