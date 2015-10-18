package mcjty.rftools.blocks.security;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;

public class PacketSecurityNameReady implements IMessage, IMessageHandler<PacketSecurityNameReady, IMessage> {

    private String name;


    @Override
    public void fromBytes(ByteBuf buf) {
        name = NetworkTools.readString(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        NetworkTools.writeString(buf, name);
    }

    public PacketSecurityNameReady() {
    }

    public PacketSecurityNameReady(SecurityChannels.SecurityChannel channel) {
        this.name = channel.getName();
    }

    @Override
    public IMessage onMessage(PacketSecurityNameReady message, MessageContext ctx) {
        SecurityCardItem.channelNameFromServer = message.name;
        return null;
    }
}
