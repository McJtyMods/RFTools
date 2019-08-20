package mcjty.rftools.shapes;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.thirteen.Context;
import mcjty.rftools.blocks.builder.BuilderSetup;
import mcjty.rftools.varia.RLE;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.function.Supplier;

public class PacketReturnShapeData implements IMessage {
    private ShapeID id;
    private RLE positions;
    private StatePalette statePalette;
    private int count;
    private int offsetY;
    private String msg;
    private BlockPos dimension;

    @Override
    public void fromBytes(ByteBuf buf) {
        id = new ShapeID(buf);
        count = buf.readInt();
        offsetY = buf.readInt();
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
        id.toBytes(buf);
        buf.writeInt(count);
        buf.writeInt(offsetY);
        NetworkTools.writeStringUTF8(buf, msg);
        NetworkTools.writePos(buf, dimension);

        if (statePalette == null) {
            buf.writeInt(0);
        } else {
            buf.writeInt(statePalette.getPalette().size());
            for (BlockState state : statePalette.getPalette()) {
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

    public PacketReturnShapeData(ByteBuf buf) {
        fromBytes(buf);
    }

    public PacketReturnShapeData(ShapeID id, RLE positions, StatePalette statePalette, BlockPos dimension, int count, int offsetY, String msg) {
        this.id = id;
        this.positions = positions;
        this.statePalette = statePalette;
        this.dimension = dimension;
        this.count = count;
        this.offsetY = offsetY;
        this.msg = msg;
    }

    public void handle(Supplier<Context> supplier) {
        Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            int dx = dimension.getX();
            int dy = dimension.getY();
            int dz = dimension.getZ();

            RLE rle = positions;
            RenderData.RenderPlane plane = null;

            if (rle != null) {
                BlockState dummy = BuilderSetup.supportBlock.getDefaultState();

                rle.reset();
//                for (int oy = 0; oy < dy; oy++) {
                int oy = offsetY;
                int y = oy - dy / 2;

                RenderData.RenderStrip strips[] = new RenderData.RenderStrip[dx];
                for (int ox = 0; ox < dx; ox++) {
                    int x = ox - dx / 2;

                    RenderData.RenderStrip strip = new RenderData.RenderStrip(x);
                    strips[ox] = strip;

                    for (int oz = 0; oz < dz; oz++) {
                        int data = rle.read();
                        if (data < 255) {
                            if (data == 0) {
                                strip.add(dummy);
                            } else {
                                data--;
                                strip.add(statePalette.getPalette().get(data));
                            }
                        } else {
                            strip.add(null);
                        }
                    }

                    strip.close();
                    plane = new RenderData.RenderPlane(strips, y, oy, -dz / 2, count);
                }
            }
            ShapeRenderer.setRenderData(id, plane, offsetY, dy, msg);
        });
        ctx.setPacketHandled(true);
    }
}