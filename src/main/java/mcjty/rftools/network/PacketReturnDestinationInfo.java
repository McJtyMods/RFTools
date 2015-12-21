package mcjty.rftools.network;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

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

    public static class Handler implements IMessageHandler<PacketReturnDestinationInfo, IMessage> {
        @Override
        public IMessage onMessage(PacketReturnDestinationInfo message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> ReturnDestinationInfoHelper.setDestinationInfo(message));
            return null;
        }

    }
}