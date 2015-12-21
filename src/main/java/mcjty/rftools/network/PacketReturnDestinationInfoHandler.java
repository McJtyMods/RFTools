package mcjty.rftools.network;


import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketReturnDestinationInfoHandler implements IMessageHandler<PacketReturnDestinationInfo, IMessage> {
    @Override
    public IMessage onMessage(PacketReturnDestinationInfo message, MessageContext ctx) {
        ReturnDestinationInfoHelper.setDestinationInfo(message);
        return null;
    }

}