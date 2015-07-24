package mcjty.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import mcjty.base.ModBaseRef;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 * Change the GUI style.
 */
public class PacketSetGuiStyle implements IMessage, IMessageHandler<PacketSetGuiStyle, IMessage> {

    private String style;

    @Override
    public void fromBytes(ByteBuf buf) {
        style = NetworkTools.readString(buf);

    }

    @Override
    public void toBytes(ByteBuf buf) {
        NetworkTools.writeString(buf, style);
    }

    public PacketSetGuiStyle() {
    }

    public PacketSetGuiStyle(String style) {
        this.style = style;
    }

    @Override
    public IMessage onMessage(PacketSetGuiStyle message, MessageContext ctx) {
        EntityPlayerMP playerEntity = ctx.getServerHandler().playerEntity;
        ModBaseRef.INSTANCE.setGuiStyle(playerEntity, message.style);
        return null;
    }

}
