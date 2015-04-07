package mcjty.rftools.blocks.teleporter;

import io.netty.buffer.ByteBuf;
import mcjty.rftools.network.NetworkTools;

public class TeleportDestinationClientInfo extends TeleportDestination implements Comparable<TeleportDestinationClientInfo> {

    private String dimensionName = "";

    public TeleportDestinationClientInfo(ByteBuf buf) {
        super(buf);
        setDimensionName(NetworkTools.readString(buf));
    }

    public TeleportDestinationClientInfo(TeleportDestination destination) {
        super(destination.getCoordinate(), destination.getDimension());
        setName(destination.getName());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        super.toBytes(buf);
        NetworkTools.writeString(buf, getDimensionName());
    }

    public String getDimensionName() {
        return dimensionName;
    }

    public void setDimensionName(String dimensionName) {
        this.dimensionName = dimensionName;
    }

    @Override
    public int compareTo(TeleportDestinationClientInfo o) {
        if (getDimension() < o.getDimension()) {
            return -1;
        } else if (getDimension() > o.getDimension()) {
            return 1;
        }
        return getName().compareTo(o.getName());
    }
}
