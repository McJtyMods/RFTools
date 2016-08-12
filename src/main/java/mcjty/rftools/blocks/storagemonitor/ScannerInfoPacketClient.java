package mcjty.rftools.blocks.storagemonitor;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.clientinfo.InfoPacketClient;
import net.minecraft.client.entity.EntityPlayerSP;

public class ScannerInfoPacketClient implements InfoPacketClient {

    private int rf;
    private boolean exportToCurrent;

    public static int rfReceived = 0;
    public static boolean exportToCurrentReceived = false;

    public ScannerInfoPacketClient() {
    }

    public ScannerInfoPacketClient(int rf, boolean exportToCurrent) {
        this.rf = rf;
        this.exportToCurrent = exportToCurrent;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        rf = buf.readInt();
        exportToCurrent = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(rf);
        buf.writeBoolean(exportToCurrent);
    }

    @Override
    public void onMessageClient(EntityPlayerSP player) {
        rfReceived = rf;
        exportToCurrentReceived = exportToCurrent;
    }
}
