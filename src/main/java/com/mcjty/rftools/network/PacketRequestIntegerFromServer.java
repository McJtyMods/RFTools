package com.mcjty.rftools.network;

import com.mcjty.rftools.RFTools;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.tileentity.TileEntity;

/**
 * This is a packet that can be used to send a command from the client side (typically the GUI) to
 * a tile entity on the server side that implements CommandHandler. This will call 'executeWithResultInteger()' on
 * that command handler. A PacketIntegerFromServer will be sent back from the client.
 */
public class PacketRequestIntegerFromServer extends AbstractServerCommand implements IMessageHandler<PacketRequestIntegerFromServer, PacketIntegerFromServer> {
    private String clientCommand;

    public PacketRequestIntegerFromServer() {
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        super.fromBytes(buf);

        byte[] dst = new byte[buf.readInt()];
        buf.readBytes(dst);
        clientCommand = new String(dst);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        super.toBytes(buf);

        buf.writeInt(clientCommand.length());
        buf.writeBytes(clientCommand.getBytes());
    }

    public PacketRequestIntegerFromServer(int x, int y, int z, String command, String clientCommand, Argument... arguments) {
        super(x, y, z, command, arguments);
        this.clientCommand = clientCommand;
    }

    @Override
    public PacketIntegerFromServer onMessage(PacketRequestIntegerFromServer message, MessageContext ctx) {
        TileEntity te = ctx.getServerHandler().playerEntity.worldObj.getTileEntity(message.x, message.y, message.z);
        if(!(te instanceof CommandHandler)) {
            RFTools.log("createStartScanPacket: TileEntity is not a CommandHandler!");
            return null;
        }
        CommandHandler commandHandler = (CommandHandler) te;
        Integer result = commandHandler.executeWithResultInteger(message.command, message.args);
        if (result == null) {
            RFTools.log("Command "+message.command+" was not handled!");
            return null;
        }
        return new PacketIntegerFromServer(message.x, message.y, message.z, message.clientCommand, result);
    }
}
