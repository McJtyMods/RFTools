package mcjty.rftools.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import mcjty.lib.varia.Logging;
import mcjty.rftools.dimension.world.GenericWorldProvider;
import net.minecraftforge.common.DimensionManager;

public class PacketRegisterDimensionsHandler implements IMessageHandler<PacketRegisterDimensions, IMessage> {
    @Override
    public IMessage onMessage(PacketRegisterDimensions message, MessageContext ctx) {
        if (DimensionManager.isDimensionRegistered(message.getId())) {
            Logging.log("Client side, already registered dimension: " + message.getId());
        } else {
            Logging.log("Client side, register dimension: " + message.getId());
            DimensionManager.registerProviderType(message.getId(), GenericWorldProvider.class, false);
            DimensionManager.registerDimension(message.getId(), message.getId());
        }
        return null;
    }

}
