package mcjty.rftools.blocks.security;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;

public class PacketGetSecurityInfo implements IMessage, IMessageHandler<PacketGetSecurityInfo, PacketSecurityInfoReady> {

    private int id;

    @Override
    public void fromBytes(ByteBuf buf) {
        id = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(id);
    }

    public PacketGetSecurityInfo() {
    }

    public PacketGetSecurityInfo(int id) {
        this.id = id;
    }

    @Override
    public PacketSecurityInfoReady onMessage(PacketGetSecurityInfo message, MessageContext ctx) {
        EntityPlayer player = ctx.getServerHandler().playerEntity;
        SecurityChannels channels = SecurityChannels.getChannels(player.worldObj);
        SecurityChannels.SecurityChannel channel = channels.getChannel(message.id);
        if (channel == null) {
            return null;
        }
        return new PacketSecurityInfoReady(channel);
    }

}
