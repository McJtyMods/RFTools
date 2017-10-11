package mcjty.rftools.shapes;

import io.netty.buffer.ByteBuf;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketRequestExtraData implements IMessage {
    private int scanId;

    @Override
    public void fromBytes(ByteBuf buf) {
        scanId = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(scanId);
    }

    public PacketRequestExtraData() {
    }

    public PacketRequestExtraData(int scanId) {
        this.scanId = scanId;
    }

    public static class Handler implements IMessageHandler<PacketRequestExtraData, IMessage> {
        @Override
        public IMessage onMessage(PacketRequestExtraData message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketRequestExtraData message, MessageContext ctx) {
            ScanExtraData extraData = ScanDataManager.getScans().getExtraData(message.scanId);
            RFToolsMessages.INSTANCE.sendTo(new PacketReturnExtraData(message.scanId, extraData), ctx.getServerHandler().player);
        }
    }


}
