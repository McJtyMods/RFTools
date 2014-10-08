package com.mcjty.rftools.network;

import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import net.minecraft.tileentity.TileEntity;

import java.util.List;

public abstract class PacketRequestListFromClient<T extends ByteBufConverter, S extends PacketRequestListFromClient, C extends PacketListFromClient<C,T>> extends AbstractServerCommand implements IMessageHandler<S, C> {
    public PacketRequestListFromClient() {
    }

    public PacketRequestListFromClient(int x, int y, int z, String command, Argument... arguments) {
        super(x, y, z, command, arguments);
    }

    @Override
    public C onMessage(S message, MessageContext ctx) {
        TileEntity te = ctx.getServerHandler().playerEntity.worldObj.getTileEntity(message.x, message.y, message.z);
        if(!(te instanceof CommandHandler)) {
            // @Todo better logging
            System.out.println("createStartScanPacket: TileEntity is not a CommandHandler!");
            return null;
        }
        CommandHandler commandHandler = (CommandHandler) te;
        List<T> list = (List<T>) commandHandler.executeWithResult(message.command, message.args);
        if (list == null) {
            System.out.println("Command "+message.command+" was not handled!");
            return null;
        }
        return createMessageToClient(message.x, message.y, message.z, list);
    }

    protected abstract C createMessageToClient(int x, int y, int z, List<T> result);
}
