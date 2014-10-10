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
 * This is typically used in combination with PacketRequestIntegerFromServer although you can also use it standalone.
 * You use this by making a subclass of this class. This implements a message that is sent from the server back to the client.
 *
 * @param S is the type of the subclass of this class. i.e. the class you're implementing
 */
public abstract class PacketIntegerFromServer<S extends PacketIntegerFromServer> implements IMessage, IMessageHandler<S, IMessage> {
    private int x;
    private int y;
    private int z;
    private Integer result;
    private String command;

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();

        byte[] dst = new byte[buf.readInt()];
        buf.readBytes(dst);
        command = new String(dst);

        boolean resultPresent = buf.readBoolean();
        if (resultPresent) {
            result = buf.readInt();
        } else {
            result = null;
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);

        buf.writeInt(command.length());
        buf.writeBytes(command.getBytes());

        buf.writeBoolean(result != null);
        if (result != null) {
            buf.writeInt(result);
        }
    }

    public PacketIntegerFromServer() {
    }

    public PacketIntegerFromServer(int x, int y, int z, String command, Integer result) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.command = command;
        this.result = result;
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
        clientCommandHandler.execute(message.command, message.result);
        return null;
    }

}
