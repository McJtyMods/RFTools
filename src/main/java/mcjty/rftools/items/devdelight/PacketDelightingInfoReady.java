package mcjty.rftools.items.devdelight;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import mcjty.rftools.network.NetworkTools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PacketDelightingInfoReady implements IMessage, IMessageHandler<PacketDelightingInfoReady, IMessage> {
    private List<String> blockClasses;
    private List<String> teClasses;
    private Map<String,DelightingInfoHelper.NBTDescription> nbtData;
    private int metadata;

    @Override
    public void fromBytes(ByteBuf buf) {
        int size = buf.readInt();
        blockClasses = new ArrayList<String>(size);
        for (int i = 0 ; i < size ; i++) {
            blockClasses.add(NetworkTools.readString(buf));
        }

        size = buf.readInt();
        teClasses = new ArrayList<String>(size);
        for (int i = 0 ; i < size ; i++) {
            teClasses.add(NetworkTools.readString(buf));
        }

        size = buf.readInt();
        nbtData = new HashMap<String, DelightingInfoHelper.NBTDescription>(size);
        for (int i = 0 ; i < size ; i++) {
            String key = NetworkTools.readString(buf);
            String type = NetworkTools.readString(buf);
            String value = NetworkTools.readString(buf);

            nbtData.put(key, new DelightingInfoHelper.NBTDescription(type, value));
        }

        metadata = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(blockClasses.size());
        for (String c : blockClasses) {
            NetworkTools.writeString(buf, c);
        }
        buf.writeInt(teClasses.size());
        for (String c : teClasses) {
            NetworkTools.writeString(buf, c);
        }
        buf.writeInt(nbtData.size());
        for (Map.Entry<String,DelightingInfoHelper.NBTDescription> me : nbtData.entrySet()) {
            String key = me.getKey();
            DelightingInfoHelper.NBTDescription value = me.getValue();
            NetworkTools.writeString(buf, key);
            NetworkTools.writeString(buf, value.getType());
            NetworkTools.writeString(buf, value.getValue());
        }
        buf.writeInt(metadata);
    }

    public PacketDelightingInfoReady() {
    }

    public PacketDelightingInfoReady(List<String> blockClasses, List<String> teClasses, Map<String,DelightingInfoHelper.NBTDescription> nbtData, int metadata) {
        this.blockClasses = new ArrayList<String>(blockClasses);
        this.teClasses = new ArrayList<String>(teClasses);
        this.nbtData = new HashMap<String, DelightingInfoHelper.NBTDescription>(nbtData);
        this.metadata = metadata;
    }

    @Override
    public IMessage onMessage(PacketDelightingInfoReady message, MessageContext ctx) {
        GuiDevelopersDelight.setServerBlockClasses(message.blockClasses);
        GuiDevelopersDelight.setServerTEClasses(message.teClasses);
        GuiDevelopersDelight.setServerNBTData(message.nbtData);
        GuiDevelopersDelight.setMetadata(message.metadata);
        return null;
    }

}
