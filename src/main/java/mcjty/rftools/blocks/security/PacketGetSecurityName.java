package mcjty.rftools.blocks.security;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;

public class PacketGetSecurityName implements IMessage, IMessageHandler<PacketGetSecurityName, PacketSecurityNameReady> {

    private int id;

    @Override
    public void fromBytes(ByteBuf buf) {
        id = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(id);
    }

    public PacketGetSecurityName() {
    }

    public PacketGetSecurityName(int id) {
        this.id = id;
    }

    @Override
    public PacketSecurityNameReady onMessage(PacketGetSecurityName message, MessageContext ctx) {
        EntityPlayer player = ctx.getServerHandler().playerEntity;
        SecurityChannels channels = SecurityChannels.getChannels(player.worldObj);
        SecurityChannels.SecurityChannel channel = channels.getChannel(message.id);
        if (channel == null) {
            return null;
        }
        return new PacketSecurityNameReady(channel);
    }

}
