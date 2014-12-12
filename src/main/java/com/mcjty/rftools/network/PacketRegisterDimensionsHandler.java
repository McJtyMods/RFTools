package com.mcjty.rftools.network;

import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.dimension.world.GenericWorldProvider;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.common.DimensionManager;

public class PacketRegisterDimensionsHandler implements IMessageHandler<PacketRegisterDimensions, IMessage> {
    @Override
    public IMessage onMessage(PacketRegisterDimensions message, MessageContext ctx) {
        if (DimensionManager.isDimensionRegistered(message.id)) {
            RFTools.log("Client side, already registered dimension: " + message.id);
        } else {
            RFTools.log("Client side, register dimension: " + message.id);
            DimensionManager.registerProviderType(message.id, GenericWorldProvider.class, false);
            DimensionManager.registerDimension(message.id, message.id);
        }
        return null;
    }

}
