package mcjty.rftools.items.teleportprobe;

import mcjty.rftools.blocks.teleporter.TeleportDestination;
import mcjty.rftools.blocks.teleporter.TeleportDestinationClientInfo;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class PacketAllReceiversReady {
    private List<TeleportDestinationClientInfo> destinationList;

    public void toBytes(PacketBuffer buf) {
        buf.writeInt(destinationList.size());
        for (TeleportDestination destination : destinationList) {
            destination.toBytes(buf);
        }
    }

    public PacketAllReceiversReady() {
    }

    public PacketAllReceiversReady(PacketBuffer buf) {
        int size = buf.readInt();
        destinationList = new ArrayList<>(size);
        for (int i = 0 ; i < size ; i++) {
            destinationList.add(new TeleportDestinationClientInfo(buf));
        }
    }

    public PacketAllReceiversReady(List<TeleportDestinationClientInfo> destinationList) {
        this.destinationList = new ArrayList<>();
        this.destinationList.addAll(destinationList);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            GuiTeleportProbe.setReceivers(destinationList);
        });
        ctx.setPacketHandled(true);
    }
}
