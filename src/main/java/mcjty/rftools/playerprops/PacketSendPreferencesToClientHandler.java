package mcjty.rftools.playerprops;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class PacketSendPreferencesToClientHandler implements IMessageHandler<PacketSendPreferencesToClient, IMessage> {
    @Override
    public IMessage onMessage(PacketSendPreferencesToClient message, MessageContext ctx) {
        SendPreferencesToClientHelper.setBuffs(message);
        return null;
    }

}
