package mcjty.rftools.blocks.teleporter;

import io.netty.buffer.ByteBuf;
import mcjty.network.NetworkTools;

public class TeleportDestinationClientInfo extends TeleportDestination implements Comparable<TeleportDestinationClientInfo> {

    private String dimensionName = "";
    private boolean favorite = false;

    public TeleportDestinationClientInfo(TeleportDestinationClientInfo clientInfo) {
        super(clientInfo.getCoordinate(), clientInfo.getDimension());
        dimensionName = clientInfo.dimensionName;
        favorite = clientInfo.favorite;
    }

    public TeleportDestinationClientInfo(ByteBuf buf) {
        super(buf);
        setDimensionName(NetworkTools.readString(buf));
        setFavorite(buf.readBoolean());
    }

    public TeleportDestinationClientInfo(TeleportDestination destination) {
        super(destination.getCoordinate(), destination.getDimension());
        setName(destination.getName());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        super.toBytes(buf);
        NetworkTools.writeString(buf, getDimensionName());
        buf.writeBoolean(favorite);
    }

    public String getDimensionName() {
        return dimensionName;
    }

    public void setDimensionName(String dimensionName) {
        this.dimensionName = dimensionName;
    }

    public void setFavorite(boolean favorite) { this.favorite = favorite; }

    public boolean isFavorite() {
        return favorite;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        TeleportDestinationClientInfo that = (TeleportDestinationClientInfo) o;

        if (favorite != that.favorite) return false;
        if (dimensionName != null ? !dimensionName.equals(that.dimensionName) : that.dimensionName != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (dimensionName != null ? dimensionName.hashCode() : 0);
        result = 31 * result + (favorite ? 1 : 0);
        return result;
    }
}
