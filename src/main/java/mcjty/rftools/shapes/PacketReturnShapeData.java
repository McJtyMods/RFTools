package mcjty.rftools.shapes;

import mcjty.lib.network.NetworkTools;
import mcjty.rftools.blocks.builder.BuilderSetup;
import mcjty.rftools.varia.RLE;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketReturnShapeData {
    private ShapeID id;
    private RLE positions;
    private StatePalette statePalette;
    private int count;
    private int offsetY;
    private String msg;
    private BlockPos dimension;

    public void toBytes(PacketBuffer buf) {
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
//                buf.writeInt(state.getBlock().getMetaFromState(state));   // @todo 1.14 persist blockstate here!
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

    public PacketReturnShapeData(PacketBuffer buf) {
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
//                int m = buf.readInt();    // @todo 1.14 no meta!
//                Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(r));
//                statePalette.add(block.getStateFromMeta(m));
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

    public PacketReturnShapeData(ShapeID id, RLE positions, StatePalette statePalette, BlockPos dimension, int count, int offsetY, String msg) {
        this.id = id;
        this.positions = positions;
        this.statePalette = statePalette;
        this.dimension = dimension;
        this.count = count;
        this.offsetY = offsetY;
        this.msg = msg;
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
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