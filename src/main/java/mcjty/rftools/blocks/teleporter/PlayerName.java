package mcjty.rftools.blocks.teleporter;

import mcjty.rftools.network.ByteBufConverter;
import io.netty.buffer.ByteBuf;

public class PlayerName implements ByteBufConverter {
    private final String name;

    public PlayerName(String name) {
        this.name = name;
    }

    public PlayerName(ByteBuf buf) {
        byte[] dst = new byte[buf.readInt()];
        buf.readBytes(dst);
        name = new String(dst);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(getName().length());
        buf.writeBytes(getName().getBytes());
    }

    public String getName() {
        return name;
    }
}
