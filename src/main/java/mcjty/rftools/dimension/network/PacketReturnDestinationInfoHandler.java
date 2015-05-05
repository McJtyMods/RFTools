package mcjty.rftools.dimension.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class PacketReturnDestinationInfoHandler implements IMessageHandler<PacketReturnDestinationInfo, IMessage> {
    @Override
    public IMessage onMessage(PacketReturnDestinationInfo message, MessageContext ctx) {
        ReturnDestinationInfoHelper.setDestinationInfo(message);
        return null;
    }

}