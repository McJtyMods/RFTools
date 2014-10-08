package com.mcjty.rftools.network;

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
    private int x;
    private int y;
    private int z;
    private List<T> list;
    private String command;

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();

        byte[] dst = new byte[buf.readInt()];
        buf.readBytes(dst);
        command = new String(dst);

        int size = buf.readInt();
        list = new ArrayList<T>();
        for (int i = 0 ; i < size ; i++) {
            T item = createItem(buf);
            list.add(item);
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

        buf.writeInt(list.size());
        for (T item : list) {
            item.toBytes(buf);
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
            // @Todo better logging
            System.out.println("createInventoryReadyPacket: TileEntity is not a ClientCommandHandler!");
            return null;
        }
        ClientCommandHandler clientCommandHandler = (ClientCommandHandler) te;
        clientCommandHandler.execute(message.command, message.list);
        return null;
    }

}
