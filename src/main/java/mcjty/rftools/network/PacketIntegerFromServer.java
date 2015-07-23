package mcjty.rftools.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import mcjty.network.NetworkTools;
import mcjty.varia.Logging;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;

/**
 * This packet is used (typically by PacketRequestIntegerFromServer) to send back an integer to the client.
 */
public class PacketIntegerFromServer implements IMessage, IMessageHandler<PacketIntegerFromServer, IMessage> {
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

        command = NetworkTools.readString(buf);

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

        NetworkTools.writeString(buf, command);

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
    public IMessage onMessage(PacketIntegerFromServer message, MessageContext ctx) {
        TileEntity te = Minecraft.getMinecraft().theWorld.getTileEntity(message.x, message.y, message.z);
        if(!(te instanceof ClientCommandHandler)) {
            Logging.log("createInventoryReadyPacket: TileEntity is not a ClientCommandHandler!");
            return null;
        }
        ClientCommandHandler clientCommandHandler = (ClientCommandHandler) te;
        if (!clientCommandHandler.execute(message.command, message.result)) {
            Logging.log("Command " + message.command + " was not handled!");
        }
        return null;
    }

}
