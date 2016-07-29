package mcjty.rftools.blocks.endergen;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.rftools.RFTools;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketEndergenicFlash implements IMessage {
    private BlockPos pos;
    private int goodCounter;
    private int badCounter;

    @Override
    public void fromBytes(ByteBuf buf) {
        pos = NetworkTools.readPos(buf);
        goodCounter = buf.readInt();
        badCounter = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        NetworkTools.writePos(buf, pos);
        buf.writeInt(goodCounter);
        buf.writeInt(badCounter);
    }

    public PacketEndergenicFlash() {
    }

    public BlockPos getPos() {
        return pos;
    }

    public PacketEndergenicFlash(BlockPos pos, int goodCounter, int badCounter) {
        this.pos = pos;
        this.goodCounter = goodCounter;
        this.badCounter = badCounter;
    }

    public static class Handler implements IMessageHandler<PacketEndergenicFlash, IMessage> {
        @Override
        public IMessage onMessage(PacketEndergenicFlash message, MessageContext ctx) {
            RFTools.proxy.addScheduledTaskClient(() -> handle(message));
            return null;
        }

        private static void handle(PacketEndergenicFlash message) {
            TileEntity te = RFTools.proxy.getClientWorld().getTileEntity(message.getPos());
            if (te instanceof EndergenicTileEntity) {
                EndergenicTileEntity tileEntity = (EndergenicTileEntity) te;
                tileEntity.syncCountersFromServer(message.goodCounter, message.badCounter);
            }
        }
    }
}