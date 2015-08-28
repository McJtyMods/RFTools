package mcjty.rftools.items.teleportprobe;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import mcjty.network.NetworkTools;

public class PacketTargetsReady implements IMessage, IMessageHandler<PacketTargetsReady, IMessage> {

    private int target;
    private String name;
    private int[] targets;
    private String[] names;

    @Override
    public void fromBytes(ByteBuf buf) {
        target = buf.readInt();
        name = NetworkTools.readString(buf);
        int size = buf.readInt();
        for (int i = 0 ; i < size ; i++) {
            targets[i] = buf.readInt();
            names[i] = NetworkTools.readString(buf);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(target);
        NetworkTools.writeString(buf, name);
        buf.writeInt(targets.length);
        for (int i = 0 ; i < targets.length ; i++) {
            buf.writeInt(targets[i]);
            NetworkTools.writeString(buf, names[i]);
        }
    }

    public PacketTargetsReady() {
    }

    public PacketTargetsReady(int target, String name, int[] targets, String[] names) {
        this.target = target;
        this.name = name;
        this.targets = targets;
        this.names = names;
    }

    @Override
    public IMessage onMessage(PacketTargetsReady message, MessageContext ctx) {
        GuiAdvancedPorter.setInfo(message.target, message.name, message.targets, message.names);
        return null;
    }

}
