package mcjty.rftools.blocks.builder;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.rftools.RFTools;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketPositionToClient implements IMessage {
    private BlockPos tePos;
    private BlockPos scanPos;

    @Override
    public void fromBytes(ByteBuf buf) {
        tePos = NetworkTools.readPos(buf);
        scanPos = NetworkTools.readPos(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        NetworkTools.writePos(buf, tePos);
        NetworkTools.writePos(buf, scanPos);
    }

    public PacketPositionToClient() {
    }

    public PacketPositionToClient(BlockPos tePos, BlockPos scanPos) {
        this.tePos = tePos;
        this.scanPos = scanPos;
    }

    public static class Handler implements IMessageHandler<PacketPositionToClient, IMessage> {
        @Override
        public IMessage onMessage(PacketPositionToClient message, MessageContext ctx) {
            RFTools.proxy.addScheduledTaskClient(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketPositionToClient message, MessageContext ctx) {
            TileEntity te = RFTools.proxy.getClientWorld().getTileEntity(message.tePos);
            if (te instanceof BuilderTileEntity) {
                BuilderTileEntity.setScanLocationClient(message.tePos, message.scanPos);
            }
        }
    }

}
