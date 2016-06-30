package mcjty.rftools.blocks.screens.network;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.network.clientinfo.InfoPacketClient;
import net.minecraft.client.entity.EntityPlayerSP;

public class ScreenInfoPacketClient implements InfoPacketClient {

    private String[] info;

    public static String[] infoReceived = new String[0];

    public ScreenInfoPacketClient() {
    }

    public ScreenInfoPacketClient(String[] info) {
        this.info = info;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int len = buf.readInt();
        info = new String[len];
        for (int i = 0 ; i < len ; i++) {
            info[i] = NetworkTools.readStringUTF8(buf);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(info.length);
        for (String s : info) {
            NetworkTools.writeStringUTF8(buf, s);
        }
    }

    @Override
    public void onMessageClient(EntityPlayerSP player) {
        infoReceived = info;
    }
}
