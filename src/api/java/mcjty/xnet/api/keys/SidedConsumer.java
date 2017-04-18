package mcjty.xnet.api.keys;

import net.minecraft.util.EnumFacing;

import javax.annotation.Nonnull;

public class SidedConsumer {

    private final ConsumerId consumerId;
    private final EnumFacing side;

    /**
     * A consumer ID and a side pointing towards the block
     * we are connecting too.
     */
    public SidedConsumer(@Nonnull ConsumerId consumerId, @Nonnull EnumFacing side) {
        this.consumerId = consumerId;
        this.side = side;
    }

    @Nonnull
    public ConsumerId getConsumerId() {
        return consumerId;
    }

    /**
     * Get the side as seen from this consumer of the connector
     * to an adjacent block.
     */
    @Nonnull
    public EnumFacing getSide() {
        return side;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SidedConsumer that = (SidedConsumer) o;

        if (consumerId != null ? !consumerId.equals(that.consumerId) : that.consumerId != null) return false;
        return side == that.side;

    }

    @Override
    public int hashCode() {
        int result = consumerId != null ? consumerId.hashCode() : 0;
        result = 31 * result + (side != null ? side.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SidedConsumer{id=" + consumerId + "/" + side + '}';
    }
}
