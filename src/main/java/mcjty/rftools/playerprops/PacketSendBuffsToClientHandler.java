package mcjty.rftools.playerprops;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketSendBuffsToClientHandler implements IMessageHandler<PacketSendBuffsToClient, IMessage> {
    @Override
    public IMessage onMessage(PacketSendBuffsToClient message, MessageContext ctx) {
        SendBuffsToClientHelper.setBuffs(message);
        return null;
    }

}
