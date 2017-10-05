package mcjty.rftools.shapes;

import io.netty.buffer.ByteBuf;
import mcjty.rftools.RFTools;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketRequestScanDirty implements IMessage {
    private int scanId;

    @Override
    public void fromBytes(ByteBuf buf) {
        scanId = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(scanId);
    }

    public PacketRequestScanDirty() {
    }

    public PacketRequestScanDirty(int scanId) {
        this.scanId = scanId;
    }

    public static class Handler implements IMessageHandler<PacketRequestScanDirty, IMessage> {
        @Override
        public IMessage onMessage(PacketRequestScanDirty message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketRequestScanDirty message, MessageContext ctx) {
            int counter = ScanDataManager.getScans().loadScan(message.scanId).getDirtyCounter();
            RFToolsMessages.INSTANCE.sendTo(new PacketReturnScanDirty(message.scanId, counter), ctx.getServerHandler().player);
        }
    }


}
