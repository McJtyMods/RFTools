package mcjty.rftools.items.creativeonly;

import mcjty.lib.network.NetworkTools;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class PacketDelightingInfoReady {
    private List<String> blockClasses;
    private List<String> teClasses;
    private Map<String,DelightingInfoHelper.NBTDescription> nbtData;

    public void toBytes(PacketBuffer buf) {
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
    }

    public PacketDelightingInfoReady() {
    }

    public PacketDelightingInfoReady(PacketBuffer buf) {
        int size = buf.readInt();
        blockClasses = new ArrayList<>(size);
        for (int i = 0 ; i < size ; i++) {
            blockClasses.add(NetworkTools.readString(buf));
        }

        size = buf.readInt();
        teClasses = new ArrayList<>(size);
        for (int i = 0 ; i < size ; i++) {
            teClasses.add(NetworkTools.readString(buf));
        }

        size = buf.readInt();
        nbtData = new HashMap<>(size);
        for (int i = 0 ; i < size ; i++) {
            String key = NetworkTools.readString(buf);
            String type = NetworkTools.readString(buf);
            String value = NetworkTools.readString(buf);

            nbtData.put(key, new DelightingInfoHelper.NBTDescription(type, value));
        }
    }

    public PacketDelightingInfoReady(List<String> blockClasses, List<String> teClasses, Map<String,DelightingInfoHelper.NBTDescription> nbtData) {
        this.blockClasses = new ArrayList<>(blockClasses);
        this.teClasses = new ArrayList<>(teClasses);
        this.nbtData = new HashMap<>(nbtData);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            GuiDevelopersDelight.setServerBlockClasses(blockClasses);
            GuiDevelopersDelight.setServerTEClasses(teClasses);
            GuiDevelopersDelight.setServerNBTData(nbtData);
        });
        ctx.setPacketHandled(true);
    }
}
