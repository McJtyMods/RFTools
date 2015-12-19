package mcjty.rftools.blocks.storage;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.clientinfo.InfoPacketClient;
import net.minecraft.client.entity.EntityPlayerSP;

public class StorageInfoPacketClient implements InfoPacketClient {

    private int cnt;

    public static int cntReceived = 1;

    public StorageInfoPacketClient() {
    }

    public StorageInfoPacketClient(int cnt) {
        this.cnt = cnt;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        cnt = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(cnt);
    }

    @Override
    public void onMessageClient(EntityPlayerSP player) {
        cntReceived = cnt;
    }
}
