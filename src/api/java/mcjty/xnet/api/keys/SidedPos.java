package mcjty.xnet.api.keys;

import mcjty.lib.varia.BlockPosTools;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;

public class SidedPos {
    private final BlockPos pos;
    private final EnumFacing side;

    /**
     * A position of a connected block and the side relative
     * from this block where the connection is. Basically
     * pos.offset(side) will be the consumer/connector
     */
    public SidedPos(@Nonnull BlockPos pos, @Nonnull EnumFacing side) {
        this.pos = pos;
        this.side = side;
    }

    @Nonnull
    public BlockPos getPos() {
        return pos;
    }

    /**
     * Get the side relative to this position for the connector.
     */
    @Nonnull
    public EnumFacing getSide() {
        return side;
    }

    @Override
    public String toString() {
        return "SidedPos{" + BlockPosTools.toString(pos) + "/" + side.getName() + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SidedPos sidedPos = (SidedPos) o;

        if (pos != null ? !pos.equals(sidedPos.pos) : sidedPos.pos != null) return false;
        if (side != sidedPos.side) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = pos != null ? pos.hashCode() : 0;
        result = 31 * result + (side != null ? side.hashCode() : 0);
        return result;
    }
}
