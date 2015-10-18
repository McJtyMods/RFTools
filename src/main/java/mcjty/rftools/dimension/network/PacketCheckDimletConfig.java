package mcjty.rftools.dimension.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.varia.Logging;
import mcjty.rftools.items.dimlets.DimletKey;
import mcjty.rftools.items.dimlets.DimletType;

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
            String name = NetworkTools.readString(buf);
            int typeOrdinal = buf.readInt();
            try {
                dimlets.put(id, new DimletKey(DimletType.values()[typeOrdinal], name));
            } catch (Exception e) {
                Logging.logError("INTERNAL ERROR: name=" + name + ", i=" + i + ", size=" + size + "!");
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(dimlets.size());
        for (Map.Entry<Integer,DimletKey> me : dimlets.entrySet()) {
            DimletKey key = me.getValue();
            buf.writeInt(me.getKey());
            NetworkTools.writeString(buf, key.getName());
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
