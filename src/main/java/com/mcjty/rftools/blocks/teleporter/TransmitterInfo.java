package com.mcjty.rftools.blocks.teleporter;

import com.mcjty.rftools.network.ByteBufConverter;
import com.mcjty.varia.Coordinate;
import io.netty.buffer.ByteBuf;

public class TransmitterInfo implements ByteBufConverter {
    private final Coordinate coordinate;
    private final String name;

    public TransmitterInfo(ByteBuf buf) {
        coordinate = new Coordinate(buf.readInt(), buf.readInt(), buf.readInt());
        byte[] dst = new byte[buf.readInt()];
        buf.readBytes(dst);
        name = new String(dst);
    }

    public TransmitterInfo(Coordinate coordinate, String name) {
        this.coordinate = coordinate;
        this.name = name;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(coordinate.getX());
        buf.writeInt(coordinate.getY());
        buf.writeInt(coordinate.getZ());
        buf.writeInt(getName().length());
        buf.writeBytes(getName().getBytes());
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public String getName() {
        return name;
    }
}
