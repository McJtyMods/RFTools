package mcjty.rftools.network;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class PacketReturnDestinationInfo implements IMessage {
    private int id;
    private String name;

    @Override
    public void fromBytes(ByteBuf buf) {
        id = buf.readInt();
        name = NetworkTools.readString(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(id);
        NetworkTools.writeString(buf, name);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public PacketReturnDestinationInfo() {
    }

    public PacketReturnDestinationInfo(int id, String name) {
        this.id = id;
        this.name = name;
    }
}