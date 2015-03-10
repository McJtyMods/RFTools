package com.mcjty.rftools.dimension.network;

import com.mcjty.rftools.dimension.DimensionInformation;
import com.mcjty.rftools.dimension.description.DimensionDescriptor;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Sync RfToolsDimensionManager data from server to client.
 */
public class PacketSyncDimensionInfo implements IMessage {
    private Map<Integer, DimensionDescriptor> dimensions;
    private Map<Integer, DimensionInformation> dimensionInformation;

    @Override
    public void fromBytes(ByteBuf buf) {
        int size = buf.readInt();
        dimensions = new HashMap<Integer, DimensionDescriptor>();
        for (int i = 0 ; i < size ; i++) {
            int id = buf.readInt();
            PacketBuffer buffer = new PacketBuffer(buf);
            NBTTagCompound tagCompound;
            try {
                tagCompound = buffer.readNBTTagCompoundFromBuffer();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            DimensionDescriptor descriptor = new DimensionDescriptor(tagCompound);
            dimensions.put(id, descriptor);
        }

        size = buf.readInt();
        dimensionInformation = new HashMap<Integer, DimensionInformation>();
        for (int i = 0 ; i < size ; i++) {
            int id = buf.readInt();
            byte[] dst = new byte[buf.readInt()];
            buf.readBytes(dst);
            String name = new String(dst);
            DimensionInformation dimInfo = new DimensionInformation(name, dimensions.get(id), buf);
            dimensionInformation.put(id, dimInfo);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(dimensions.size());
        for (Map.Entry<Integer,DimensionDescriptor> me : dimensions.entrySet()) {
            buf.writeInt(me.getKey());
            NBTTagCompound tagCompound = new NBTTagCompound();
            me.getValue().writeToNBT(tagCompound);
            PacketBuffer buffer = new PacketBuffer(buf);
            try {
                buffer.writeNBTTagCompoundToBuffer(tagCompound);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        buf.writeInt(dimensionInformation.size());
        for (Map.Entry<Integer, DimensionInformation> me : dimensionInformation.entrySet()) {
            buf.writeInt(me.getKey());
            DimensionInformation dimInfo = me.getValue();
            buf.writeInt(dimInfo.getName().length());
            buf.writeBytes(dimInfo.getName().getBytes());
            dimInfo.toBytes(buf);
        }
    }

    public Map<Integer, DimensionDescriptor> getDimensions() {
        return dimensions;
    }

    public Map<Integer, DimensionInformation> getDimensionInformation() {
        return dimensionInformation;
    }

    public PacketSyncDimensionInfo() {
    }

    public PacketSyncDimensionInfo(Map<Integer, DimensionDescriptor> dimensions, Map<Integer, DimensionInformation> dimensionInformation) {
        this.dimensions = new HashMap<Integer, DimensionDescriptor>(dimensions);
        this.dimensionInformation = new HashMap<Integer, DimensionInformation>(dimensionInformation);
    }

}
