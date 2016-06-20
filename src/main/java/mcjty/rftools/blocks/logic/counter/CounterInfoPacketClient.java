package mcjty.rftools.blocks.logic.counter;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.clientinfo.InfoPacketClient;
import net.minecraft.client.entity.EntityPlayerSP;

public class CounterInfoPacketClient implements InfoPacketClient {

    private int cnt;

    public static int cntReceived = 0;

    public CounterInfoPacketClient() {
    }

    public CounterInfoPacketClient(int cnt) {
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
