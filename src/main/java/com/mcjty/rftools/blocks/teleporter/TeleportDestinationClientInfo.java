package com.mcjty.rftools.blocks.teleporter;

import io.netty.buffer.ByteBuf;

public class TeleportDestinationClientInfo extends TeleportDestination implements Comparable<TeleportDestinationClientInfo> {

    private String dimensionName = "";

    public TeleportDestinationClientInfo(ByteBuf buf) {
        super(buf);
        byte[] dst = new byte[buf.readInt()];
        buf.readBytes(dst);
        setDimensionName(new String(dst));
    }

    public TeleportDestinationClientInfo(TeleportDestination destination) {
        super(destination.getCoordinate(), destination.getDimension());
        setName(destination.getName());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        super.toBytes(buf);
        buf.writeInt(getDimensionName().length());
        buf.writeBytes(getDimensionName().getBytes());
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
        return dimensionName.compareTo(o.dimensionName);
    }
}
