package com.mcjty.rftools.blocks.screens.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

import java.util.HashMap;
import java.util.Map;

public class PacketReturnScreenData implements IMessage {
    int x;
    int y;
    int z;
    Map<Integer, String> screenData;

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        int size = buf.readInt();
        screenData = new HashMap<Integer, String>(size);
        for (int i = 0 ; i < size ; i++) {
            int key = buf.readInt();
            byte[] dst = new byte[buf.readInt()];
            buf.readBytes(dst);
            screenData.put(key, new String(dst));
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeInt(screenData.size());
        for (Map.Entry<Integer, String> me : screenData.entrySet()) {
            buf.writeInt(me.getKey());
            String c = me.getValue();
            buf.writeInt(c.length());
            buf.writeBytes(c.getBytes());
        }
    }

    public PacketReturnScreenData() {
    }

    public PacketReturnScreenData(int x, int y, int z, Map<Integer, String> screenData) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.screenData = screenData;
    }
}