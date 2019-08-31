package mcjty.rftools.shapes;

import mcjty.lib.network.NetworkTools;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public class PacketReturnExtraData {

    private int scanId;
    private ScanExtraData data;

    public void toBytes(PacketBuffer buf) {
        buf.writeInt(scanId);
        if (data == null) {
            buf.writeInt(-1);
        } else {
            List<ScanExtraData.Beacon> beacons = data.getBeacons();
            buf.writeInt(beacons.size());
            for (ScanExtraData.Beacon beacon : beacons) {
                NetworkTools.writePos(buf, beacon.getPos());
                buf.writeByte(beacon.getType().ordinal());
                buf.writeBoolean(beacon.isDoBeacon());
            }
        }
    }

    public PacketReturnExtraData() {
    }

    public PacketReturnExtraData(PacketBuffer buf) {
        scanId = buf.readInt();
        int size = buf.readInt();
        if (size == -1) {
            data = null;
        } else {
            data = new ScanExtraData();
            for (int i = 0; i < size; i++) {
                BlockPos pos = NetworkTools.readPos(buf);
                BeaconType type = BeaconType.VALUES[buf.readByte()];
                boolean doBeacon = buf.readBoolean();
                data.addBeacon(pos, type, doBeacon);
            }
        }
    }

    public PacketReturnExtraData(int scanId, ScanExtraData extraData) {
        this.scanId = scanId;
        this.data = extraData;
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ScanDataManagerClient.getScansClient().registerExtraDataFromServer(scanId, data);
        });
        ctx.setPacketHandled(true);
    }
}