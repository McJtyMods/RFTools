package com.mcjty.rftools.network;

import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.dimension.world.GenericWorldProvider;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraftforge.common.DimensionManager;

public class DimensionSyncPacket {

    private ByteBuf data = Unpooled.buffer();

    private byte[] dimensions;

    public void addDimension(int id) {
        data.writeByte(id);
    }

    public void consumePacket(ByteBuf data) {
        dimensions = new byte[data.readableBytes()];
        data.readBytes(dimensions);
    }

    public ByteBuf getData() {
        return data;
    }

    public void execute() {
        // Only do this on client side.
        for (byte id : dimensions) {
            RFTools.log("DimensionSyncPacket: Registering id: id = " + id);
            if (!DimensionManager.isDimensionRegistered(id)) {
                DimensionManager.registerProviderType(id, GenericWorldProvider.class, false);
                DimensionManager.registerDimension(id, id);
            }
        }
    }

}
