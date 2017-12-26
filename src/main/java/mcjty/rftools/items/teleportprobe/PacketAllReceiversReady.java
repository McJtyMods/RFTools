package mcjty.rftools.items.teleportprobe;

import io.netty.buffer.ByteBuf;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.teleporter.TeleportDestination;
import mcjty.rftools.blocks.teleporter.TeleportDestinationClientInfo;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.ArrayList;
import java.util.List;

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

    public PacketAllReceiversReady(List<TeleportDestinationClientInfo> destinationList) {
        this.destinationList = new ArrayList<>();
        this.destinationList.addAll(destinationList);
    }

    public static class Handler implements IMessageHandler<PacketAllReceiversReady, IMessage> {
        @Override
        public IMessage onMessage(PacketAllReceiversReady message, MessageContext ctx) {
            RFTools.proxy.addScheduledTaskClient(() -> GuiTeleportProbe.setReceivers(message.destinationList));
            return null;
        }
    }
}
