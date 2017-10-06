package mcjty.rftools.shapes;

import io.netty.buffer.ByteBuf;
import mcjty.rftools.RFTools;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.ArrayList;
import java.util.List;

public class PacketReturnExtraData implements IMessage {

    private int scanId;
    private ScanExtraData data;

    @Override
    public void fromBytes(ByteBuf buf) {
        scanId = buf.readInt();
        int size = buf.readInt();
        if (size == -1) {
            data = null;
        } else {
            data = new ScanExtraData();
            for (int i = 0; i < size; i++) {
                long p = buf.readLong();
                data.addBeacon(BlockPos.fromLong(p));
            }
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(scanId);
        if (data == null) {
            buf.writeInt(-1);
        } else {
            List<BlockPos> beacons = data.getBeacons();
            buf.writeInt(beacons.size());
            for (BlockPos pos : beacons) {
                buf.writeLong(pos.toLong());
            }
        }
    }

    public PacketReturnExtraData() {
    }

    public PacketReturnExtraData(int scanId, ScanExtraData extraData) {
        this.scanId = scanId;
        this.data = extraData;
    }

    public static class Handler implements IMessageHandler<PacketReturnExtraData, IMessage> {
        @Override
        public IMessage onMessage(PacketReturnExtraData message, MessageContext ctx) {
            RFTools.proxy.addScheduledTaskClient(() -> handle(message));
            return null;
        }

        private void handle(PacketReturnExtraData message) {
            ScanDataManager.getScansClient().registerExtraDataFromServer(message.scanId, message.data);
        }
    }
}