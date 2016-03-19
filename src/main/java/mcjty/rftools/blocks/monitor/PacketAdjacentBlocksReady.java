package mcjty.rftools.blocks.monitor;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.ClientCommandHandler;
import mcjty.lib.network.PacketListFromServer;
import mcjty.lib.varia.Logging;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.List;
import java.util.stream.Collectors;

public class PacketAdjacentBlocksReady extends PacketListFromServer<PacketAdjacentBlocksReady,BlockPosNet> {

    public PacketAdjacentBlocksReady() {
    }

    public PacketAdjacentBlocksReady(BlockPos pos, String command, List<BlockPos> list) {
        super(pos, command, list.stream().map(BlockPosNet::new).collect(Collectors.toList()));
    }

    @Override
    protected BlockPosNet createItem(ByteBuf buf) {
        return new BlockPosNet(buf);
    }

    public static class Handler implements IMessageHandler<PacketAdjacentBlocksReady, IMessage> {
        @Override
        public IMessage onMessage(PacketAdjacentBlocksReady message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketAdjacentBlocksReady message, MessageContext ctx) {
            TileEntity te = Minecraft.getMinecraft().theWorld.getTileEntity(message.pos);
            if(!(te instanceof ClientCommandHandler)) {
                Logging.log("createInventoryReadyPacket: TileEntity is not a ClientCommandHandler!");
                return;
            }
            ClientCommandHandler clientCommandHandler = (ClientCommandHandler) te;
            if (!clientCommandHandler.execute(message.command, message.list)) {
                Logging.log("Command " + message.command + " was not handled!");
            }
        }
    }
}
