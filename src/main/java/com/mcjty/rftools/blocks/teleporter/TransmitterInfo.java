package com.mcjty.rftools.blocks.teleporter;

import com.mcjty.rftools.network.ByteBufConverter;
import com.mcjty.varia.Coordinate;
import io.netty.buffer.ByteBuf;

public class TransmitterInfo implements ByteBufConverter {
    private final Coordinate coordinate;
    private final String name;
    private final TeleportDestination teleportDestination;

    public TransmitterInfo(ByteBuf buf) {
        coordinate = new Coordinate(buf.readInt(), buf.readInt(), buf.readInt());
        byte[] dst = new byte[buf.readInt()];
        buf.readBytes(dst);
        name = new String(dst);

        teleportDestination = new TeleportDestination(buf);
    }

    public TransmitterInfo(Coordinate coordinate, String name, TeleportDestination destination) {
        this.coordinate = coordinate;
        this.name = name;
        if (destination == null) {
            this.teleportDestination = new TeleportDestination(null, 0);
        } else {
            this.teleportDestination = destination;
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(coordinate.getX());
        buf.writeInt(coordinate.getY());
        buf.writeInt(coordinate.getZ());
        buf.writeInt(getName().length());
        buf.writeBytes(getName().getBytes());
        teleportDestination.toBytes(buf);
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public String getName() {
        return name;
    }

    public TeleportDestination getTeleportDestination() {
        return teleportDestination;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TransmitterInfo that = (TransmitterInfo) o;

        if (coordinate != null ? !coordinate.equals(that.coordinate) : that.coordinate != null) {
            return false;
        }
        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        if (!teleportDestination.equals(that.teleportDestination)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = coordinate != null ? coordinate.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (teleportDestination.hashCode());
        return result;
    }
}
