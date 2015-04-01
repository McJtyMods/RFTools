package mcjty.rftools.dimension.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class PacketReturnEnergyHandler implements IMessageHandler<PacketReturnEnergy, IMessage> {
    @Override
    public IMessage onMessage(PacketReturnEnergy message, MessageContext ctx) {
        ReturnEnergyHelper.setEnergyLevel(message);
        return null;
    }

}