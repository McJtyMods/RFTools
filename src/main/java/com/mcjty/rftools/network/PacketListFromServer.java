package com.mcjty.rftools.network;

import com.mcjty.rftools.RFTools;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * This is typically used in combination with PacketRequestListFromServer although you can also use it standalone.
 * You use this by making a subclass of this class. This implements a message that is sent from the server back to the client.
 *
 * @param S is the type of the subclass of this class. i.e. the class you're implementing
 * @param T is the type of the items in the list that is requested from the server
 */
public abstract class PacketListFromServer<S extends PacketListFromServer, T extends ByteBufConverter> implements IMessage, IMessageHandler<S, IMessage> {
    int x;
    int y;
    int z;
    List<T> list;
    String command;

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();

        byte[] dst = new byte[buf.readInt()];
        buf.readBytes(dst);
        command = new String(dst);

        int size = buf.readInt();
        if (size != -1) {
            list = new ArrayList<T>(size);
            for (int i = 0 ; i < size ; i++) {
                T item = createItem(buf);
                list.add(item);
            }
        } else {
            list = null;
        }
    }

    protected abstract T createItem(ByteBuf buf);

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);

        buf.writeInt(command.length());
        buf.writeBytes(command.getBytes());

        if (list == null) {
            buf.writeInt(-1);
        } else {
            buf.writeInt(list.size());
            for (T item : list) {
                item.toBytes(buf);
            }
        }
    }

    public PacketListFromServer() {
    }

    public PacketListFromServer(int x, int y, int z, String command, List<T> list) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.command = command;
        this.list = new ArrayList<T>();
        this.list.addAll(list);
    }

    @Override
    public IMessage onMessage(S message, MessageContext ctx) {
        TileEntity te = Minecraft.getMinecraft().theWorld.getTileEntity(message.x, message.y, message.z);
        if(!(te instanceof ClientCommandHandler)) {
            RFTools.log("createInventoryReadyPacket: TileEntity is not a ClientCommandHandler!");
            return null;
        }
        ClientCommandHandler clientCommandHandler = (ClientCommandHandler) te;
        if (!clientCommandHandler.execute(message.command, message.list)) {
            RFTools.log("Command "+message.command+" was not handled!");
        }
        return null;
    }

}
