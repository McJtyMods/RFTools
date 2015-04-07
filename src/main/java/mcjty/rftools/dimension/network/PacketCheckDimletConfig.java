package mcjty.rftools.dimension.network;

import mcjty.rftools.RFTools;
import mcjty.rftools.items.dimlets.DimletKey;
import mcjty.rftools.items.dimlets.DimletType;
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
            int s = buf.readInt();
            String name;
            if (s == -1) {
                name = null;
            } else if (s == 0) {
                name = "";
            } else {
                byte[] dst = new byte[s];
                buf.readBytes(dst);
                name = new String(dst);
            }
            int typeOrdinal = buf.readInt();
            try {
                dimlets.put(id, new DimletKey(DimletType.values()[typeOrdinal], name));
            } catch (Exception e) {
                RFTools.logError("INTERNAL ERROR: name=" + name + ", i=" + i + ", size=" + size + "!");
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(dimlets.size());
        for (Map.Entry<Integer,DimletKey> me : dimlets.entrySet()) {
            buf.writeInt(me.getKey());
            DimletKey key = me.getValue();
            String name = key.getName();
            if (name == null) {
                buf.writeInt(-1);
            } else if (name.isEmpty()) {
                buf.writeInt(0);
            } else {
                buf.writeInt(name.length());
                buf.writeBytes(name.getBytes());
            }
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
