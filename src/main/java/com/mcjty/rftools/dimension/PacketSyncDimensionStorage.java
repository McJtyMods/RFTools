package com.mcjty.rftools.dimension;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

/**
 * Sync DimensionStorage data from server to client.
 */
public class PacketSyncDimensionStorage implements IMessage ,IMessageHandler<PacketSyncDimensionStorage, IMessage> {
    private Map<Integer, Integer> energy;

    @Override
    public void fromBytes(ByteBuf buf) {
        int size = buf.readInt();
        energy = new HashMap<Integer, Integer>();
        for (int i = 0 ; i < size ; i++) {
            int id = buf.readInt();
            int rf = buf.readInt();
            energy.put(id, rf);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(energy.size());
        for (Map.Entry<Integer,Integer> me : energy.entrySet()) {
            buf.writeInt(me.getKey());
            buf.writeInt(me.getValue());
        }
    }

    public PacketSyncDimensionStorage() {
    }

    public PacketSyncDimensionStorage(Map<Integer, Integer> energy) {
        this.energy = energy;
    }

    @Override
    public IMessage onMessage(PacketSyncDimensionStorage message, MessageContext ctx) {
        World world = Minecraft.getMinecraft().theWorld;
        System.out.println("SYNC DIMENSION STORAGE: world.isRemote = " + world.isRemote);
        DimensionStorage dimensionStorage = DimensionStorage.getDimensionStorage(world);

        dimensionStorage.syncFromServer(message.energy);
        dimensionStorage.save(world);

        return null;
    }

}
