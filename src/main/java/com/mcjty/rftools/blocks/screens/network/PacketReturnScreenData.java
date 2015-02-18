package com.mcjty.rftools.blocks.screens.network;

import com.mcjty.rftools.blocks.screens.modules.ScreenDataType;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

import java.util.HashMap;
import java.util.Map;

public class PacketReturnScreenData implements IMessage {
    int x;
    int y;
    int z;
    Map<Integer, Object[]> screenData;

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        int size = buf.readInt();
        screenData = new HashMap<Integer, Object[]>(size);
        for (int i = 0 ; i < size ; i++) {
            int key = buf.readInt();
            int arsize = buf.readInt();
            Object[] ar = new Object[arsize];
            for (int j = 0 ; j < arsize ; j++) {
                byte type = buf.readByte();
                ScreenDataType dataType = ScreenDataType.values()[type];
                ar[j] = dataType.readObject(buf);
            }
            screenData.put(key, ar);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeInt(screenData.size());
        for (Map.Entry<Integer, Object[]> me : screenData.entrySet()) {
            buf.writeInt(me.getKey());
            Object[] c = me.getValue();
            buf.writeInt(c.length);
            for (Object o : c) {
                ScreenDataType.writeObject(buf, o);
            }
        }
    }

    public PacketReturnScreenData() {
    }

    public PacketReturnScreenData(int x, int y, int z, Map<Integer, Object[]> screenData) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.screenData = screenData;
    }
}