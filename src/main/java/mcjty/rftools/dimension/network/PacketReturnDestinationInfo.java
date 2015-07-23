package mcjty.rftools.dimension.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;
import mcjty.network.NetworkTools;

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