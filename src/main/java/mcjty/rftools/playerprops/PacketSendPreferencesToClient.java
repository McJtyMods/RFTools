package mcjty.rftools.playerprops;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

public class PacketSendPreferencesToClient implements IMessage {
    private int buffX;
    private int buffY;
    private GuiStyle style;

    @Override
    public void fromBytes(ByteBuf buf) {
        buffX = buf.readInt();
        buffY = buf.readInt();
        style = GuiStyle.values()[buf.readInt()];
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(buffX);
        buf.writeInt(buffY);
        buf.writeInt(style.ordinal());
    }

    public PacketSendPreferencesToClient() {
    }

    public PacketSendPreferencesToClient(int buffX, int buffY, GuiStyle style) {
        this.buffX = buffX;
        this.buffY = buffY;
        this.style = style;
    }

    public int getBuffX() {
        return buffX;
    }

    public int getBuffY() {
        return buffY;
    }

    public GuiStyle getStyle() {
        return style;
    }
}
