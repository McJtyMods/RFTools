package com.mcjty.rftools.network;

import com.mcjty.rftools.RFTools;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import net.minecraft.tileentity.TileEntity;

import java.util.List;

/**
 * Make a subclass of this class to implement a command that can be sent from the client (typically in a GUI)
 * and will perform some command on the server-side tile entity. The result of that command (a list of some
 * type of object) will be sent back to the client through the 'PacketListFromServer' class. So typically
 * you would also make a subclass of PacketListFromServer.
 *
 * The items of this list should implement ByteBufConverter.
 *
 * @param T is the type of the items in the list that is requested from the server
 * @param S is the type of the subclass of this class. i.e. the class you're implementing
 * @param C is the type of the subclass of PacketListFromServer. i.e. the class sent back from the server.
 */
public abstract class PacketRequestListFromServer<T extends ByteBufConverter, S extends PacketRequestListFromServer, C extends PacketListFromServer<C,T>> extends AbstractServerCommand implements IMessageHandler<S, C> {
    public PacketRequestListFromServer() {
    }

    public PacketRequestListFromServer(int x, int y, int z, String command, Argument... arguments) {
        super(x, y, z, command, arguments);
    }

    @Override
    public C onMessage(S message, MessageContext ctx) {
        TileEntity te = ctx.getServerHandler().playerEntity.worldObj.getTileEntity(message.x, message.y, message.z);
        if(!(te instanceof CommandHandler)) {
            RFTools.log("createStartScanPacket: TileEntity is not a CommandHandler!");
            return null;
        }
        CommandHandler commandHandler = (CommandHandler) te;
        List<T> list = (List<T>) commandHandler.executeWithResultList(message.command, message.args);
        if (list == null) {
            RFTools.log("Command "+message.command+" was not handled!");
            return null;
        }
        return createMessageToClient(message.x, message.y, message.z, list);
    }

    protected abstract C createMessageToClient(int x, int y, int z, List<T> result);
}
