package mcjty.rftools.playerprops;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

public class PacketSendPreferencesToClient implements IMessage {
    private int buffX;
    private int buffY;

    @Override
    public void fromBytes(ByteBuf buf) {
        buffX = buf.readInt();
        buffY = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(buffX);
        buf.writeInt(buffY);
    }

    public PacketSendPreferencesToClient() {
    }

    public PacketSendPreferencesToClient(int buffX, int buffY) {
        this.buffX = buffX;
        this.buffY = buffY;
    }

    public int getBuffX() {
        return buffX;
    }

    public int getBuffY() {
        return buffY;
    }
}
