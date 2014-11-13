package com.mcjty.rftools.blocks.teleporter;

import io.netty.buffer.ByteBuf;

public class TeleportDestinationClientInfo extends TeleportDestination {

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
}
