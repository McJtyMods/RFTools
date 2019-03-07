package mcjty.rftools.items.teleportprobe;

import io.netty.buffer.ByteBuf;
import mcjty.lib.thirteen.Context;
import mcjty.rftools.blocks.teleporter.TeleportDestination;
import mcjty.rftools.blocks.teleporter.TeleportDestinationClientInfo;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class PacketAllReceiversReady implements IMessage {
    private List<TeleportDestinationClientInfo> destinationList;

    @Override
    public void fromBytes(ByteBuf buf) {
        int size = buf.readInt();
        destinationList = new ArrayList<>(size);
        for (int i = 0 ; i < size ; i++) {
            destinationList.add(new TeleportDestinationClientInfo(buf));
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(destinationList.size());
        for (TeleportDestination destination : destinationList) {
            destination.toBytes(buf);
        }
    }

    public PacketAllReceiversReady() {
    }

    public PacketAllReceiversReady(ByteBuf buf) {
        fromBytes(buf);
    }

    public PacketAllReceiversReady(List<TeleportDestinationClientInfo> destinationList) {
        this.destinationList = new ArrayList<>();
        this.destinationList.addAll(destinationList);
    }

    public void handle(Supplier<Context> supplier) {
        Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            GuiTeleportProbe.setReceivers(destinationList);
        });
        ctx.setPacketHandled(true);
    }
}
