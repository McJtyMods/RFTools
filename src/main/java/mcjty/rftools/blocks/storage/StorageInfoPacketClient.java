package mcjty.rftools.blocks.storage;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.network.clientinfo.InfoPacketClient;
import net.minecraft.client.entity.EntityPlayerSP;

public class StorageInfoPacketClient implements InfoPacketClient {

    private int cnt;
    private String nameModule;

    public static int cntReceived = 1;
    public static String nameModuleReceived = "";

    public StorageInfoPacketClient() {
    }

    public StorageInfoPacketClient(int cnt, String nameModule) {
        this.cnt = cnt;
        this.nameModule = nameModule;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        cnt = buf.readInt();
        nameModule = NetworkTools.readString(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(cnt);
        NetworkTools.writeString(buf, nameModule);
    }

    @Override
    public void onMessageClient(EntityPlayerSP player) {
        cntReceived = cnt;
        nameModuleReceived = nameModule;
    }
}
