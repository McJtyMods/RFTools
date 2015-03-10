package com.mcjty.rftools.dimension.network;

import com.mcjty.rftools.items.dimlets.DimletKey;
import com.mcjty.rftools.items.dimlets.DimletType;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

import java.util.HashMap;
import java.util.Map;

public class PacketCheckDimletConfig implements IMessage {
    private Map<Integer, DimletKey> dimlets;

    @Override
    public void fromBytes(ByteBuf buf) {
        int size = buf.readInt();
        dimlets = new HashMap<Integer, DimletKey>();
        for (int i = 0 ; i < size ; i++) {
            int id = buf.readInt();
            byte[] dst = new byte[buf.readInt()];
            buf.readBytes(dst);
            String name = new String(dst);
            int typeOrdinal = buf.readInt();
            dimlets.put(id, new DimletKey(DimletType.values()[typeOrdinal], name));
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(dimlets.size());
        for (Map.Entry<Integer,DimletKey> me : dimlets.entrySet()) {
            buf.writeInt(me.getKey());
            DimletKey key = me.getValue();
            buf.writeInt(key.getName().length());
            buf.writeBytes(key.getName().getBytes());
            buf.writeInt(key.getType().ordinal());
        }
    }

    public Map<Integer, DimletKey> getDimlets() {
        return dimlets;
    }

    public PacketCheckDimletConfig() {
    }

    public PacketCheckDimletConfig(Map<Integer, DimletKey> dimlets) {
        this.dimlets = new HashMap<Integer, DimletKey>(dimlets);
    }

}
