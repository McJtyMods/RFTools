package mcjty.rftools.blocks.monitor;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.IClientCommandHandler;
import mcjty.lib.network.NetworkTools;
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

public class PacketAdjacentBlocksReady extends PacketListFromServer<PacketAdjacentBlocksReady,BlockPos> {

    public PacketAdjacentBlocksReady() {
    }

    public PacketAdjacentBlocksReady(BlockPos pos, String command, List<BlockPos> list) {
        super(pos, command, list);
    }

    @Override
    protected BlockPos createItem(ByteBuf buf) {
        return NetworkTools.readPos(buf);
    }

    @Override
    protected void writeItemToBuf(ByteBuf buf, BlockPos item) {
        NetworkTools.writePos(buf, item);
    }

    public static class Handler implements IMessageHandler<PacketAdjacentBlocksReady, IMessage> {
        @Override
        public IMessage onMessage(PacketAdjacentBlocksReady message, MessageContext ctx) {
            RFTools.proxy.addScheduledTaskClient(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketAdjacentBlocksReady message, MessageContext ctx) {
            TileEntity te = RFTools.proxy.getClientWorld().getTileEntity(message.pos);
            if(!(te instanceof IClientCommandHandler)) {
                Logging.log("createInventoryReadyPacket: TileEntity is not a ClientCommandHandler!");
                return;
            }
            IClientCommandHandler clientCommandHandler = (IClientCommandHandler) te;
            if (!clientCommandHandler.execute(message.command, message.list, Type.create(BlockPos.class))) {
                Logging.log("Command " + message.command + " was not handled!");
            }
        }
    }
}
