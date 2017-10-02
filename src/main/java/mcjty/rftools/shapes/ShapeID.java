package mcjty.rftools.shapes;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;

/// ID to identify a shape for a player/projector/scanner/...
public final class ShapeID {
    private final int dimension;
    @Nullable private final BlockPos pos;     // null if this is for a shapecard (i.e. shapecard gui in hand)
    private final int check;        // Check from the shapecard

    public ShapeID(int dimension, BlockPos pos, int check) {
        this.dimension = dimension;
        this.pos = pos;
        this.check = check;
    }

    public ShapeID(ByteBuf buf) {
        int dim = 0;
        BlockPos p = null;
        if (buf.readBoolean()) {
            dim = buf.readInt();
            p = NetworkTools.readPos(buf);
        }
        check = buf.readInt();
        dimension = dim;
        pos = p;
    }

    public void toBytes(ByteBuf buf) {
        if (getPos() == null) {
            buf.writeBoolean(false);
        } else {
            buf.writeBoolean(true);
            buf.writeInt(getDimension());
            NetworkTools.writePos(buf, getPos());
        }
        buf.writeInt(getCheck());
    }



    public int getDimension() {
        return dimension;
    }

    @Nullable
    public BlockPos getPos() {
        return pos;
    }

    public int getCheck() {
        return check;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ShapeID shapeID = (ShapeID) o;

        if (dimension != shapeID.dimension) return false;
        if (check != shapeID.check) return false;
        return pos != null ? pos.equals(shapeID.pos) : shapeID.pos == null;

    }

    @Override
    public int hashCode() {
        int result = dimension;
        result = 31 * result + (pos != null ? pos.hashCode() : 0);
        result = 31 * result + check;
        return result;
    }
}
