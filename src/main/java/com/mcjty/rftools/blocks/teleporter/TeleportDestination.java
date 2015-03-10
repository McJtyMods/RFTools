package com.mcjty.rftools.blocks.teleporter;

import com.mcjty.rftools.network.ByteBufConverter;
import com.mcjty.varia.Coordinate;
import io.netty.buffer.ByteBuf;

public class TeleportDestination implements ByteBufConverter {
    private final Coordinate coordinate;
    private final int dimension;
    private String name = "";

    public TeleportDestination(ByteBuf buf) {
        int cx = buf.readInt();
        int cy = buf.readInt();
        int cz = buf.readInt();
        if (cx == -1 && cy == -1 && cz == -1) {
            coordinate = null;
        } else {
            coordinate = new Coordinate(cx, cy, cz);
        }
        dimension = buf.readInt();
        byte[] dst = new byte[buf.readInt()];
        buf.readBytes(dst);
        setName(new String(dst));
    }

    public TeleportDestination(Coordinate coordinate, int dimension) {
        this.coordinate = coordinate;
        this.dimension = dimension;
    }

    public boolean isValid() {
        return coordinate != null;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        if (coordinate == null) {
            buf.writeInt(-1);
            buf.writeInt(-1);
            buf.writeInt(-1);
        } else {
            buf.writeInt(coordinate.getX());
            buf.writeInt(coordinate.getY());
            buf.writeInt(coordinate.getZ());
        }
        buf.writeInt(dimension);
        buf.writeInt(getName().length());
        buf.writeBytes(getName().getBytes());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null) {
            this.name = "";
        } else {
            this.name = name;
        }
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public int getDimension() {
        return dimension;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TeleportDestination that = (TeleportDestination) o;

        if (dimension != that.dimension) {
            return false;
        }
        if (coordinate != null ? !coordinate.equals(that.coordinate) : that.coordinate != null) {
            return false;
        }
        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = coordinate != null ? coordinate.hashCode() : 0;
        result = 31 * result + dimension;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}
