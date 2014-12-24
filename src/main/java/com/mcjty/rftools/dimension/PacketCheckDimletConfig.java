package com.mcjty.rftools.dimension;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

import java.util.HashMap;
import java.util.Map;

public class PacketCheckDimletConfig implements IMessage {
    Map<Integer, String> dimlets;

    @Override
    public void fromBytes(ByteBuf buf) {
        int size = buf.readInt();
        dimlets = new HashMap<Integer, String>();
        for (int i = 0 ; i < size ; i++) {
            int id = buf.readInt();
            byte[] dst = new byte[buf.readInt()];
            buf.readBytes(dst);
            String name = new String(dst);
            dimlets.put(id, name);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(dimlets.size());
        for (Map.Entry<Integer,String> me : dimlets.entrySet()) {
            buf.writeInt(me.getKey());
            buf.writeInt(me.getValue().length());
            buf.writeBytes(me.getValue().getBytes());
        }
    }

    public PacketCheckDimletConfig() {
    }

    public PacketCheckDimletConfig(Map<Integer, String> dimlets) {
        this.dimlets = new HashMap<Integer, String>(dimlets);
    }

}
