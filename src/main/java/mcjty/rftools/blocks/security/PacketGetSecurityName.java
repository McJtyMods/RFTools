package mcjty.rftools.blocks.security;

import io.netty.buffer.ByteBuf;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketGetSecurityName implements IMessage {

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

    public static class Handler implements IMessageHandler<PacketGetSecurityName, IMessage> {
        @Override
        public IMessage onMessage(PacketGetSecurityName message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketGetSecurityName message, MessageContext ctx) {
            EntityPlayer player = ctx.getServerHandler().playerEntity;
            SecurityChannels channels = SecurityChannels.getChannels(player.getEntityWorld());
            SecurityChannels.SecurityChannel channel = channels.getChannel(message.id);
            if (channel == null) {
                return;
            }
            RFToolsMessages.INSTANCE.sendTo(new PacketSecurityNameReady(channel), ctx.getServerHandler().playerEntity);
        }
    }
}
