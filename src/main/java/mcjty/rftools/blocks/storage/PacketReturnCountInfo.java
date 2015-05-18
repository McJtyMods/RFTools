package mcjty.rftools.blocks.storage;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;
import mcjty.rftools.network.NetworkTools;

public class PacketReturnCountInfo implements IMessage {
    private int cnt;

    @Override
    public void fromBytes(ByteBuf buf) {
        cnt = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(cnt);
    }

    public int getCnt() {
        return cnt;
    }

    public PacketReturnCountInfo() {
    }

    public PacketReturnCountInfo(int cnt) {
        this.cnt = cnt;
    }
}