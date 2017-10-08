package mcjty.rftools.shapes;

import io.netty.buffer.ByteBuf;
import mcjty.rftools.RFTools;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketReturnScanDirty implements IMessage {
    private int scanId;
    private int dirtyCounter;

    @Override
    public void fromBytes(ByteBuf buf) {
        scanId = buf.readInt();
        dirtyCounter = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(scanId);
        buf.writeInt(dirtyCounter);
    }

    public PacketReturnScanDirty() {
    }

    public PacketReturnScanDirty(int scanId, int dirtyCounter) {
        this.scanId = scanId;
        this.dirtyCounter = dirtyCounter;
    }

    public static class Handler implements IMessageHandler<PacketReturnScanDirty, IMessage> {
        @Override
        public IMessage onMessage(PacketReturnScanDirty message, MessageContext ctx) {
            RFTools.proxy.addScheduledTaskClient(() -> handle(message));
            return null;
        }

        private void handle(PacketReturnScanDirty message) {
            ScanDataManagerClient.getScansClient().getOrCreateScan(message.scanId).setDirtyCounter(message.dirtyCounter);
        }
    }
}