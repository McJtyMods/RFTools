package mcjty.rftools.blocks.monitor;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.ByteBufConverter;
import mcjty.lib.network.NetworkTools;
import net.minecraft.util.math.BlockPos;

public class BlockPosNet implements ByteBufConverter {

    private final BlockPos pos;

    public BlockPosNet(BlockPos pos) {
        this.pos = pos;
    }

    public BlockPosNet(ByteBuf buf) {
        pos = NetworkTools.readPos(buf);
    }

    public BlockPos getPos() {
        return pos;
    }

    @Override
    public void toBytes(ByteBuf byteBuf) {
        byteBuf.writeInt(pos.getX());
        byteBuf.writeInt(pos.getY());
        byteBuf.writeInt(pos.getZ());
    }
}
