package com.mcjty.rftools.network;

import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import net.minecraft.tileentity.TileEntity;

import java.util.List;

/**
 * Make a subclass of this class to implement a command that can be sent from the client (typically in a GUI)
 * and will perform some command on the server-side tile entity. The result of that command (an integer) will
 * be sent back to the client through the 'PacketIntegerFromServer' class. So typically
 * you would also make a subclass of PacketIntegerFromServer.
 *
 * @param S is the type of the subclass of this class. i.e. the class you're implementing
 * @param C is the type of the subclass of PacketIntegerFromServer. i.e. the class sent back from the server.
 */
public abstract class PacketRequestIntegerFromServer<S extends PacketRequestIntegerFromServer, C extends PacketIntegerFromServer<C>> extends AbstractServerCommand implements IMessageHandler<S, C> {
    public PacketRequestIntegerFromServer() {
    }

    public PacketRequestIntegerFromServer(int x, int y, int z, String command, Argument... arguments) {
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
        Integer result = commandHandler.executeWithResultInteger(message.command, message.args);
        if (result == null) {
            System.out.println("Command "+message.command+" was not handled!");
            return null;
        }
        return createMessageToClient(message.x, message.y, message.z, result);
    }

    protected abstract C createMessageToClient(int x, int y, int z, Integer result);
}
