package mcjty.rftools.items.teleportprobe;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.rftools.RFTools;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketTargetsReady implements IMessage {

    private int target;
    private int[] targets;
    private String[] names;

    @Override
    public void fromBytes(ByteBuf buf) {
        target = buf.readInt();
        int size = buf.readInt();
        targets = new int[size];
        names = new String[size];
        for (int i = 0 ; i < size ; i++) {
            targets[i] = buf.readInt();
            names[i] = NetworkTools.readString(buf);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(target);
        buf.writeInt(targets.length);
        for (int i = 0 ; i < targets.length ; i++) {
            buf.writeInt(targets[i]);
            NetworkTools.writeString(buf, names[i]);
        }
    }

    public PacketTargetsReady() {
    }

    public PacketTargetsReady(int target, int[] targets, String[] names) {
        this.target = target;
        this.targets = targets;
        this.names = names;
    }


    public static class Handler implements IMessageHandler<PacketTargetsReady, IMessage> {
        @Override
        public IMessage onMessage(PacketTargetsReady message, MessageContext ctx) {
            RFTools.proxy.addScheduledTaskClient(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketTargetsReady message, MessageContext ctx) {
            GuiAdvancedPorter.setInfo(message.target, message.targets, message.names);
        }
    }

}
