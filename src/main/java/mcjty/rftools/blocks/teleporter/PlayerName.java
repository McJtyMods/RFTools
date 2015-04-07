package mcjty.rftools.blocks.teleporter;

import io.netty.buffer.ByteBuf;
import mcjty.rftools.network.ByteBufConverter;
import mcjty.rftools.network.NetworkTools;

public class PlayerName implements ByteBufConverter {
    private final String name;

    public PlayerName(String name) {
        this.name = name;
    }

    public PlayerName(ByteBuf buf) {
        name = NetworkTools.readString(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        NetworkTools.writeString(buf, getName());
    }

    public String getName() {
        return name;
    }
}
