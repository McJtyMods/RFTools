package mcjty.rftools.network;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.ClientCommandHandler;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.network.PacketListToClient;
import mcjty.lib.varia.Logging;
import mcjty.rftools.RFTools;
import mcjty.lib.typed.Type;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.List;

public class PacketHudLogReady extends PacketListToClient<String> {

    public PacketHudLogReady() {
    }

    public PacketHudLogReady(BlockPos pos, String command, List<String> list) {
        super(pos, command, list);
    }

    public static class Handler implements IMessageHandler<PacketHudLogReady, IMessage> {
        @Override
        public IMessage onMessage(PacketHudLogReady message, MessageContext ctx) {
            RFTools.proxy.addScheduledTaskClient(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketHudLogReady message, MessageContext ctx) {
            TileEntity te = RFTools.proxy.getClientWorld().getTileEntity(message.pos);
            if(!(te instanceof ClientCommandHandler)) {
                Logging.log("TileEntity is not a ClientCommandHandler!");
                return;
            }
            ClientCommandHandler clientCommandHandler = (ClientCommandHandler) te;
            if (!clientCommandHandler.execute(message.command, message.list, Type.STRING)) {
                Logging.log("Command " + message.command + " was not handled!");
            }
        }
    }

    @Override
    protected String createItem(ByteBuf buf) {
        return NetworkTools.readStringUTF8(buf);
    }

    @Override
    protected void writeItemToBuf(ByteBuf buf, String s) {
        NetworkTools.writeStringUTF8(buf, s);
    }
}
