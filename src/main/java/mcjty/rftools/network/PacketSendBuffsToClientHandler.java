package mcjty.rftools.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class PacketSendBuffsToClientHandler implements IMessageHandler<PacketSendBuffsToClient, IMessage> {
    @Override
    public IMessage onMessage(PacketSendBuffsToClient message, MessageContext ctx) {
        SendBuffsToClientHelper.setBuffs(message);
        return null;
    }

}
