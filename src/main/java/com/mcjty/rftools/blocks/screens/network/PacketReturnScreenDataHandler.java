package com.mcjty.rftools.blocks.screens.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class PacketReturnScreenDataHandler implements IMessageHandler<PacketReturnScreenData, IMessage> {
    @Override
    public IMessage onMessage(PacketReturnScreenData message, MessageContext ctx) {
        PacketGetScreenDataHelper.setScreenData(message);
        return null;
    }

}