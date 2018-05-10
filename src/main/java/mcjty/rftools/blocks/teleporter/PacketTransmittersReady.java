package mcjty.rftools.blocks.teleporter;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.IClientCommandHandler;
import mcjty.lib.network.PacketListFromServer;
import mcjty.lib.varia.Logging;
import mcjty.rftools.RFTools;
import mcjty.lib.typed.Type;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.List;

public class PacketTransmittersReady extends PacketListFromServer<PacketTransmittersReady,TransmitterInfo> {

    public PacketTransmittersReady() {
    }

    public PacketTransmittersReady(BlockPos pos, String command, List<TransmitterInfo> list) {
        super(pos, command, list);
    }

    public static class Handler implements IMessageHandler<PacketTransmittersReady, IMessage> {
        @Override
        public IMessage onMessage(PacketTransmittersReady message, MessageContext ctx) {
            RFTools.proxy.addScheduledTaskClient(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketTransmittersReady message, MessageContext ctx) {
            TileEntity te = RFTools.proxy.getClientWorld().getTileEntity(message.pos);
            if(!(te instanceof IClientCommandHandler)) {
                Logging.log("createInventoryReadyPacket: TileEntity is not a ClientCommandHandler!");
                return;
            }
            IClientCommandHandler clientCommandHandler = (IClientCommandHandler) te;
            if (!clientCommandHandler.execute(message.command, message.list, Type.create(TransmitterInfo.class))) {
                Logging.log("Command " + message.command + " was not handled!");
            }
        }
    }

    @Override
    protected TransmitterInfo createItem(ByteBuf buf) {
        return new TransmitterInfo(buf);
    }

    @Override
    protected void writeItemToBuf(ByteBuf buf, TransmitterInfo item) {
        item.toBytes(buf);
    }
}
