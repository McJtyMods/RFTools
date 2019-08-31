package mcjty.rftools.items.teleportprobe;

import mcjty.lib.network.NetworkTools;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketTargetsReady {

    private int target;
    private int[] targets;
    private String[] names;

    public void toBytes(PacketBuffer buf) {
        buf.writeInt(target);
        buf.writeInt(targets.length);
        for (int i = 0 ; i < targets.length ; i++) {
            buf.writeInt(targets[i]);
            NetworkTools.writeString(buf, names[i]);
        }
    }

    public PacketTargetsReady() {
    }

    public PacketTargetsReady(PacketBuffer buf) {
        target = buf.readInt();
        int size = buf.readInt();
        targets = new int[size];
        names = new String[size];
        for (int i = 0 ; i < size ; i++) {
            targets[i] = buf.readInt();
            names[i] = NetworkTools.readString(buf);
        }
    }

    public PacketTargetsReady(int target, int[] targets, String[] names) {
        this.target = target;
        this.targets = targets;
        this.names = names;
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            GuiAdvancedPorter.setInfo(target, targets, names);
        });
        ctx.setPacketHandled(true);
    }
}
