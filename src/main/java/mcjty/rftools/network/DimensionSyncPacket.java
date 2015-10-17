package mcjty.rftools.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import mcjty.lib.varia.Logging;
import mcjty.rftools.dimension.world.GenericWorldProvider;
import net.minecraftforge.common.DimensionManager;

public class DimensionSyncPacket {

    private ByteBuf data = Unpooled.buffer();

    private int[] dimensions;

    public void addDimension(int id) {
        data.writeInt(id);
    }

    public void consumePacket(ByteBuf data) {
        int cnt = data.readableBytes() / 4;
        dimensions = new int[cnt];
        for (int i = 0 ; i < cnt ; i++) {
            dimensions[i] = data.readInt();
        }
    }

    public ByteBuf getData() {
        return data;
    }

    public void execute() {
        // Only do this on client side.
        for (int id : dimensions) {
            Logging.log("DimensionSyncPacket: Registering id: id = " + id);
            if (!DimensionManager.isDimensionRegistered(id)) {
                DimensionManager.registerProviderType(id, GenericWorldProvider.class, false);
                DimensionManager.registerDimension(id, id);
            }
        }
    }

}
