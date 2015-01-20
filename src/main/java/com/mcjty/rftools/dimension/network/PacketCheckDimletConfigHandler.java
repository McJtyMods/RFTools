package com.mcjty.rftools.dimension.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class PacketCheckDimletConfigHandler implements IMessageHandler<PacketCheckDimletConfig, IMessage> {
    @Override
    public IMessage onMessage(PacketCheckDimletConfig message, MessageContext ctx) {
        CheckDimletConfigHelper.checkDimletsFromServer(message);
        return null;
    }

}
