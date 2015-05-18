package mcjty.rftools.blocks.storage;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class PacketReturnCountInfoHandler implements IMessageHandler<PacketReturnCountInfo, IMessage> {
    @Override
    public IMessage onMessage(PacketReturnCountInfo message, MessageContext ctx) {
        ReturnCountInfoHelper.setDestinationInfo(message);
        return null;
    }

}