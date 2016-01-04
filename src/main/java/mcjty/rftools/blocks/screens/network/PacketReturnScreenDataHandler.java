package mcjty.rftools.blocks.screens.network;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketReturnScreenDataHandler implements IMessageHandler<PacketReturnScreenData, IMessage> {
    @Override
    public IMessage onMessage(PacketReturnScreenData message, MessageContext ctx) {
        PacketGetScreenDataHelper.setScreenData(message);
        return null;
    }

}